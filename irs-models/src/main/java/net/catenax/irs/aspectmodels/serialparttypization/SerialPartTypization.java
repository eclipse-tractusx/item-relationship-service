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

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openmanufacturing.sds.aspectmodel.java.CollectionAspect;
import lombok.Value;
import net.catenax.irs.aspectmodels.AspectModel;

/**
 * Generated class for Serial Part Typization. A serialized part is an instantiation of a (design-)
 * part, where the particular instantiation can be uniquely identified by means of a serial numbers
 * or a similar identifier (e.g. VAN) or a combination of multiple identifiers (e.g. combination of
 * manufacturer, date and number)
 */
@Value
public class SerialPartTypization implements AspectModel, CollectionAspect<Set<KeyValueList>, KeyValueList> {

    @NotNull
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private final String catenaXId;

    @NotNull
    private final Set<KeyValueList> localIdentifiers;

    @NotNull
    private final ManufacturingEntity manufacturingInformation;

    @NotNull
    private final PartTypeInformationEntity partTypeInformation;

    @JsonCreator
    public SerialPartTypization(@JsonProperty("catenaXId") final String catenaXId,
            @JsonProperty("localIdentifiers") final Set<KeyValueList> localIdentifiers,
            @JsonProperty("manufacturingInformation") final ManufacturingEntity manufacturingInformation,
            @JsonProperty("partTypeInformation") final PartTypeInformationEntity partTypeInformation) {
        this.catenaXId = catenaXId;
        this.localIdentifiers = localIdentifiers;
        this.manufacturingInformation = manufacturingInformation;
        this.partTypeInformation = partTypeInformation;
    }
}
