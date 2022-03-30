/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.openmanufacturing.sds.aspectmodel.java.exception.EnumAttributeNotFoundException;

/**
 * Generated class {@link LifecycleContextCharacteristic}.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LifecycleContextCharacteristic {
    ASREQUIRED("AsRequired"),
    ASDESIGNED("AsDesigned"),
    ASPLANNED("AsPlanned"),
    ASBUILT("AsBuilt"),
    ASMAINTAINED("AsMaintained"),
    ASRECYCLED("AsRecycled");

    private final String value;

    LifecycleContextCharacteristic(String value) {
        this.value = value;
    }

    @JsonCreator
    static LifecycleContextCharacteristic enumDeserializationConstructor(String value) {
        return fromValue(value).orElseThrow(() -> new EnumAttributeNotFoundException("Tried to parse value \"" + value
                + "\", but there is no enum field like that in LifecycleContextCharacteristic"));
    }

    public static Optional<LifecycleContextCharacteristic> fromValue(String value) {
        return Arrays.stream(LifecycleContextCharacteristic.values())
                     .filter(enumValue -> compareEnumValues(enumValue, value))
                     .findAny();
    }

    private static boolean compareEnumValues(LifecycleContextCharacteristic enumValue, String value) {
        return enumValue.getValue().equals(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
