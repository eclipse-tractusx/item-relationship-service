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
import net.catenax.prs.dtos.PartLifecycleStage;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * JPA entity part representing the primary key of the {@link PartRelationshipEntity}.
 */
@Embeddable
@Data // safe on this class as it is not an @Entity, and it has no JPA relationships
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartRelationshipEntityKey implements Serializable {

    /**
     * Part identifier of the parent in the relationship.
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "oneIDManufacturer", column = @Column(name = "parentOneIDManufacturer")),
        @AttributeOverride(name = "objectIDManufacturer", column = @Column(name = "parentObjectIDManufacturer")),
    })
    @NotNull
    @Valid
    private PartIdEntityPart parentId;

    /**
     * Part identifier of the child in the relationship.
     */
    @Embedded
    @NotNull
    @Valid
    private PartIdEntityPart childId;

    /**
     * Instant at which part relationship came into effect.
     */
    @NotNull
    private Instant effectTime;

    /**
     * TRUE if the child is not part of the parent; FALSE otherwise.
     */
    @NotNull
    private Boolean removed;

    /**
     * Part was built, or a maintenance operation on the part after it was built.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private PartLifecycleStage lifeCycleStage;
}
