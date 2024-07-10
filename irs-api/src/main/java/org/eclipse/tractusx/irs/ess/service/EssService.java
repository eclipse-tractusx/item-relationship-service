/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.ess.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.SemanticModelNames;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Notification;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.ResponseNotificationContent;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business logic for investigation if part is in supply chain/part chain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssService {

    public static final String PART_AS_PLANNED = SemanticModelNames.PART_AS_PLANNED_1_0_1;
    public static final String PART_SITE_INFORMATION_AS_PLANNED = SemanticModelNames.PART_SITE_INFORMATION_AS_PLANNED_1_0_0;

    private final IrsItemGraphQueryService irsItemGraphQueryService;
    private final BpnInvestigationJobCache bpnInvestigationJobCache;
    private final JobStore jobStore;
    private final EssRecursiveNotificationHandler recursiveNotificationHandler;

    public JobHandle startIrsJob(final RegisterBpnInvestigationJob request) {
        return startIrsJob(request, null);
    }

    public JobHandle startIrsJob(final RegisterBpnInvestigationJob request, final UUID batchId) {
        final JobHandle jobHandle = irsItemGraphQueryService.registerItemJob(
                bpnInvestigations(request.getKey(), request.getBomLifecycle()), batchId);

        final UUID createdJobId = jobHandle.getId();
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(createdJobId.toString());
        multiTransferJob.ifPresent(job -> {
            final Jobs createdJob = irsItemGraphQueryService.getJobForJobId(job, true);
            bpnInvestigationJobCache.store(createdJobId,
                    new BpnInvestigationJob(createdJob, request.getIncidentBPNSs()));
        });

        return jobHandle;
    }

    public Jobs getIrsJob(final String jobId) {
        final Optional<BpnInvestigationJob> job = bpnInvestigationJobCache.findByJobId(UUID.fromString(jobId));

        if (job.isPresent()) {
            final BpnInvestigationJob bpnInvestigationJob = job.get();
            return updateState(bpnInvestigationJob);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No investigation job exists with id " + jobId);
        }
    }

    public void handleNotificationCallback(final EdcNotification<ResponseNotificationContent> notification) {
        log.info("Received notification response with id {}", notification.getHeader().getNotificationId());
        final var investigationJob = bpnInvestigationJobCache.findAll()
                                                             .stream()
                                                             .filter(investigationJobNotificationPredicate(
                                                                     notification))
                                                             .findFirst();

        investigationJob.ifPresent(job -> {
            final String originalNotificationId = notification.getHeader().getOriginalNotificationId();
            job.withAnsweredNotification(notification);
            final Optional<String> notificationResult = Optional.ofNullable(notification.getContent().getResult())
                                                                .map(Object::toString);

            final SupplyChainImpacted supplyChainImpacted = notificationResult.map(SupplyChainImpacted::fromString)
                                                                              .orElse(SupplyChainImpacted.UNKNOWN);
            log.debug("Received answer for Notification with id '{}' and investigation result '{}'.",
                    originalNotificationId, supplyChainImpacted);
            log.debug("Unanswered notifications left: '{}'", job.getUnansweredNotifications());
            final UUID jobId = job.getJobSnapshot().getJob().getId();

            if (job.getUnansweredNotifications().isEmpty()) {
                job = job.complete();
            }

            bpnInvestigationJobCache.store(jobId,
                    job.update(job.getJobSnapshot(), supplyChainImpacted));
            recursiveNotificationHandler.handleNotification(jobId, supplyChainImpacted,
                    notification.getContent().getBpn(), notification.getContent().getHops());
        });
    }

    private Jobs updateState(final BpnInvestigationJob investigationJob) {
        final Jobs jobSnapshot = investigationJob.getJobSnapshot();
        log.debug("Unanswered Notifications '{}'", investigationJob.getUnansweredNotifications());
        log.debug("Answered Notifications '{}'", investigationJob.getAnsweredNotifications());

        final JobState jobState = updateJobState(investigationJob);

        return jobSnapshot.toBuilder().job(jobSnapshot.getJob().toBuilder().state(jobState).build()).build();
    }

    private JobState updateJobState(final BpnInvestigationJob investigationJob) {
        if (hasUnansweredNotifications(investigationJob)) {
            return JobState.RUNNING;
        }
        if (hasAnsweredNotifications(investigationJob)) {
            return JobState.COMPLETED;
        }
        return investigationJob.getState();
    }

    private boolean hasAnsweredNotifications(final BpnInvestigationJob investigationJob) {
        return !investigationJob.getAnsweredNotifications().isEmpty();
    }

    private boolean hasUnansweredNotifications(final BpnInvestigationJob job) {
        return !job.getUnansweredNotifications().isEmpty();
    }

    private Predicate<BpnInvestigationJob> investigationJobNotificationPredicate(
            final EdcNotification<ResponseNotificationContent> notification) {
        return investigationJob -> investigationJob.getUnansweredNotifications()
                                                   .stream()
                                                   .map(Notification::notificationId)
                                                   .anyMatch(notificationId -> notificationId.equals(
                                                           notification.getHeader().getOriginalNotificationId()));
    }

    private RegisterJob bpnInvestigations(final PartChainIdentificationKey key, final BomLifecycle bomLifecycle) {
        return RegisterJob.builder()
                          .key(key)
                          .bomLifecycle(bomLifecycle != null ? bomLifecycle : BomLifecycle.AS_PLANNED)
                          .depth(1)
                          .aspects(List.of(PART_SITE_INFORMATION_AS_PLANNED, PART_AS_PLANNED))
                          .collectAspects(true)
                          .build();
    }

}
