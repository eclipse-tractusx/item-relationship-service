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
 * https://www.apache.org/licenses/LICENSE-2.0. *
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
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.Quantity;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 * SingleLevelBomAsSpecified
 */
@Data
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class SingleLevelBomAsSpecified implements RelationshipSubmodel {

    private String catenaXId;
    private Set<SingleLevelBomAsSpecified.ChildData> childParts;

    @Override
    public List<Relationship> asRelationships() {
        return Optional.ofNullable(this.childParts).stream().flatMap(Collection::stream)
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

        private String childPartsCategory;
        private Set<Part> part;
        private String childCatenaXId;

        public Relationship toRelationship(final String catenaXId) {
            final Part part = this.part.stream().findFirst().orElse(new Part());

            final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                      .childCatenaXId(GlobalAssetIdentification.of(this.childCatenaXId))
                                                                      .lifecycleContext(BomLifecycle.AS_SPECIFIED)
                                                                      .assembledOn(part.getCreatedOn())
                                                                      .lastModifiedOn(part.getLastModifiedOn());

            if (part.getPartQuantity() != null) {
                linkedItem.quantity(Quantity.builder()
                                            .quantityNumber(part.getPartQuantity().getQuantityNumber())
                                            .measurementUnit(MeasurementUnit.builder().lexicalValue(part.getPartQuantity().getMeasurementUnit()).build())
                                            .build());
            }

            return Relationship.builder()
                               .catenaXId(GlobalAssetIdentification.of(catenaXId))
                               .linkedItem(linkedItem.build())
                               .aspectType(AspectType.SINGLE_LEVEL_BOM_AS_SPECIFIED.toString())
                               .build();
        }

        /**
         * Part
         */
        @Data
        @Jacksonized
        @SuppressWarnings("PMD.ShortClassName")
        /* package */ static class Part {

            private String ownerPartId;
            private String partVersion;
            private PartQuantity partQuantity;
            private String partDescription;
            private ZonedDateTime createdOn;
            private ZonedDateTime lastModifiedOn;

            /**
             * Part Quantity
             */
            @Data
            @Jacksonized
            /* package */ static class PartQuantity {
                private Double quantityNumber;
                private String measurementUnit;
            }
        }
    }
}
