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

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Generated class for Quantity. Comprises the number of objects and the unit of measurement for the
 * respective child objects
 */
@Value
public class Quantity implements Serializable {

    @NotNull
    private final Double quantityNumber;

    @NotNull
    private final MeasurementUnit measurementUnit;

    @JsonCreator
    public Quantity(@JsonProperty("quantityNumber") final Double quantityNumber,
            @JsonProperty("measurementUnit") final MeasurementUnit measurementUnit) {
        this.quantityNumber = quantityNumber;
        this.measurementUnit = measurementUnit;
    }
}
