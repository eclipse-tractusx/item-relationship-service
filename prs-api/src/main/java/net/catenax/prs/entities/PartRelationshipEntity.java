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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity part representing a relationship between two parts.
 */
@Entity
@Table(name = "part_relationship")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString // safe on this entity as it has no relationships
public class PartRelationshipEntity implements Serializable {
    /**
     * The entity primary key, which itself contains most of the relationship
     * information (parent and child twin identifiers).
     */
    @EmbeddedId
    @NotNull
    @Valid
    private PartRelationshipEntityKey key;

    /**
     * A value linking {@link PartRelationshipEntity} tuples originating in the same uploaded message.
     */
    @NotNull
    private UUID partRelationshipListId;

    /**
     * The time at which the data was uploaded.
     */
    @NotNull
    private Instant uploadDateTime;

    /**
     * JPA Version attribute.
     * <p>
     * If there is a Version-property of non-primitive type,
     * Spring Data JPA considers the entity new if the value of that property is null.
     * This allows using persist (i.e. INSERT) rather than merge (i.e. UPDATE)
     * and programmatically detect duplicates.
     */
    @Version
    private Long version;
}
