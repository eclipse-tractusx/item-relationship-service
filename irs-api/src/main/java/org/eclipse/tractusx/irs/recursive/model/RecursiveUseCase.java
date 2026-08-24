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
package org.eclipse.tractusx.irs.recursive.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 * Known recursive use cases with their lifecycle and payload aspect bundles.
 *
 * <h3>Use-Case Packages</h3>
 * <p>Each use case is the single source of truth for its allowed BOM lifecycles and
 * payload aspects. Callers may select a supported lifecycle and an aspect subset;
 * omitted values use the configured defaults.</p>
 *
 * <h3>Extending for new use cases</h3>
 * <p>Add the supported semantic IDs to {@link RecursiveAspect}, then add a use-case
 * constant with its allowed lifecycles and aspect bundle. No service-specific
 * registration is required.</p>
 */
public enum RecursiveUseCase {

    /**
     * Recursive PURIS collection with the anonymized payload models (the only PURIS bundle in use).
     */
    PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE(
            BomLifecycle.AS_PLANNED,
            Set.of(BomLifecycle.AS_PLANNED),
            List.of(
                    RecursiveAspect.ITEM_STOCK_ANONYMIZED,
                    RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED,
                    RecursiveAspect.PLANNED_PRODUCTION_OUTPUT_ANONYMIZED
            )
    );

    private final BomLifecycle defaultBomLifecycle;
    private final Set<BomLifecycle> allowedBomLifecycles;
    private final List<RecursiveAspect> allowedAspects;
    private final List<String> aspectSemanticIds;

    RecursiveUseCase(final BomLifecycle defaultBomLifecycle, final Set<BomLifecycle> allowedBomLifecycles,
            final List<RecursiveAspect> allowedAspects) {
        if (!allowedBomLifecycles.contains(defaultBomLifecycle)) {
            throw new IllegalArgumentException("Default BOM lifecycle must be part of the allowed lifecycles");
        }
        this.defaultBomLifecycle = defaultBomLifecycle;
        this.allowedBomLifecycles = Set.copyOf(allowedBomLifecycles);
        this.allowedAspects = List.copyOf(allowedAspects);
        this.aspectSemanticIds = this.allowedAspects.stream().map(RecursiveAspect::getSemanticId).toList();
    }

    /**
     * Default traversal lifecycle for this use case.
     *
     * @return the lifecycle used when the request does not specify one
     */
    public BomLifecycle getDefaultBomLifecycle() {
        return defaultBomLifecycle;
    }

    public Set<BomLifecycle> getAllowedBomLifecycles() {
        return allowedBomLifecycles;
    }

    public List<RecursiveAspect> getAllowedAspects() {
        return allowedAspects;
    }

    /**
     * Semantic IDs of the aspects this use case needs to read from the DTR.
     *
     * @return the required semantic aspect IDs
     */
    public List<String> getAspectSemanticIds() {
        return aspectSemanticIds;
    }

    public BomLifecycle resolveLifecycle(final BomLifecycle requestedLifecycle) {
        final BomLifecycle resolved = requestedLifecycle == null ? defaultBomLifecycle : requestedLifecycle;
        if (!allowedBomLifecycles.contains(resolved)) {
            throw new IllegalArgumentException(
                    "bomLifecycle " + resolved + " is not supported for useCase " + name());
        }
        return resolved;
    }

    public Optional<RecursiveAspect> findAllowedAspect(final String semanticId) {
        return RecursiveAspect.fromSemanticId(semanticId).filter(allowedAspects::contains);
    }

    /**
     * Validates the complete selection used by a recursive job.
     * Missing, empty or unsupported selections are rejected at result boundaries so that
     * request defaulting cannot accidentally be repeated for persisted or partner-provided data.
     *
     * @param bomLifecycle selected traversal lifecycle
     * @param requestedAspectIds requested, persisted or partner-provided aspect IDs
     * @return canonical IDs that belong to this use-case bundle, or empty when the selection is invalid
     */
    public Optional<Set<String>> selectAspectIds(final BomLifecycle bomLifecycle,
            final Collection<String> requestedAspectIds) {
        if (bomLifecycle == null || !allowedBomLifecycles.contains(bomLifecycle)
                || requestedAspectIds == null || requestedAspectIds.isEmpty()) {
            return Optional.empty();
        }
        final Set<String> result = new LinkedHashSet<>();
        for (final String requestedAspectId : requestedAspectIds) {
            final Optional<RecursiveAspect> aspect = findAllowedAspect(requestedAspectId);
            if (aspect.isEmpty()) {
                return Optional.empty();
            }
            result.add(aspect.get().getSemanticId());
        }
        return Optional.of(Collections.unmodifiableSet(result));
    }

}
