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

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Generated class for Key Value List. A list of key value pairs for local identifiers, which are
 * composed of a key and a corresponding value.
 */
@Value
public class KeyValueList {

    @NotNull
    private final String key;

    @NotNull
    private final String value;

    @JsonCreator
    public KeyValueList(@JsonProperty("key") final String key, @JsonProperty("value") final String value) {
        this.key = key;
        this.value = value;
    }
}
