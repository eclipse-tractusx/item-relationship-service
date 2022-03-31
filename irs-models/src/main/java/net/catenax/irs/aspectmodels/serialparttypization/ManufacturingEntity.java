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

import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Generated class for Manufacturing Entity. Encapsulates the manufacturing relevant attributes
 */
@Value
public class ManufacturingEntity {

    @NotNull
    private final XMLGregorianCalendar date;

    @Pattern(regexp = "^[A-Z][A-Z][A-Z]$")
    private final Optional<String> country;

    @JsonCreator
    public ManufacturingEntity(@JsonProperty("date") final XMLGregorianCalendar date,
            @JsonProperty("country") final Optional<String> country) {
        this.date = date;
        this.country = country;
    }
}
