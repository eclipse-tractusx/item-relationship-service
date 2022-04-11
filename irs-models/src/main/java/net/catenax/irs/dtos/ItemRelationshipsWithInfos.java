//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dtos;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * API type for query response type with relationships and part information.
 */
@Schema(description = "List of relationships with information about parts.")
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = ItemRelationshipsWithInfos.ItemRelationshipsWithInfosBuilder.class)
public class ItemRelationshipsWithInfos {
    @Schema(description = "List of the relationships")
    private List<IrsPartRelationship> relationships;

    @Schema(description = "List of part infos")
    private List<PartInfo> partInfos;
}
