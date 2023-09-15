/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.connector.batch;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;

/**
 * Entity for execute batch of jobs and monitor processing state.
 */
@Getter
@Builder(toBuilder = true)
@Slf4j
@Jacksonized
public class Batch {

    /**
     * Batch Id
     */
    private UUID batchId;

    /**
     * Batch Order Id
     */
    private UUID batchOrderId;

    /**
     * Processing State of Batch
     */
    @Setter
    private ProcessingState batchState;

    /**
     * Batch Number in Batch Order
     */
    private Integer batchNumber;

    /**
     * Total number of batches
     */
    private Integer batchTotal;

    /**
     * Batch Url
     */
    private String batchUrl;

    /**
     * List of Job Progress with details about job
     */
    @Setter
    private List<JobProgress> jobProgressList;

    /**
     * Timestamp when the Batch was started
     */
    @Setter
    private ZonedDateTime startedOn;

    /**
     * Timestamp when the Batch was started
     */
    @Setter
    private ZonedDateTime completedOn;

    /**
     * Owner
     */
    private String owner;

}
