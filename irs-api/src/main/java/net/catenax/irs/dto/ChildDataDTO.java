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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

/**
 * ChildDataDTO model used for internal application use
 */
@Data
@Builder(toBuilder = true)
@JsonDeserialize(builder = ChildDataDTO.ChildDataDTOBuilder.class)
public class ChildDataDTO {
    /**
     * lifecycleContext
     */
    private String lifecycleContext;

    /**
     * childCatenaXId
     */
    private String childCatenaXId;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class ChildDataDTOBuilder {

    }
}
