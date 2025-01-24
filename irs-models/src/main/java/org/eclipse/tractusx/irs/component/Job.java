/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.component;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable"})
public class Job {

    private static final int JOB_ID_FIELD_MAX_LENGTH = 36;
    private static final int GLOBAL_ASSET_ID_LENGTH = 45;
    private static final String EXAMPLE_DATE_TIME = "2022-02-03T14:48:54.709Z";

    @NotNull
    @Size(min = JOB_ID_FIELD_MAX_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Id of the job.", minLength = JOB_ID_FIELD_MAX_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = UUID.class,
            pattern = "/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i",
            example = "e5347c88-a921-11ec-b909-0242ac120002")
    @JsonAlias("jobId")
    private UUID id;

    @Schema(implementation = String.class, description = "Part global unique id in the format urn:uuid:uuid4.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
            minLength = GLOBAL_ASSET_ID_LENGTH, maxLength = GLOBAL_ASSET_ID_LENGTH, pattern = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonUnwrapped
    private GlobalAssetIdentification globalAssetId;

    @Schema(implementation = String.class, description = "Asset Administration Shell Id", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0")
    private String aasIndetifier;

    @NotBlank
    @JsonAlias("jobState")
    @Schema(implementation = JobState.class, example = "COMPLETED")
    private JobState state;

    @Schema(description = "Job error details.", implementation = JobErrorDetails.class)
    private JobErrorDetails exception;

    /**
     * Timestamp when the job was created
     */
    @Schema(implementation = ZonedDateTime.class, example = EXAMPLE_DATE_TIME)
    private ZonedDateTime createdOn;

    /**
     * Timestamp when the job was started
     */
    @Schema(implementation = ZonedDateTime.class, example = EXAMPLE_DATE_TIME)
    private ZonedDateTime startedOn;

    /**
     * Last time job was modified
     */
    @Schema(implementation = ZonedDateTime.class, example = EXAMPLE_DATE_TIME)
    private ZonedDateTime lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    @Schema(implementation = ZonedDateTime.class, example = EXAMPLE_DATE_TIME)
    @JsonAlias("jobCompleted")
    private ZonedDateTime completedOn;

    @Schema(description = "Summary of the job with statistics of the job processing.", implementation = Summary.class)
    private Summary summary;

    @Schema(description = "The passed job parameters", implementation = JobParameter.class)
    @JsonAlias("jobParameter")
    private JobParameter parameter;

}
