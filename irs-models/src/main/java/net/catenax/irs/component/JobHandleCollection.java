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

import java.util.Collection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * contains a collection of JobHandle
 */
@ApiModel(description = "Collection of job handle")
@Value
@Builder
@JsonDeserialize(builder = JobHandleCollection.JobHandleCollectionBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class JobHandleCollection {

    private Collection<JobHandle> jobs;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "with")
    public static class JobHandleCollectionBuilder {
    }
}
