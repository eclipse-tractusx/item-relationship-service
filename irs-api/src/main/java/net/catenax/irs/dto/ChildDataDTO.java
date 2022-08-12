//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//
package net.catenax.irs.dto;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.LinkedItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.MeasurementUnit;
import net.catenax.irs.component.Quantity;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.enums.BomLifecycle;

/**
 * ChildDataDTO model used for internal application use
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ChildDataDTO {

    private ZonedDateTime assembledOn;

    private QuantityDTO quantity;

    private ZonedDateTime lastModifiedOn;

    private String lifecycleContext;

    private String childCatenaXId;

    public Relationship toRelationship(final String catenaXId, final RelationshipAspect relationshipAspect) {
        final LinkedItem.LinkedItemBuilder linkedItem = LinkedItem.builder()
                                                                .childCatenaXId(GlobalAssetIdentification.of(getChildCatenaXId()))
                                                                .lifecycleContext(
                                                                BomLifecycle.fromLifecycleContextCharacteristic(
                                                                        getLifecycleContext()))
                                                                .assembledOn(getAssembledOn())
                                                                .lastModifiedOn(getLastModifiedOn());

        if (this.getQuantity() != null) {
            linkedItem.quantity(Quantity.builder()
                                       .quantityNumber(getQuantity().getQuantityNumber())
                                       .measurementUnit(MeasurementUnit.builder()
                                                                       .datatypeURI(getQuantity().getMeasurementUnit()
                                                                                                 .getDatatypeURI())
                                                                       .lexicalValue(getQuantity().getMeasurementUnit()
                                                                                                  .getLexicalValue())
                                                                       .build())
                                       .build());
        }

        return Relationship.builder()
                           .catenaXId(GlobalAssetIdentification.of(catenaXId))
                           .linkedItem(linkedItem.build())
                           .aspectType(relationshipAspect.name())
                           .build();
    }

}
