/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@Jacksonized
@SuppressWarnings("PMD.ShortClassName")
public class Job {

    private static final int INPUT_FIELD_MIN_LENGTH = 36;
    private static final int JOB_ID_FIELD_MAX_LENGTH = 36;

    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "JobId of the job.", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = UUID.class,
            pattern = "/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i")
    private UUID jobId;

    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Part global unique Id", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification globalAssetId;

    @NotBlank
    private JobState jobState;

    @Schema(description = "Job error details.", implementation = JobErrorDetails.class)
    private JobErrorDetails exception;

    /**
     * Timestamp when the job was created
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime createdOn;

    /**
     * Timestamp when the job was started
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime startedOn;

    /**
     * Last time job was modified
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    @Schema(implementation = ZonedDateTime.class)
    private ZonedDateTime jobCompleted;

    /**
     * Owner of the job
     */
    @Schema(description = "The IRS api consumer.")
    private String owner;

    @Schema(description = "Summary of the job with statistics of the job processing.", implementation = Summary.class)
    private Summary summary;

    @Schema(description = "The passed job parameters", implementation = JobParameter.class)
    private JobParameter jobParameter;

}
