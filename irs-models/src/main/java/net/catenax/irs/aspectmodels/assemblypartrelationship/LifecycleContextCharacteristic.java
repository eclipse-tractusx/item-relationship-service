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

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.openmanufacturing.sds.aspectmodel.java.exception.EnumAttributeNotFoundException;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Generated class {@link LifecycleContextCharacteristic}.
 */
@ExcludeFromCodeCoverageGeneratedReport
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LifecycleContextCharacteristic {
    ASREQUIRED("AsRequired"),
    ASDESIGNED("AsDesigned"),
    ASPLANNED("AsPlanned"),
    ASBUILT("AsBuilt"),
    ASMAINTAINED("AsMaintained"),
    ASRECYCLED("AsRecycled");

    private final String value;

    LifecycleContextCharacteristic(final String value) {
        this.value = value;
    }

    @JsonCreator
    static LifecycleContextCharacteristic enumDeserializationConstructor(final String value) {
        return fromValue(value).orElseThrow(() -> new EnumAttributeNotFoundException("Tried to parse value \"" + value
                + "\", but there is no enum field like that in LifecycleContextCharacteristic"));
    }

    public static Optional<LifecycleContextCharacteristic> fromValue(final String value) {
        return Arrays.stream(LifecycleContextCharacteristic.values())
                     .filter(enumValue -> compareEnumValues(enumValue, value))
                     .findAny();
    }

    private static boolean compareEnumValues(final LifecycleContextCharacteristic enumValue, final String value) {
        return enumValue.getValue().equals(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
