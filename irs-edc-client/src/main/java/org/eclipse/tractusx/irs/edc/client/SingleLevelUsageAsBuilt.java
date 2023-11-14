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
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
 * SingleLevelUsageAsBuilt
 */
@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
class SingleLevelUsageAsBuilt implements RelationshipSubmodel {

    private String catenaXId;
    private Set<Customer> customers;

    @Override
    public List<Relationship> asRelationships() {
        return Optional.ofNullable(this.customers).stream().flatMap(Collection::stream)
                       .map(customer -> customer.toRelationship(this.catenaXId))
                       .flatMap(Optional::stream)
                       .toList();
    }

    /**
     * Customer
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    /* package */ static class Customer {

        private String businessPartner;
        private ZonedDateTime createdOn;
        private ZonedDateTime lastModifiedOn;
        private Set<ParentItem> parentItems;

        public Optional<Relationship> toRelationship(final String catenaXId) {
            return parentItems.stream().findFirst().map(parentItem -> parentItem.toRelationship(catenaXId, this.businessPartner));
        }
    }
    /**
     * ParentData
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    /* package */ static class ParentItem {

        private String catenaXId;
        private Quantity quantity;
        private ZonedDateTime createdOn;
        private ZonedDateTime lastModifiedOn;

        public Relationship toRelationship(final String catenaXId, final String businessPartner) {
            final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                      .childCatenaXId(GlobalAssetIdentification.of(catenaXId))
                                                                      .lifecycleContext(BomLifecycle.AS_BUILT)
                                                                      .assembledOn(this.createdOn)
                                                                      .lastModifiedOn(this.lastModifiedOn);

            if (thereIsQuantity()) {
                linkedItem.quantity(org.eclipse.tractusx.irs.component.Quantity.builder()
                                                                               .quantityNumber(this.quantity.getQuantityNumber())
                                                                               .measurementUnit(MeasurementUnit.builder()
                                                                                                               .lexicalValue(this.quantity.getMeasurementUnit())
                                                                                                               .build())
                                                                               .build());
            }

            return Relationship.builder()
                               .catenaXId(GlobalAssetIdentification.of(this.catenaXId))
                               .linkedItem(linkedItem.build())
                               .bpn(businessPartner)
                               .aspectType(AspectType.SINGLE_LEVEL_USAGE_AS_BUILT.toString())
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
            private String measurementUnit;

        }
    }
}
