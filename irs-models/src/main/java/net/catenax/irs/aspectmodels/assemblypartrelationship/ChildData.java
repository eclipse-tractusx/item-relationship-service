//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Generated class for Child Data. Catena-X ID and meta data of the assembled child part.
 */
@Value
@EqualsAndHashCode
public class ChildData implements Serializable {

    private final XMLGregorianCalendar lastModifiedOn;
    @NotNull
    private final LifecycleContextCharacteristic lifecycleContext;
    @NotNull
    private final XMLGregorianCalendar createdOn;
    @NotNull
    private final Quantity quantity;
    @NotNull
    private final XMLGregorianCalendar assembledOn;

    @NotNull
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private final String childCatenaXId;

    @JsonCreator
    public ChildData(@JsonProperty("createdOn") final XMLGregorianCalendar createdOn,
            @JsonProperty("quantity") final Quantity quantity,
            @JsonProperty("lastModifiedOn") final XMLGregorianCalendar lastModifiedOn,
            @JsonProperty("lifecycleContext") final LifecycleContextCharacteristic lifecycleContext,
            @JsonProperty("assembledOn") final XMLGregorianCalendar assembledOn,
            @JsonProperty("childCatenaXId") final String childCatenaXId) {
        this.createdOn = createdOn;
        this.quantity = quantity;
        this.lastModifiedOn = lastModifiedOn;
        this.lifecycleContext = lifecycleContext;
        this.assembledOn = assembledOn;
        this.childCatenaXId = childCatenaXId;
    }
}
