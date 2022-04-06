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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Information about request items tree job
 */
@ApiModel(description = "Information about request items tree job")
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = IrsPartRelationshipsWithInfos.IrsPartRelationshipsWithInfosBuilder.class)
public class IrsPartRelationshipsWithInfos {

    /**
     * job
     */
    @Schema(description = "", implementation = Job.class)
    Jobs job;

    /**
     * relationships
     */
    List<Relationship> relationships;

    /**
     * shells
     */
    List<Shells> shells;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class IrsPartRelationshipsWithInfosBuilder {
    }

}
