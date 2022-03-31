//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;

/**
 * TestData Class used to store data provided by JSON
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestData {
    @JsonProperty("catenaXId")
    private final String catenaXId;
    @JsonProperty("AssemblyPartRelationship")
    private final AssemblyPartRelationship assemblyPartRelationship;

    @JsonCreator
    public TestData(@JsonProperty("catenaXId") final String catenaXId,
            @JsonProperty("AssemblyPartRelationship") final List<AssemblyPartRelationship> assemblyPartRelationship) {
        this.catenaXId = catenaXId;
        final AssemblyPartRelationship emptyAssemblyPartRelationship = new AssemblyPartRelationship(catenaXId,
                Set.of());
        if (assemblyPartRelationship == null) {
            this.assemblyPartRelationship = emptyAssemblyPartRelationship;
        } else {
            this.assemblyPartRelationship = assemblyPartRelationship.stream()
                                                                    .findFirst()
                                                                    .orElse(emptyAssemblyPartRelationship);
        }
    }
}
