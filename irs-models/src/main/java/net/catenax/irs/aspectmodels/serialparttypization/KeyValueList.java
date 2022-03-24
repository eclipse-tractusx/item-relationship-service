/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated class for Key Value List. A list of key value pairs for local identifiers, which are
 * composed of a key and a corresponding value.
 */
public class KeyValueList {

  @NotNull private String key;

  @NotNull private String value;

  @JsonCreator
  public KeyValueList(
      @JsonProperty(value = "key") String key, @JsonProperty(value = "value") String value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Returns Identifier Key
   *
   * @return {@link #key}
   */
  public String getKey() {
    return this.key;
  }
  /**
   * Returns Identifier Value
   *
   * @return {@link #value}
   */
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final KeyValueList that = (KeyValueList) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }
}
