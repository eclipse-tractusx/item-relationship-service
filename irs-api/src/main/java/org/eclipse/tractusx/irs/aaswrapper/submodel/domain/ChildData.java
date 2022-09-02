/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.MeasurementUnit;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.dto.RelationshipAspect;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class ChildData {

    private ZonedDateTime assembledOn;
    private Quantity quantity;
    private ZonedDateTime lastModifiedOn;
    private LifecycleContextCharacteristic lifecycleContext;
    private String childCatenaXId;

    public Relationship toRelationship(final String catenaXId, final RelationshipAspect relationshipAspect) {
        final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                  .childCatenaXId(GlobalAssetIdentification.of(this.childCatenaXId))
                                                                  .lifecycleContext(
                                                                          BomLifecycle.fromLifecycleContextCharacteristic(
                                                                                  this.lifecycleContext.getValue()))
                                                                  .assembledOn(this.assembledOn)
                                                                  .lastModifiedOn(this.lastModifiedOn);

        if (thereIsQuantity()) {
            final String datatypeURI = thereIsMeasurementUnit() ? this.quantity.getMeasurementUnit().getDatatypeURI() : null;
            final String lexicalValue = thereIsMeasurementUnit() ? this.quantity.getMeasurementUnit().getLexicalValue() : null;

            linkedItem.quantity(org.eclipse.tractusx.irs.component.Quantity.builder()
                                                                           .quantityNumber(this.quantity.getQuantityNumber())
                                                                           .measurementUnit(MeasurementUnit.builder()
                                                                                                  .datatypeURI(datatypeURI)
                                                                                                  .lexicalValue(lexicalValue)
                                                                                                  .build())
                                                                           .build());
        }

        return Relationship.builder()
                           .catenaXId(GlobalAssetIdentification.of(catenaXId))
                           .linkedItem(linkedItem.build())
                           .aspectType(relationshipAspect.name())
                           .build();
    }

    private boolean thereIsMeasurementUnit() {
        return this.quantity != null && this.quantity.getMeasurementUnit() != null;
    }

    private boolean thereIsQuantity() {
        return this.quantity != null;
    }
}
