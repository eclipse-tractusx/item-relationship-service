//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aspectmodels.serialparttypization;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.openmanufacturing.sds.aspectmodel.java.exception.EnumAttributeNotFoundException;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Generated class {@link ClassificationCharacteristic}.
 */
@ExcludeFromCodeCoverageGeneratedReport
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ClassificationCharacteristic {
    PRODUCT("product"),
    RAW_MATERIAL("raw material"),
    SOFTWARE("software"),
    ASSEMBLY("assembly"),
    TOOL("tool"),
    COMPONENT("component");

    private final String value;

    ClassificationCharacteristic(final String value) {
        this.value = value;
    }

    @JsonCreator
    static ClassificationCharacteristic enumDeserializationConstructor(final String value) {
        return fromValue(value).orElseThrow(() -> new EnumAttributeNotFoundException("Tried to parse value \"" + value
                + "\", but there is no enum field like that in ClassificationCharacteristic"));
    }

    public static Optional<ClassificationCharacteristic> fromValue(final String value) {
        return Arrays.stream(ClassificationCharacteristic.values())
                     .filter(enumValue -> compareEnumValues(enumValue, value))
                     .findAny();
    }

    private static boolean compareEnumValues(final ClassificationCharacteristic enumValue, final String value) {
        return enumValue.getValue().equals(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
