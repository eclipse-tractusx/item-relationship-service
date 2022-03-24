/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated class for Part Type Information Entity. Encapsulation for data related to the part type
 */
public class PartTypeInformationEntity {

  @NotNull private String manufacturerPartId;
  private Optional<String> customerPartId;

  @NotNull private String nameAtManufacturer;
  private Optional<String> nameAtCustomer;

  @NotNull private ClassificationCharacteristic classification;

  @JsonCreator
  public PartTypeInformationEntity(
      @JsonProperty(value = "manufacturerPartId") String manufacturerPartId,
      @JsonProperty(value = "customerPartId") Optional<String> customerPartId,
      @JsonProperty(value = "nameAtManufacturer") String nameAtManufacturer,
      @JsonProperty(value = "nameAtCustomer") Optional<String> nameAtCustomer,
      @JsonProperty(value = "classification") ClassificationCharacteristic classification) {
    this.manufacturerPartId = manufacturerPartId;
    this.customerPartId = customerPartId;
    this.nameAtManufacturer = nameAtManufacturer;
    this.nameAtCustomer = nameAtCustomer;
    this.classification = classification;
  }

  /**
   * Returns Manufacturer Part ID
   *
   * @return {@link #manufacturerPartId}
   */
  public String getManufacturerPartId() {
    return this.manufacturerPartId;
  }
  /**
   * Returns Customer Part ID
   *
   * @return {@link #customerPartId}
   */
  public Optional<String> getCustomerPartId() {
    return this.customerPartId;
  }
  /**
   * Returns Name at Manufacturer
   *
   * @return {@link #nameAtManufacturer}
   */
  public String getNameAtManufacturer() {
    return this.nameAtManufacturer;
  }
  /**
   * Returns Name at Customer
   *
   * @return {@link #nameAtCustomer}
   */
  public Optional<String> getNameAtCustomer() {
    return this.nameAtCustomer;
  }
  /**
   * Returns Classifcation
   *
   * @return {@link #classification}
   */
  public ClassificationCharacteristic getClassification() {
    return this.classification;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PartTypeInformationEntity that = (PartTypeInformationEntity) o;
    return Objects.equals(manufacturerPartId, that.manufacturerPartId)
        && Objects.equals(customerPartId, that.customerPartId)
        && Objects.equals(nameAtManufacturer, that.nameAtManufacturer)
        && Objects.equals(nameAtCustomer, that.nameAtCustomer)
        && Objects.equals(classification, that.classification);
  }
}
