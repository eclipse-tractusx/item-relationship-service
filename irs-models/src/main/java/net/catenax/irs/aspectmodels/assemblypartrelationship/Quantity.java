/*
 * Copyright (c) 2022 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */
package net.catenax.irs.aspectmodels.assemblypartrelationship;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated class for Quantity. Comprises the number of objects and the unit of measurement for the
 * respective child objects
 */
public class Quantity implements Serializable {

    @NotNull
    private final Double quantityNumber;

    @NotNull
    private final MeasurementUnit measurementUnit;

    @JsonCreator
    public Quantity(@JsonProperty(value = "quantityNumber") Double quantityNumber,
            @JsonProperty(value = "measurementUnit") MeasurementUnit measurementUnit) {
        this.quantityNumber = quantityNumber;
        this.measurementUnit = measurementUnit;
    }

    /**
     * Returns Quantity Number
     *
     * @return {@link #quantityNumber}
     */
    public Double getQuantityNumber() {
        return this.quantityNumber;
    }

    /**
     * Returns Measurement Unit
     *
     * @return {@link #measurementUnit}
     */
    public MeasurementUnit getMeasurementUnit() {
        return this.measurementUnit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Quantity that = (Quantity) o;
        return Objects.equals(quantityNumber, that.quantityNumber) && Objects.equals(measurementUnit,
                that.measurementUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantityNumber, measurementUnit);
    }

    @Override
    public String toString() {
        return "Quantity{" + "quantityNumber=" + quantityNumber + ", measurementUnit=" + measurementUnit + '}';
    }
}
