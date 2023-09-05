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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.batch;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.ProcessingState;

/**
 * Entity with details need to execute Batch Order.
 */
@Getter
@Builder(toBuilder = true)
@Slf4j
@Jacksonized
public class BatchOrder {

    /**
     * Batch Order Id
     */
    private UUID batchOrderId;

    /**
     * Processing State of Batch Order
     */
    @Setter
    private ProcessingState batchOrderState;

    /**
     * Bom Lifecycle requested in order
     */
    private BomLifecycle bomLifecycle;

    /**
     * List of Aspects requested in order
     */
    private List<String> aspects;

    /**
     * Depth requested in order
     */
    private Integer depth;

    /**
     * Direction requested in order
     */
    private Direction direction;

    /**
     * Needs of collect aspects
     */
    private Boolean collectAspects;

    /**
     * Flag to specify whether BPNs should be collected and resolved via the configured BPDM URL
     */
    private Boolean lookupBPNs;

    /**
     * Timeout for Batch Order
     */
    private Integer timeout;

    /**
     * Timeout for Job in Batch
     */
    private Integer jobTimeout;

    /**
     * Callback Url to send results
     */
    private String callbackUrl;

    /**
     * Owner
     */
    private String owner;

}
