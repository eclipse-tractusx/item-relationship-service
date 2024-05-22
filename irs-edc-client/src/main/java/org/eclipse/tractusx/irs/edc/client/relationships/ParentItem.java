/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.relationships;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 * Customer
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class ParentItem {

    private String catenaXId;
    private Quantity quantity;
    private String businessPartner;
    private ZonedDateTime createdOn;
    private ZonedDateTime lastModifiedOn;

    public Relationship toRelationship(final String catenaXId, final BomLifecycle bomLifecycle,
            final AspectType aspectType) {
        final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                  .childCatenaXId(
                                                                          GlobalAssetIdentification.of(catenaXId))
                                                                  .lifecycleContext(bomLifecycle)
                                                                  .hasAlternatives(Boolean.FALSE)
                                                                  .assembledOn(this.createdOn)
                                                                  .lastModifiedOn(this.lastModifiedOn);

        if (thereIsQuantity()) {
            linkedItem.quantity(org.eclipse.tractusx.irs.component.Quantity.builder()
                                                                           .quantityNumber(
                                                                                   this.quantity.getQuantityNumber())
                                                                           .measurementUnit(MeasurementUnit.builder()
                                                                                                           .lexicalValue(
                                                                                                                   this.quantity.getMeasurementUnit())
                                                                                                           .build())
                                                                           .build());
        }

        return Relationship.builder()
                           .catenaXId(GlobalAssetIdentification.of(this.catenaXId))
                           .linkedItem(linkedItem.build())
                           .bpn(businessPartner)
                           .aspectType(aspectType.toString())
                           .build();
    }

    private boolean thereIsQuantity() {
        return this.quantity != null;
    }

    /**
     * Quantity
     */
    @Data
    /* package */ static class Quantity {

        @JsonAlias({ "quantityNumber",
                     "value"
        })
        private Double quantityNumber;
        @JsonAlias({ "measurementUnit",
                     "unit"
        })
        private String measurementUnit;

    }
}
