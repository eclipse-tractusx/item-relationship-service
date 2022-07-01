//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//
package net.catenax.irs.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * AssemblyPartRelationshipDTO model used for internal application use
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class AssemblyPartRelationshipDTO {

    private String catenaXId;

    private Set<ChildDataDTO> childParts;

}
