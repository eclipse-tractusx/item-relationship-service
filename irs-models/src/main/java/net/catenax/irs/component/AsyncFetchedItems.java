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
import lombok.extern.jackson.Jacksonized;

/**
 * Describe the state of the fetched items
 */
@Schema(description = "Statistics of job execution.")
@Value
@Builder(toBuilder = true)
@Jacksonized
public class AsyncFetchedItems {

    @Schema(description = "Number of running item transfers.", implementation = Integer.class)
    private Integer running;

    @Schema(description = "Number of completed item transfers.", implementation = Integer.class)
    private Integer completed;

    @Schema(description = "Number of failed item transfers.", implementation = Integer.class)
    private Integer failed;

}
