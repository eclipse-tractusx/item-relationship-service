/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Generated class for Manufacturing Entity. Encapsulates the manufacturing relevant attributes */
public class ManufacturingEntity {

  @NotNull private XMLGregorianCalendar date;

  @Pattern(regexp = "^[A-Z][A-Z][A-Z]$")
  private Optional<String> country;

  @JsonCreator
  public ManufacturingEntity(
      @JsonProperty(value = "date") XMLGregorianCalendar date,
      @JsonProperty(value = "country") Optional<String> country) {
    this.date = date;
    this.country = country;
  }

  /**
   * Returns Production Date
   *
   * @return {@link #date}
   */
  public XMLGregorianCalendar getDate() {
    return this.date;
  }
  /**
   * Returns Country code
   *
   * @return {@link #country}
   */
  public Optional<String> getCountry() {
    return this.country;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ManufacturingEntity that = (ManufacturingEntity) o;
    return Objects.equals(date, that.date) && Objects.equals(country, that.country);
  }
}
