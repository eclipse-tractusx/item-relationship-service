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
import net.catenax.irs.component.Job;
import net.catenax.irs.dtos.ItemLifecycleStage;

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
    @NotNull
    @Valid
    private Job parentId;

    /**
     * Part identifier of the child in the relationship.
     */
    @Embedded
    @NotNull
    @Valid
    private Job childId;

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
    private ItemLifecycleStage lifeCycleStage;
}
