//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.enums;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.dtos.ItemLifecycleStage;

/***
 * API type for the view of the items tree to be returned by a query.
 *
 * @see ItemLifecycleStage
 */
@ExcludeFromCodeCoverageGeneratedReport
@Schema(description = "The lifecycle context in which the child part was assembled into the parent part.")
@Getter
public enum BomLifecycle {
    @Schema(description = "The view of the ItemsTree as the vehicle was assembled.") AS_BUILT("asBuilt", "AsBuilt");
    //@Schema(description = "The view of the PartsTree ... lifecycle.") AS_MAINTAINED("asMaintained"),
    //@Schema(description = "TThe view of the PartsTree ... lifecycle.") AS_PLANNED("asPlanned"),
    //@Schema(description = "TThe view of the PartsTree ... lifecycle.") AS_DESIGNED("asDesigned"),
    //@Schema(description = "The view of the PartsTree ... lifecycle.") AS_ORDERED("asOrdered"),
    //@Schema(description = "The view of the PartsTree ... lifecycle.") AS_RECYCLED("asRecycled");

    private final String value;
    private final String lifecycleContextCharacteristicValue;

    BomLifecycle(final String value, final String lifecycleContextCharacteristicValue) {
        this.value = value;
        this.lifecycleContextCharacteristicValue = lifecycleContextCharacteristicValue;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #value}
     * @return the corresponding BomLifecycle
     */
    public static BomLifecycle value(final String value) {
        return BomLifecycle.valueOf(value);
    }

    @JsonCreator
    public static BomLifecycle fromValue(final String value) {
        return Stream.of(BomLifecycle.values())
                     .filter(bomLifecycle -> bomLifecycle.value.equals(value))
                     .findFirst()
                     .orElseThrow(() -> new NoSuchElementException("Unsupported BomLifecycle: " + value
                             + ". Must be one of: " + supportedBomLifecycles()));
    }

    private static String supportedBomLifecycles() {
        return Stream.of(BomLifecycle.values()).map(bomLifecycle -> bomLifecycle.value).collect(Collectors.joining(", "));
    }

    public static BomLifecycle fromLifecycleContextCharacteristic(final String value) {
        return Stream.of(BomLifecycle.values())
                     .filter(bomLifecycle -> bomLifecycle.lifecycleContextCharacteristicValue.equals(value))
                     .findFirst()
                     .orElseThrow();
    }

    /**
     * @return convert BomLifecycle to string value
     */
    @Override
    public String toString() {
        return value;
    }
}
