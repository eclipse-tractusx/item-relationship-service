//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
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
