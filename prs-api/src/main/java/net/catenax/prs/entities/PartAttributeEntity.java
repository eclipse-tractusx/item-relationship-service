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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * JPA entity part representing an attribute key-value pair attached to a part identifier.
 */
@Entity
@Table(name = "part_attribute")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString // safe on this entity as it has no relationships
public class PartAttributeEntity implements Serializable {

    /**
     * The entity primary key, which itself contains the part identifier
     * and attribute name.
     */
    @EmbeddedId
    @NotNull
    @Valid
    private PartAttributeEntityKey key;

    /**
     * The attribute value.
     */
    @NotNull
    private String value;

    /**
     * Instant at which part attribute came into effect.
     */
    @NotNull
    private Instant effectTime;

    /**
     * The time at which the data was uploaded.
     */
    @NotNull
    private Instant lastModifiedTime;

}
