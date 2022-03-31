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
 * Class for MeasurementUnit. Comprises the datatypeURI and the lexical value for the
 * respective quantity
 */
@Value
public class MeasurementUnit implements Serializable {

    @NotNull
    private final String datatypeURI;

    @NotNull
    private final String lexicalValue;

    @JsonCreator
    public MeasurementUnit(@JsonProperty("datatypeURI") final String datatypeURI,
            @JsonProperty("lexicalValue") final String lexicalValue) {
        this.datatypeURI = datatypeURI;
        this.lexicalValue = lexicalValue;
    }
}
