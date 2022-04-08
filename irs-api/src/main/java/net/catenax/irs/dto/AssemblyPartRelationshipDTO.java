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

/**
 * AssemblyPartRelationshipDTO model used for internal application use
 */
@Data
@Builder
public class AssemblyPartRelationshipDTO {
    /**
     * catenaXId
     */
    private String catenaXId;

    /**
     * childParts
     */
    private Set<ChildDataDTO> childParts;
}
