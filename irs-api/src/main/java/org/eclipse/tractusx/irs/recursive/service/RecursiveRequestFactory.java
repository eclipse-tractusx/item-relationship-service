/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.util.RecursiveGlobalAssetId;

/**
 * Validates and normalizes incoming recursive job requests and turns them into the initial
 * persisted job state: lifecycle and aspect validation against the use-case bundle, deadline
 * resolution (TTL for root jobs, inherited {@code expectedResponseBy} for
 * child jobs) and message-id handling.
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveRequestFactory {

    private final RecursiveProperties properties;
    private final Clock clock;

    /**
     * Normalizes the request and resolves everything needed to accept the job.
     *
     * @param request              the raw root or child request
     * @param isRootJob            true for jobs started via the public API
     * @param inheritedDeadline    the parent's {@code expectedResponseBy} for child jobs
     * @param inheritedMessageId   parent message id for child jobs (used as response correlation); null for root jobs
     * @return all derived values; turn into a state via {@link PreparedRecursiveJob#toState(UUID)}
     */
    /* package */ PreparedRecursiveJob prepare(final RecursiveJobRequest request, final boolean isRootJob,
            final ZonedDateTime inheritedDeadline, final String inheritedMessageId) {
        final String globalAssetId = resolveGlobalAssetId(request);
        final RecursiveUseCase useCase = Objects.requireNonNull(request.getUseCase(), "useCase must be provided");
        final BomLifecycle bomLifecycle = useCase.resolveLifecycle(request.getBomLifecycle());
        final List<String> aspects = resolveAspects(request, useCase);
        final String localBpnl = properties.getLocalBpnl();

        final String requesterBpnl = isRootJob ? localBpnl : request.getRequesterBpn();
        final String messageId = inheritedMessageId != null && !inheritedMessageId.isBlank()
                ? inheritedMessageId
                : UUID.randomUUID().toString();
        final ZonedDateTime createdOn = ZonedDateTime.now(clock);
        final ZonedDateTime deadline = resolveDeadline(request, isRootJob, inheritedDeadline, createdOn);

        return new PreparedRecursiveJob(request.getOpeningId(), useCase, globalAssetId, bomLifecycle,
                aspects, requesterBpnl, localBpnl, messageId, createdOn, deadline,
                childResponseDeadline(deadline, createdOn), isRootJob);
    }

    /* package */ static ZonedDateTime parseExpectedResponseBy(final String expectedResponseBy) {
        if (expectedResponseBy == null || expectedResponseBy.isBlank()) {
            throw new RecursiveNotificationValidationException("expectedResponseBy is required for partner requests");
        }
        try {
            return OffsetDateTime.parse(expectedResponseBy, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                 .toZonedDateTime();
        } catch (final DateTimeParseException e) {
            throw new RecursiveNotificationValidationException(
                    "expectedResponseBy must be an ISO-8601 offset timestamp", e);
        }
    }

    private String resolveGlobalAssetId(final RecursiveJobRequest request) {
        final String globalAssetId = Optional.ofNullable(request.getGlobalAssetId())
                                             .filter(value -> !value.isBlank())
                                             .orElseThrow(() -> new IllegalArgumentException(
                                                     "globalAssetId must be provided."));
        return RecursiveGlobalAssetId.canonicalize(globalAssetId);
    }

    private List<String> resolveAspects(final RecursiveJobRequest request, final RecursiveUseCase useCase) {
        final List<String> requested = request.getAspects();
        if (requested == null || requested.isEmpty()) {
            return useCase.getAspectSemanticIds();
        }

        final List<String> unsupported = requested.stream()
                .filter(aspect -> useCase.findAllowedAspect(aspect).isEmpty())
                .map(aspect -> aspect == null || aspect.isBlank() ? "<blank>" : aspect)
                .distinct()
                .toList();
        if (!unsupported.isEmpty()) {
            throw new RecursiveUnsupportedAspectException(unsupported, useCase.getAspectSemanticIds());
        }

        return requested.stream()
                .map(useCase::findAllowedAspect)
                .flatMap(Optional::stream)
                .map(RecursiveAspect::getSemanticId)
                .distinct()
                .toList();
    }

    private ZonedDateTime resolveDeadline(final RecursiveJobRequest request, final boolean isRootJob,
            final ZonedDateTime inheritedDeadline, final ZonedDateTime creationTime) {
        if (!isRootJob) {
            if (inheritedDeadline == null) {
                throw new RecursiveNotificationValidationException(
                        "expectedResponseBy is required for partner requests");
            }
            return inheritedDeadline;
        }
        return creationTime.plus(effectiveJobTtl(request.getTtl()));
    }

    private Duration effectiveJobTtl(final String ttl) {
        final RecursiveProperties.Timeout timeout = properties.getTimeout();
        Duration requestedTtl = timeout.getDefaultJobTtl();
        if (ttl != null && !ttl.isBlank()) {
            try {
                requestedTtl = Duration.parse(ttl);
            } catch (final DateTimeParseException e) {
                log.warn("Could not parse recursive job ttl, using {}", timeout.getDefaultJobTtl());
                requestedTtl = timeout.getDefaultJobTtl();
            }
        }
        return requestedTtl.compareTo(timeout.getMaxJobTtl()) > 0 ? timeout.getMaxJobTtl() : requestedTtl;
    }

    private ZonedDateTime childResponseDeadline(final ZonedDateTime deadline, final ZonedDateTime referenceTime) {
        final ZonedDateTime bufferedDeadline =
                deadline.minus(properties.getTimeout().getChildResponseSafetyBuffer());
        return bufferedDeadline.isAfter(referenceTime) ? bufferedDeadline : deadline;
    }

    /**
     * Everything derived from an accepted request; the job id is assigned by the caller after
     * duplicate and grant checks have passed.
     */
    record PreparedRecursiveJob(String openingId, RecursiveUseCase useCase, String globalAssetId,
            BomLifecycle bomLifecycle, List<String> aspects, String requesterBpnl, String receiverBpnl, String messageId,
            ZonedDateTime createdOn, ZonedDateTime deadline, ZonedDateTime childResponseDeadline, boolean rootJob) {

        /* package */ RecursiveJobState toState(final UUID jobId) {
            return RecursiveJobState.builder()
                                    .jobId(jobId)
                                    .openingId(openingId)
                                    .useCase(useCase)
                                    .globalAssetId(globalAssetId)
                                    .bomLifecycle(bomLifecycle)
                                    .aspects(aspects)
                                    .requesterBpnl(requesterBpnl)
                                    .receiverBpnl(receiverBpnl)
                                    .messageId(messageId)
                                    .createdOn(createdOn)
                                    .lastModifiedOn(createdOn)
                                    .deadline(deadline)
                                    .childResponseDeadline(childResponseDeadline)
                                    .state(RecursiveJobPhase.GRANT_CHECKED)
                                    .rootJob(rootJob)
                                    .bomChildren(List.of())
                                    .childBranches(List.of())
                                    .build();
        }
    }
}
