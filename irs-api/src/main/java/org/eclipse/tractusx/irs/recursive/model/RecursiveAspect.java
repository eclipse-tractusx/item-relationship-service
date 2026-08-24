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

import java.util.Arrays;
import java.util.Optional;

/**
 * Payload aspects that may be collected and returned by the recursive path.
 */
public enum RecursiveAspect {

    ITEM_STOCK_ANONYMIZED(
            "urn:samm:io.catenax.item_stock_anonymized:1.0.0#ItemStockAnonymized"),
    DELIVERY_INFORMATION_ANONYMIZED(
            "urn:samm:io.catenax.delivery_information_anonymized:1.0.0#DeliveryInformationAnonymized"),
    PLANNED_PRODUCTION_OUTPUT_ANONYMIZED(
            "urn:samm:io.catenax.planned_production_output_anonymized:1.0.0#PlannedProductionOutputAnonymized");

    private final String semanticId;
    private final String idShort;

    RecursiveAspect(final String semanticId) {
        this.semanticId = semanticId;
        this.idShort = semanticId.substring(semanticId.indexOf('#') + 1);
    }

    public String getSemanticId() {
        return semanticId;
    }

    public String getIdShort() {
        return idShort;
    }

    /**
     * Resolves the exact semantic ID accepted by the recursive API.
     *
     * @param semanticId semantic ID from a request, persisted state or child result
     * @return the matching supported aspect
     */
    public static Optional<RecursiveAspect> fromSemanticId(final String semanticId) {
        if (semanticId == null || semanticId.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(aspect -> aspect.semanticId.equals(semanticId))
                .findFirst();
    }

    /**
     * Matches the different representations used by DTR submodel descriptors.
     *
     * @param aspectType semantic ID representation from the descriptor
     * @param descriptorIdShort idShort representation from the descriptor
     * @return true when either descriptor representation identifies this aspect
     */
    public boolean matchesDescriptor(final String aspectType, final String descriptorIdShort) {
        if (aspectType != null && !aspectType.isBlank()) {
            return semanticId.equals(aspectType);
        }
        return idShort.equals(descriptorIdShort);
    }
}
