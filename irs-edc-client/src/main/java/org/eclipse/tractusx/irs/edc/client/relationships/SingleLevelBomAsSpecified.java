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
package org.eclipse.tractusx.irs.edc.client.relationships;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
import org.eclipse.tractusx.irs.edc.client.RelationshipSubmodel;

/**
 * SingleLevelBomAsSpecified
 */
@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class SingleLevelBomAsSpecified implements RelationshipSubmodel {

    @JsonAlias({ "catenaXId",
                 "assetId"
    })
    private String catenaXId;
    @JsonAlias({ "childParts",
                 "childItems"
    })
    private Set<ChildData> childParts;

    @Override
    public List<Relationship> asRelationships() {
        return Optional.ofNullable(this.childParts)
                       .stream()
                       .flatMap(Collection::stream)
                       .map(childData -> childData.toRelationship(this.catenaXId))
                       .toList();
    }

    /**
     * ChildData
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    /* package */ static class ChildData {

        @JsonAlias({ "childPartsCategory",
                     "childItemCategory"
        })
        private String childPartsCategory;
        @JsonAlias({ "part",
                     "item"
        })
        private Set<Part> part;
        @JsonAlias({ "childCatenaXId",
                     "childassetId"
        })
        private String childCatenaXId;
        private String businessPartner;

        public Relationship toRelationship(final String catenaXId) {
            final Part childPart = this.part.stream().findFirst().orElse(Part.builder().build());

            final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                      .childCatenaXId(GlobalAssetIdentification.of(
                                                                              this.childCatenaXId))
                                                                      .lifecycleContext(BomLifecycle.AS_SPECIFIED)
                                                                      .hasAlternatives(Boolean.FALSE)
                                                                      .assembledOn(childPart.getCreatedOn())
                                                                      .lastModifiedOn(childPart.getLastModifiedOn());

            if (childPart.getPartQuantity() != null) {
                linkedItem.quantity(Quantity.builder()
                                            .quantityNumber(childPart.getPartQuantity().getQuantityNumber())
                                            .measurementUnit(MeasurementUnit.builder()
                                                                            .lexicalValue(childPart.getPartQuantity()
                                                                                                   .getMeasurementUnit())
                                                                            .build())
                                            .build());
            }

            return Relationship.builder()
                               .catenaXId(GlobalAssetIdentification.of(catenaXId))
                               .linkedItem(linkedItem.build())
                               .bpn(this.businessPartner)
                               .aspectType(AspectType.SINGLE_LEVEL_BOM_AS_SPECIFIED.toString())
                               .build();
        }

        /**
         * Part
         */
        @Data
        @Builder
        @Jacksonized
        @SuppressWarnings("PMD.ShortClassName")
        /* package */ static class Part {

            @JsonAlias({ "ownerPartId",
                         "ownerItemId"
            })
            private String ownerPartId;
            @JsonAlias({ "partVersion",
                         "itemVersion"
            })
            private String partVersion;
            @JsonAlias({ "partQuantity",
                         "itemQuantity"
            })
            private PartQuantity partQuantity;
            @JsonAlias({ "partDescription",
                         "itemDescription"
            })
            private String partDescription;
            private ZonedDateTime createdOn;
            private ZonedDateTime lastModifiedOn;

            /**
             * Part Quantity
             */
            @Data
            @Builder
            @Jacksonized
            /* package */ static class PartQuantity {
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
    }
}
