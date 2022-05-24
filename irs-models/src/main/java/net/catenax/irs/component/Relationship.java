//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Relationship
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
public class Relationship {

    @Schema(implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification catenaXId;

    private ChildItem childItem;

    private ChildItem parentItem;

}
