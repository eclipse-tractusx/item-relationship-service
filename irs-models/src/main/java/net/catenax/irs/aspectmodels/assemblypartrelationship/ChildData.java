/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated class for Child Data. Catena-X ID and meta data of the assembled child part.
 */
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
    public ChildData(@JsonProperty(value = "createdOn") XMLGregorianCalendar createdOn,
            @JsonProperty(value = "quantity") Quantity quantity,
            @JsonProperty(value = "lastModifiedOn") XMLGregorianCalendar lastModifiedOn,
            @JsonProperty(value = "lifecycleContext") LifecycleContextCharacteristic lifecycleContext,
            @JsonProperty(value = "assembledOn") XMLGregorianCalendar assembledOn,
            @JsonProperty(value = "childCatenaXId") String childCatenaXId) {
        this.createdOn = createdOn;
        this.quantity = quantity;
        this.lastModifiedOn = lastModifiedOn;
        this.lifecycleContext = lifecycleContext;
        this.assembledOn = assembledOn;
        this.childCatenaXId = childCatenaXId;
    }

    /**
     * Returns Created On
     *
     * @return {@link #createdOn}
     */
    public XMLGregorianCalendar getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Returns Quantity
     *
     * @return {@link #quantity}
     */
    public Quantity getQuantity() {
        return this.quantity;
    }

    /**
     * Returns Last Modification Date
     *
     * @return {@link #lastModifiedOn}
     */
    public XMLGregorianCalendar getLastModifiedOn() {
        return this.lastModifiedOn;
    }

    /**
     * Returns Lifecycle Context
     *
     * @return {@link #lifecycleContext}
     */
    public LifecycleContextCharacteristic getLifecycleContext() {
        return this.lifecycleContext;
    }

    /**
     * Returns Catena-X Child Identifier
     *
     * @return {@link #childCatenaXId}
     */
    public String getChildCatenaXId() {
        return this.childCatenaXId;
    }

    /**
     * Returns Assembly Date
     *
     * @return {@link #assembledOn}
     */
    public XMLGregorianCalendar getAssembledOn() {
        return assembledOn;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ChildData childData = (ChildData) o;
        return Objects.equals(lastModifiedOn, childData.lastModifiedOn)
                && lifecycleContext == childData.lifecycleContext && Objects.equals(createdOn, childData.createdOn)
                && Objects.equals(quantity, childData.quantity) && Objects.equals(assembledOn, childData.assembledOn)
                && Objects.equals(childCatenaXId, childData.childCatenaXId);
    }

    @Override
    public String toString() {
        return "ChildData{" + "lastModifiedOn=" + lastModifiedOn + ", lifecycleContext=" + lifecycleContext
                + ", createdOn=" + createdOn + ", quantity=" + quantity + ", assembledOn=" + assembledOn
                + ", childCatenaXId='" + childCatenaXId + '\'' + '}';
    }
}
