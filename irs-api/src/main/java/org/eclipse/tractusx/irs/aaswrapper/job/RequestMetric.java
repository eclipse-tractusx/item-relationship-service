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
package org.eclipse.tractusx.irs.aaswrapper.job;

import lombok.Getter;
import lombok.ToString;

/**
 * Metrics to track requests to external services.
 */
@Getter
@ToString
public class RequestMetric {
    private Integer running;
    private Integer completed;
    private Integer failed;
    private RequestType type;

    public RequestMetric() {
        this.running = 0;
        this.completed = 0;
        this.failed = 0;
    }

    public void setType(final RequestType type) {
        this.type = type;
    }

    /**
     * Increment running count by 1.
     */
    public void incrementRunning() {
        this.running += 1;
    }

    /**
     * Increment completed count by 1.
     */
    public void incrementCompleted() {
        this.completed += 1;
    }

    /**
     * Increment failed count by 1.
     */
    public void incrementFailed() {
        this.failed += 1;
    }

    /**
     * Type of tracked request. Can be used to filter for the different metrics.
     */
    public enum RequestType {
        DITIGAL_TWIN,
        RELATIONSHIP,
        SUBMODEL
    }
}
