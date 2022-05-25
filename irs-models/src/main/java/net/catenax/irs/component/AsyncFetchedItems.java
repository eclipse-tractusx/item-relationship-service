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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Describe the state of the fetched items
 */
@Schema(description = "Statistics of job execution.")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = AsyncFetchedItems.AsyncFetchedItemsBuilder.class)
public class AsyncFetchedItems {

    @Schema(description = "Number of item transfers remaining.", implementation = Integer.class)
    private Integer queue;

    @Schema(description = "Number of running item transfers.", implementation = Integer.class)
    private Integer running;

    @Schema(description = "Number of completed item transfers.", implementation = Integer.class)
    private Integer complete;

    @Schema(description = "Number of failed item transfers.", implementation = Integer.class)
    private Integer failed;

    /**
     * User to build async fetched items
     */
    @Schema(description = "User to build async fetched items")
    @JsonPOJOBuilder(withPrefix = "")
    public static class AsyncFetchedItemsBuilder {
    }
}
