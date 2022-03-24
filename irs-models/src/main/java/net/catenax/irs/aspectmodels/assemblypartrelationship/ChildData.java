/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Generated class for Child Data. Catena-X ID and meta data of the assembled child part. */
public class ChildData {

  @NotNull private XMLGregorianCalendar createdOn;

  @NotNull private Quantity quantity;
  private Optional<XMLGregorianCalendar> lastModifiedOn;

  @NotNull private LifecycleContextCharacteristic lifecycleContext;

  @NotNull
  @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
  private String childCatenaXId;

  @JsonCreator
  public ChildData(
      @JsonProperty(value = "createdOn") XMLGregorianCalendar createdOn,
      @JsonProperty(value = "quantity") Quantity quantity,
      @JsonProperty(value = "lastModifiedOn") Optional<XMLGregorianCalendar> lastModifiedOn,
      @JsonProperty(value = "lifecycleContext") LifecycleContextCharacteristic lifecycleContext,
      @JsonProperty(value = "childCatenaXId") String childCatenaXId) {
    this.createdOn = createdOn;
    this.quantity = quantity;
    this.lastModifiedOn = lastModifiedOn;
    this.lifecycleContext = lifecycleContext;
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
  public Optional<XMLGregorianCalendar> getLastModifiedOn() {
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ChildData that = (ChildData) o;
    return Objects.equals(createdOn, that.createdOn)
        && Objects.equals(quantity, that.quantity)
        && Objects.equals(lastModifiedOn, that.lastModifiedOn)
        && Objects.equals(lifecycleContext, that.lifecycleContext)
        && Objects.equals(childCatenaXId, that.childCatenaXId);
  }

  @Override
  public String toString() {
    return "ChildData{" + "createdOn=" + createdOn + ", quantity=" + quantity + ", lastModifiedOn=" + lastModifiedOn
            + ", lifecycleContext=" + lifecycleContext + ", childCatenaXId='" + childCatenaXId + '\'' + '}';
  }
}
