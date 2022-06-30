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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Summary
 */
@Value
@Builder(toBuilder = true)
public class Summary {

    /**
     * asyncFetchedItems
     */
    @Schema(description = "Summary of the fetched jobs", implementation = AsyncFetchedItems.class)
    private AsyncFetchedItems asyncFetchedItems;


}
