//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Collection;

/**
 * JPA embeddable entity part representing a part identifier.
 */
@Embeddable
@Data // safe on this class as it is not an @Entity, and it has no JPA relationships
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameterEntityPart implements Serializable {

    /**
     * Readable ID of manufacturer including plant.
     */
    @NotEmpty
    private BomLifecycle bomLifecycle;

    /**
     * Unique identifier of a single, unique physical (sub)component/part/batch,
     * given by its manufacturer.
     * For a vehicle, the Vehicle Identification Number (VIN).
     */
    @NotEmpty
    private Collection<AspectType> aspects;

    @NotEmpty
    private Integer depth;

    @NotEmpty
    private Direction direction;
}
