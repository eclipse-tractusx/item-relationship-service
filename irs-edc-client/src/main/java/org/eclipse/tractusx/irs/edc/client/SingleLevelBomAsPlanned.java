/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc.client;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 * SingleLevelBomAsPlanned
 */
@Data
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
class SingleLevelBomAsPlanned implements RelationshipSubmodel {

    private String catenaXId;
    private Set<ChildData> childItems;

    @Override
    public List<Relationship> asRelationships() {
        return Optional.ofNullable(this.childItems).stream().flatMap(Collection::stream)
                       .map(childData -> childData.toRelationship(this.catenaXId))
                       .toList();
    }

    /**
     * ChildData
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    /* package */ static class ChildData {

        private ZonedDateTime createdOn;
        private Quantity quantity;
        private ZonedDateTime lastModifiedOn;
        private String catenaXId;
        private String businessPartner;

        public Relationship toRelationship(final String catenaXId) {
            final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                      .childCatenaXId(GlobalAssetIdentification.of(this.catenaXId))
                                                                      .lifecycleContext(BomLifecycle.AS_PLANNED)
                                                                      .hasAlternatives(Boolean.FALSE)
                                                                      .assembledOn(this.createdOn)
                                                                      .lastModifiedOn(this.lastModifiedOn);

            if (thereIsQuantity()) {
                MeasurementUnit measurementUnit = MeasurementUnit.builder().build();
                if (this.quantity.getMeasurementUnit() instanceof String str) {
                    measurementUnit = MeasurementUnit.builder()
                                                     .lexicalValue(str)
                                                     .build();
                } else if (this.quantity.getMeasurementUnit() instanceof Map<?, ?> map) {
                    measurementUnit = MeasurementUnit.builder()
                                                     .lexicalValue(String.valueOf(map.get("lexicalValue")))
                                                     .datatypeURI(String.valueOf(map.get("datatypeURI")))
                                                     .build();
                }


                linkedItem.quantity(org.eclipse.tractusx.irs.component.Quantity.builder()
                                                                               .quantityNumber(this.quantity.getQuantityNumber())
                                                                               .measurementUnit(measurementUnit)
                                                                               .build());
            }

            return Relationship.builder()
                               .catenaXId(GlobalAssetIdentification.of(catenaXId))
                               .linkedItem(linkedItem.build())
                               .bpn(this.businessPartner)
                               .aspectType(AspectType.SINGLE_LEVEL_BOM_AS_PLANNED.toString())
                               .build();
        }

        private boolean thereIsQuantity() {
            return this.quantity != null;
        }

        /**
         * Quantity
         */
        @Data
        @Jacksonized
        /* package */ static class Quantity {

            private Double quantityNumber;
            private Object measurementUnit;

            /**
             * MeasurementUnit
             */
            @Data
            @Jacksonized
            /* package */ static class MeasurementUnit {
                private String lexicalValue;
                private String datatypeURI;
            }
        }
    }


}
