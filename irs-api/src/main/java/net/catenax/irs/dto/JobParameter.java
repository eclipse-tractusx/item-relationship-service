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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

/**
 * JobParameter model used for job creation
 */
@Data
@Builder(toBuilder = true)
@JsonDeserialize(builder = JobParameter.JobParameterBuilder.class)
public class JobParameter {

    /**
     * Job Data key for root item ID
     */
    private String rootItemId;

    /**
     * Expected depth of the tree
     */
    private int treeDepth;

    /**
     * Specified aspect types by the consumer
     */
    private List<String> aspectTypes;

    /**
     * Given lifecycleContext from the consumer
     */
    private String bomLifecycle;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class JobParameterBuilder {
    }

}
