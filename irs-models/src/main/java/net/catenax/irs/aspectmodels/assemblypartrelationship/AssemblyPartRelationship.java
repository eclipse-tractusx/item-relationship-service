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
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.openmanufacturing.sds.aspectmodel.java.CollectionAspect;
import lombok.Value;
import net.catenax.irs.aspectmodels.AspectModel;

/**
 * Generated class for Assembly Part Relationship. The aspect provides the child parts (one
 * structural level down) which the given object assembles.
 */
@Value
public class AssemblyPartRelationship
        implements AspectModel, CollectionAspect<Set<ChildData>, ChildData>, Serializable {

    @NotNull
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private final String catenaXId;

    @NotNull
    private final Set<ChildData> childParts;

    @JsonCreator
    public AssemblyPartRelationship(@JsonProperty("catenaXId") final String catenaXId,
            @JsonProperty("childParts") final Set<ChildData> childParts) {
        this.catenaXId = catenaXId;
        this.childParts = childParts;
    }
}
