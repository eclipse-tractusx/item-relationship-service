//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.requests;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import net.catenax.irs.annotations.ValueOfEnum;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * JobsByProcessingState request object
 */
@Value
@ExcludeFromCodeCoverageGeneratedReport
public class JobsByJobStateRequest implements Serializable {

    @ValueOfEnum(enumClass = JobState.class)
    @Parameter(description = "List of jobs (globalAssetIds) for a certain processing state.", in = QUERY, explode = Explode.FALSE,
            example = JobState.JobStateConstants.RUNNING, array = @ArraySchema(
            schema = @Schema(implementation = JobState.class,
                    defaultValue = JobState.JobStateConstants.RUNNING)))
    protected final List<JobState> processingStates;

}
