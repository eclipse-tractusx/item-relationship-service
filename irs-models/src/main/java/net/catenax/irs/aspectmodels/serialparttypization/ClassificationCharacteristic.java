/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.openmanufacturing.sds.aspectmodel.java.exception.EnumAttributeNotFoundException;

/** Generated class {@link ClassificationCharacteristic}. */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ClassificationCharacteristic {
  PRODUCT("product"),
  RAW_MATERIAL("raw material"),
  SOFTWARE("software"),
  ASSEMBLY("assembly"),
  TOOL("tool"),
  COMPONENT("component");

  private String value;

  ClassificationCharacteristic(String value) {
    this.value = value;
  }

  @JsonCreator
  static ClassificationCharacteristic enumDeserializationConstructor(String value) {
    return fromValue(value)
        .orElseThrow(
            () ->
                new EnumAttributeNotFoundException(
                    "Tried to parse value \""
                        + value
                        + "\", but there is no enum field like that in ClassificationCharacteristic"));
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public static Optional<ClassificationCharacteristic> fromValue(String value) {
    return Arrays.stream(ClassificationCharacteristic.values())
        .filter(enumValue -> compareEnumValues(enumValue, value))
        .findAny();
  }

  private static boolean compareEnumValues(ClassificationCharacteristic enumValue, String value) {
    return enumValue.getValue().equals(value);
  }
}
