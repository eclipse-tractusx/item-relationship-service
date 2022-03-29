/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openmanufacturing.sds.aspectmodel.java.CollectionAspect;
import net.catenax.irs.aspectmodels.AspectModel;

/**
 * Generated class for Serial Part Typization. A serialized part is an instantiation of a (design-)
 * part, where the particular instantiation can be uniquely identified by means of a serial numbers
 * or a similar identifier (e.g. VAN) or a combination of multiple identifiers (e.g. combination of
 * manufacturer, date and number)
 */
public class SerialPartTypization extends AspectModel implements CollectionAspect<Set<KeyValueList>, KeyValueList> {

  @NotNull
  @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
  private String catenaXId;

  @NotNull private Set<KeyValueList> localIdentifiers;

  @NotNull private ManufacturingEntity manufacturingInformation;

  @NotNull private PartTypeInformationEntity partTypeInformation;

  @JsonCreator
  public SerialPartTypization(
      @JsonProperty(value = "catenaXId") String catenaXId,
      @JsonProperty(value = "localIdentifiers") Set<KeyValueList> localIdentifiers,
      @JsonProperty(value = "manufacturingInformation")
          ManufacturingEntity manufacturingInformation,
      @JsonProperty(value = "partTypeInformation") PartTypeInformationEntity partTypeInformation) {
    this.catenaXId = catenaXId;
    this.localIdentifiers = localIdentifiers;
    this.manufacturingInformation = manufacturingInformation;
    this.partTypeInformation = partTypeInformation;
  }

  /**
   * Returns Catena-X Identifier
   *
   * @return {@link #catenaXId}
   */
  public String getCatenaXId() {
    return this.catenaXId;
  }
  /**
   * Returns Local Identifiers
   *
   * @return {@link #localIdentifiers}
   */
  public Set<KeyValueList> getLocalIdentifiers() {
    return this.localIdentifiers;
  }
  /**
   * Returns Manufacturing Information
   *
   * @return {@link #manufacturingInformation}
   */
  public ManufacturingEntity getManufacturingInformation() {
    return this.manufacturingInformation;
  }
  /**
   * Returns Part Type Information
   *
   * @return {@link #partTypeInformation}
   */
  public PartTypeInformationEntity getPartTypeInformation() {
    return this.partTypeInformation;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SerialPartTypization that = (SerialPartTypization) o;
    return Objects.equals(catenaXId, that.catenaXId)
        && Objects.equals(localIdentifiers, that.localIdentifiers)
        && Objects.equals(manufacturingInformation, that.manufacturingInformation)
        && Objects.equals(partTypeInformation, that.partTypeInformation);
  }
}
