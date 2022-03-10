//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * JPA entity part representing the primary key of the {@link PartAspectEntity}.
 */
@Embeddable
@Data // safe on this class as it is not an @Entity, and it has no JPA relationships
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartAspectEntityKey implements Serializable {
    /**
     * Part identifier.
     */
    @Embedded
    @NotNull
    @Valid
    private PartIdEntityPart partId;

    /**
     * Item name.
     */
    @NotNull
    private String name;
}
