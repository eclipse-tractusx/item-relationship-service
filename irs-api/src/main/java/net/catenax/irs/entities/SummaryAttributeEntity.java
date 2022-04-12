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

import lombok.*;
import net.catenax.irs.component.AsyncFetchedItems;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * JPA entity part representing an attribute key-value pair attached to a part identifier.
 */
@Entity
@Table(name = "summary_attribute")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString // safe on this entity as it has no relationships
public class SummaryAttributeEntity implements Serializable {

    /**
     * The entity primary key, which itself contains the part identifier
     * and attribute name.
     */
    @EmbeddedId
    @NotNull
    @Valid
    private SummaryAttributeEntityKey key;

    /**
     * The attribute value.
     */
    @NotNull
    private AsyncFetchedItems asyncFetchedItems;

}
