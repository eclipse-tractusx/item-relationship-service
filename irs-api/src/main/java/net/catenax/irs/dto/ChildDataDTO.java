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

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.ChildItem;
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

    private LocalDateTime assembledOn;

    private QuantityDTO quantity;

    private LocalDateTime lastModifiedOn;

    private String lifecycleContext;

    private String childCatenaXId;

    public Relationship toRelationship(final String catenaXId) {
        ChildItem.ChildItemBuilder childItem = ChildItem.builder()
                                                        .childCatenaXId(
                                                                withGlobalAssetIdentification(getChildCatenaXId()))
                                                        .lifecycleContext(
                                                                BomLifecycle.fromLifecycleContextCharacteristic(
                                                                        getLifecycleContext()))
                                                        .assembledOn(getAssembledOn())
                                                        .lastModifiedOn(getLastModifiedOn());

        if (this.getQuantity() != null) {
            childItem.quantity(Quantity.builder()
                                       .quantityNumber(getQuantity().getQuantityNumber().intValue())
                                       .measurementUnit(MeasurementUnit.builder()
                                                                       .datatypeURI(getQuantity().getMeasurementUnit()
                                                                                                 .getDatatypeURI())
                                                                       .lexicalValue(getQuantity().getMeasurementUnit()
                                                                                                  .getLexicalValue())
                                                                       .build())
                                       .build()).build();
        }

        return Relationship.builder()
                           .catenaXId(withGlobalAssetIdentification(catenaXId))
                           .childItem(childItem.build())
                           .build();
    }

    private GlobalAssetIdentification withGlobalAssetIdentification(final String catenaXId) {
        return GlobalAssetIdentification.builder().globalAssetId(catenaXId).build();
    }
}
