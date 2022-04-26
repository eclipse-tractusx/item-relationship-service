//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.aaswrapper.registry.domain.AasTombstone;
import net.catenax.irs.aaswrapper.submodel.domain.ItemRelationshipAspectTombstone;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;

/**
 * Container class to store item data
 */
@Getter
@Builder
@Jacksonized
public class ItemContainer {

    @Singular
    private List<AssemblyPartRelationshipDTO> assemblyPartRelationships;

    @Singular
    private List<ItemRelationshipAspectTombstone> itemRelationshipAspectTombstones;

    @Singular
    private List<AasTombstone> aasShellTombstones;
}
