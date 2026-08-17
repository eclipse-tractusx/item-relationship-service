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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneScope;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneType;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/**
 * Creates and sanitizes recursive tombstones.
 *
 * <p>Every tombstone that leaves this IRS instance goes through
 * {@link #sanitized(RecursiveTombstone, List)} or one of the factory methods. Details are
 * anonymized so no partner BPNs, URLs or asset IDs can leak through error reporting.</p>
 *
 * <p>Each tombstone carries an identity-free {@code errorRef}. It is generated once at creation and
 * preserved through sanitization, and is logged at creation so support can map a customer-quoted
 * errorRef back to the detailed internal diagnostics.</p>
 */
@Slf4j
@SuppressWarnings("PMD.TooManyMethods")
final class RecursiveTombstones {

    private RecursiveTombstones() {
    }

    /* package */ static RecursiveTombstone chain(final List<String> aspects,
            final RecursiveTombstoneReason reason, final String detail) {
        return create(RecursiveTombstoneScope.RECURSIVE_CHAIN, aspectValue(aspects), reason, detail);
    }

    /* package */ static RecursiveTombstone childBranch(final List<String> aspects,
            final RecursiveTombstoneReason reason, final String detail) {
        return create(RecursiveTombstoneScope.CHILD_BRANCH, aspectValue(aspects), reason, detail);
    }

    /* package */ static RecursiveTombstone childBranch(final List<String> aspects,
            final RecursiveTombstoneReason reason, final String detail, final List<String> errorRefs) {
        return create(RecursiveTombstoneScope.CHILD_BRANCH, aspectValue(aspects), reason, detail,
                sanitizedErrorRefs(errorRefs), 1);
    }

    /* package */ static RecursiveTombstone local(final String aspect, final RecursiveTombstoneReason reason,
            final String detail) {
        return create(RecursiveTombstoneScope.LOCAL_NODE, aspect == null ? List.of() : List.of(aspect), reason,
                detail);
    }

    /**
     * Re-creates a valid tombstone received from a child or read from persisted state so that only
     * whitelisted fields with sanitized values remain.
     *
     * @param raw             the raw tombstone
     * @param selectedAspects aspects selected for the current recursive job
     * @return a sanitized tombstone safe for external consumers
     */
    /* package */ static RecursiveTombstone sanitized(final RecursiveTombstone raw,
            final List<String> selectedAspects) {
        validateRequiredFields(raw);
        final List<String> aspects = sanitizedAspects(raw, selectedAspects);
        final String detail = RecursiveFailureDetails.anonymizedDetail(raw.getDetail());
        return create(raw.getScope(), aspects, raw.getReason(), detail,
                sanitizedErrorRefs(raw.getErrorRefs()), raw.getOccurrences());
    }

    /** Keeps only canonical aspects selected for the current job. */
    private static List<String> sanitizedAspects(final RecursiveTombstone raw,
            final List<String> selectedAspects) {
        final List<String> allowedAspects = aspectValue(selectedAspects).stream()
                .map(RecursiveAspect::fromSemanticId)
                .flatMap(Optional::stream)
                .map(RecursiveAspect::getSemanticId)
                .distinct()
                .toList();
        return aspectValue(raw.getAspects()).stream()
                .map(RecursiveAspect::fromSemanticId)
                .flatMap(Optional::stream)
                .map(RecursiveAspect::getSemanticId)
                .filter(allowedAspects::contains)
                .distinct()
                .toList();
    }

    private static List<String> sanitizedErrorRefs(final List<String> errorRefs) {
        if (errorRefs == null || errorRefs.isEmpty()) {
            return List.of(newErrorRef());
        }
        final List<String> sanitized = errorRefs.stream()
                                                .filter(java.util.Objects::nonNull)
                                                .map(RecursiveTombstones::sanitizedErrorRef)
                                                .toList();
        return sanitized.isEmpty() ? List.of(newErrorRef()) : sanitized;
    }

    private static String sanitizedErrorRef(final String errorRef) {
        try {
            return UUID.fromString(errorRef).toString();
        } catch (final IllegalArgumentException exception) {
            return newErrorRef();
        }
    }

    private static RecursiveTombstone create(final RecursiveTombstoneScope scope, final List<String> aspects,
            final RecursiveTombstoneReason reason, final String detail) {
        return create(scope, aspects, reason, detail, List.of(newErrorRef()), 1);
    }

    private static RecursiveTombstone create(final RecursiveTombstoneScope scope, final List<String> aspects,
            final RecursiveTombstoneReason reason, final String detail, final List<String> errorRefs,
            final int occurrences) {
        if (log.isDebugEnabled()) {
            log.debug("Recursive tombstone errorRefs={} scope={} reason={} detail={}",
                    RecursiveLogValue.of(errorRefs.toString()), RecursiveLogValue.of(scope.name()),
                    RecursiveLogValue.of(reason.name()), RecursiveLogValue.of(detail));
        }
        return RecursiveTombstone.builder()
                                 .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                                 .scope(scope)
                                 .aspects(List.copyOf(aspects))
                                 .reason(reason)
                                 .retryable(reason.isRetryable())
                                 .detail(detailValue(reason, detail))
                                 .occurrences(occurrences)
                                 .errorRefs(List.copyOf(errorRefs))
                                 .build();
    }

    private static String detailValue(final RecursiveTombstoneReason reason, final String detail) {
        final String rawDetail = detail == null || detail.isBlank()
                ? "Recursive tombstone reason: " + reason.name()
                : detail;
        final String anonymizedDetail = RecursiveFailureDetails.anonymizedDetail(rawDetail);
        if (!RecursivePatternStore.SAFE_SINGLE_LINE_PATTERN.matcher(anonymizedDetail).matches()) {
            return "Recursive tombstone reason: " + reason.name();
        }
        return anonymizedDetail;
    }

    private static String newErrorRef() {
        return UUID.randomUUID().toString();
    }

    private static void validateRequiredFields(final RecursiveTombstone raw) {
        if (raw == null || !hasRequiredEnums(raw) || !hasValidDetail(raw) || !hasValidCounts(raw)) {
            throw new IllegalArgumentException("Recursive tombstone is incomplete.");
        }
    }

    private static boolean hasRequiredEnums(final RecursiveTombstone raw) {
        return raw.getType() == RecursiveTombstoneType.RECURSIVE_TOMBSTONE
                && raw.getScope() != null
                && raw.getReason() != null
                && raw.getRetryable() != null
                && raw.getRetryable() == raw.getReason().isRetryable();
    }

    private static boolean hasValidDetail(final RecursiveTombstone raw) {
        return raw.getDetail() != null
                && !raw.getDetail().isBlank()
                && RecursivePatternStore.SAFE_SINGLE_LINE_PATTERN.matcher(raw.getDetail()).matches();
    }

    private static boolean hasValidCounts(final RecursiveTombstone raw) {
        return raw.getOccurrences() != null
                && raw.getOccurrences() > 0
                && raw.getAspects() != null
                && raw.getErrorRefs() != null
                && !raw.getErrorRefs().isEmpty();
    }

    private static List<String> aspectValue(final List<String> aspects) {
        return aspects == null ? List.of() : aspects.stream().filter(java.util.Objects::nonNull).toList();
    }
}
