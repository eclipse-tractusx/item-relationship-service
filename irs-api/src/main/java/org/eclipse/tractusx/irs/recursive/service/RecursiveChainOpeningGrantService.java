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
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.util.RecursiveGlobalAssetId;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Manages chain opening grants and performs the technical gate check
 * that every recursive hop must pass before traversal.
 *
 * <p>Grants are identified by {@code openingId + globalAssetId + requesterBpn + useCase}
 * ({@link RecursiveChainOpeningGrantKey}): several grants may exist for the same opening,
 * one per requested material and requesting partner.</p>
 */
@Slf4j
public class RecursiveChainOpeningGrantService {

    private final RecursiveChainOpeningGrantStore grantStore;
    private final Clock clock;

    public RecursiveChainOpeningGrantService(final RecursiveChainOpeningGrantStore grantStore) {
        this(grantStore, Clock.systemUTC());
    }

    public RecursiveChainOpeningGrantService(final RecursiveChainOpeningGrantStore grantStore, final Clock clock) {
        this.grantStore = grantStore;
        this.clock = clock;
    }

    /**
     * Stores a new grant. Fails when a grant with the same key already exists.
     *
     * @param grant the grant to store
     * @return the persisted grant with audit timestamps
     */
    public RecursiveChainOpeningGrant registerGrant(final RecursiveChainOpeningGrant grant) {
        final RecursiveChainOpeningGrant normalizedGrant = normalizeGrant(grant);
        Objects.requireNonNull(normalizedGrant.getUseCase(), "useCase must be provided");
        final RecursiveChainOpeningGrantKey key = RecursiveChainOpeningGrantKey.of(normalizedGrant);
        if (grantStore.find(key).isPresent()) {
            throw new RecursiveChainOpeningGrantAlreadyExistsException("Grant already exists for " + key);
        }
        final RecursiveChainOpeningGrant auditedGrant = withAuditMetadata(normalizedGrant);
        log.info("Registering grant for {}, allowedBpnls={}", RecursiveLogValue.of(key.toString()),
                allowedBpnlSize(auditedGrant));
        grantStore.store(auditedGrant);
        return auditedGrant;
    }

    /**
     * Stores or replaces a grant and records the update timestamp.
     *
     * @param grant the grant to replace
     * @return the persisted grant with audit timestamps
     */
    public RecursiveChainOpeningGrant replaceGrant(final RecursiveChainOpeningGrant grant) {
        final RecursiveChainOpeningGrant normalizedGrant = normalizeGrant(grant);
        Objects.requireNonNull(normalizedGrant.getUseCase(), "useCase must be provided");
        final RecursiveChainOpeningGrant auditedGrant = withAuditMetadata(normalizedGrant);
        log.info("Replacing grant for {}, allowedBpnls={}, updatedAt={}",
                RecursiveLogValue.of(RecursiveChainOpeningGrantKey.of(normalizedGrant).toString()),
                allowedBpnlSize(auditedGrant), auditedGrant.getUpdatedAt());
        grantStore.store(auditedGrant);
        return auditedGrant;
    }

    /**
     * Validates that a grant exists for the full grant key and that it is
     * within its validity window.
     *
     * @param openingId     identifies the chain opening
     * @param useCase       identifies the recursive use case
     * @param requesterBpn  identifies the requesting partner
     * @param globalAssetId identifies the requested material at this node
     * @return the validated grant
     * @throws RecursiveChainOpeningGrantInactiveException if no valid grant exists
     */
    public RecursiveChainOpeningGrant getActiveGrant(final String openingId, final RecursiveUseCase useCase,
            final String requesterBpn, final String globalAssetId) {
        Objects.requireNonNull(useCase, "useCase must be provided");
        final String canonicalGlobalAssetId = RecursiveGlobalAssetId.canonicalize(globalAssetId);
        final RecursiveChainOpeningGrantKey key =
                new RecursiveChainOpeningGrantKey(openingId, canonicalGlobalAssetId, requesterBpn, useCase);
        final RecursiveChainOpeningGrant grant = grantStore.find(key)
                .orElseThrow(() -> new RecursiveChainOpeningGrantInactiveException("No grant found for " + key));

        final ZonedDateTime now = ZonedDateTime.now(clock);
        if (grant.getValidFrom() != null && now.isBefore(grant.getValidFrom())) {
            throw new RecursiveChainOpeningGrantInactiveException(
                    "Grant not yet valid. validFrom=" + grant.getValidFrom());
        }
        if (grant.getValidTo() != null && now.isAfter(grant.getValidTo())) {
            throw new RecursiveChainOpeningGrantInactiveException(
                    "Grant expired. validTo=" + grant.getValidTo());
        }

        log.info("Grant validated for {}", RecursiveLogValue.of(key.toString()));
        return grant;
    }

    /**
     * Intersects BOM-discovered partner BPNLs with the grant's allowed set.
     *
     * @param bomCandidates BPNLs found via BOM traversal
     * @param grant         the validated grant
     * @return only those BPNLs that are both in the BOM and in the allow-list
     */
    public Set<String> filterAllowedPartners(final Set<String> bomCandidates, final RecursiveChainOpeningGrant grant) {
        if (grant.getAllowedBpnlSet() == null || grant.getAllowedBpnlSet().isEmpty()) {
            log.info("Grant has empty allowedBpnlSet - leaf node, no granted child partners");
            return Collections.emptySet();
        }
        final Set<String> allowed = new HashSet<>(bomCandidates);
        allowed.retainAll(grant.getAllowedBpnlSet());
        log.info("Partner filter: {} BOM candidates, {} allowed by grant, {} after intersection",
                bomCandidates.size(), grant.getAllowedBpnlSet().size(), allowed.size());
        return Collections.unmodifiableSet(allowed);
    }

    public List<RecursiveChainOpeningGrant> findGrants(final String openingId, final String globalAssetId,
            final String requesterBpn, final RecursiveUseCase useCase, final boolean validOnly) {
        final ZonedDateTime now = ZonedDateTime.now(clock);
        final String canonicalGlobalAssetId = RecursiveGlobalAssetId.canonicalizeOptional(globalAssetId);
        if (isCompleteGrantKey(openingId, canonicalGlobalAssetId, requesterBpn, useCase)) {
            final RecursiveChainOpeningGrantKey key = new RecursiveChainOpeningGrantKey(openingId,
                    canonicalGlobalAssetId, requesterBpn, useCase);
            return grantStore.find(key)
                    .filter(grant -> !validOnly || isValidAt(grant, now))
                    .stream()
                    .toList();
        }
        return grantStore.findAll().stream()
                .filter(grant -> StringUtils.isBlank(openingId) || openingId.equals(grant.getOpeningId()))
                .filter(grant -> StringUtils.isBlank(canonicalGlobalAssetId)
                        || canonicalGlobalAssetId.equals(grant.getGlobalAssetId()))
                .filter(grant -> StringUtils.isBlank(requesterBpn) || requesterBpn.equals(grant.getRequesterBpn()))
                .filter(grant -> useCase == null || useCase.equals(grant.getUseCase()))
                .filter(grant -> !validOnly || isValidAt(grant, now))
                .sorted(Comparator
                        .comparing(RecursiveChainOpeningGrant::getOpeningId,
                                Comparator.nullsLast(String::compareTo))
                        .thenComparing(RecursiveChainOpeningGrant::getGlobalAssetId,
                                Comparator.nullsLast(String::compareTo))
                        .thenComparing(RecursiveChainOpeningGrant::getRequesterBpn,
                                Comparator.nullsLast(String::compareTo))
                        .thenComparing(grant -> grant.getUseCase() == null ? null : grant.getUseCase().name(),
                                Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    public boolean deleteGrant(final String openingId, final String globalAssetId, final String requesterBpn,
            final RecursiveUseCase useCase) {
        final String canonicalGlobalAssetId = RecursiveGlobalAssetId.canonicalize(globalAssetId);
        final RecursiveChainOpeningGrantKey key = new RecursiveChainOpeningGrantKey(openingId, canonicalGlobalAssetId,
                requesterBpn, useCase);
        return grantStore.find(key).isPresent() && grantStore.remove(key);
    }

    private RecursiveChainOpeningGrant normalizeGrant(final RecursiveChainOpeningGrant grant) {
        return grant.toBuilder()
                .globalAssetId(RecursiveGlobalAssetId.canonicalize(grant.getGlobalAssetId()))
                .build();
    }

    private RecursiveChainOpeningGrant withAuditMetadata(final RecursiveChainOpeningGrant grant) {
        final ZonedDateTime now = ZonedDateTime.now(clock);
        final ZonedDateTime createdAt = grantStore.find(RecursiveChainOpeningGrantKey.of(grant))
                .map(RecursiveChainOpeningGrant::getCreatedAt)
                .orElse(now);
        return grant.toBuilder()
                .createdAt(createdAt)
                .updatedAt(now)
                .build();
    }

    private boolean isValidAt(final RecursiveChainOpeningGrant grant, final ZonedDateTime now) {
        return (grant.getValidFrom() == null || !now.isBefore(grant.getValidFrom()))
                && (grant.getValidTo() == null || !now.isAfter(grant.getValidTo()));
    }

    private boolean isCompleteGrantKey(final String openingId, final String globalAssetId,
            final String requesterBpn, final RecursiveUseCase useCase) {
        return StringUtils.isNoneBlank(openingId, globalAssetId, requesterBpn) && useCase != null;
    }

    private int allowedBpnlSize(final RecursiveChainOpeningGrant grant) {
        return grant.getAllowedBpnlSet() == null ? 0 : grant.getAllowedBpnlSet().size();
    }
}
