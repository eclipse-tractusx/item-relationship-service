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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Generated class for Part Type Information Entity. Encapsulation for data related to the part type
 */
@Value
public class PartTypeInformationEntity {

    @NotNull
    private final String manufacturerPartId;

    private final Optional<String> customerPartId;

    @NotNull
    private final String nameAtManufacturer;

    private final Optional<String> nameAtCustomer;

    @NotNull
    private final ClassificationCharacteristic classification;

    @JsonCreator
    public PartTypeInformationEntity(@JsonProperty("manufacturerPartId") final String manufacturerPartId,
            @JsonProperty("customerPartId") final Optional<String> customerPartId,
            @JsonProperty("nameAtManufacturer") final String nameAtManufacturer,
            @JsonProperty("nameAtCustomer") final Optional<String> nameAtCustomer,
            @JsonProperty("classification") final ClassificationCharacteristic classification) {
        this.manufacturerPartId = manufacturerPartId;
        this.customerPartId = customerPartId;
        this.nameAtManufacturer = nameAtManufacturer;
        this.nameAtCustomer = nameAtCustomer;
        this.classification = classification;
    }
}
