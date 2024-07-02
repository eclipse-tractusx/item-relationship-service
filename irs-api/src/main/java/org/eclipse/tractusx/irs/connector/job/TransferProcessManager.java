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
package org.eclipse.tractusx.irs.connector.job;

import java.util.function.Consumer;

import org.eclipse.tractusx.irs.component.JobParameter;

/**
 * Manages the processes to retrieve data by executing them asynchronously.
 *
 * @param <T> type of the DataRequest
 * @param <P> type of the TransferProcess
 */
public interface TransferProcessManager<T extends DataRequest, P extends TransferProcess> {

    String CANCELLATION_IMPOSSIBLE_FUTURE_NOT_FOUND = "Cancellation impossible for transfer process %s: Future not found";
    String CANCELLATION_FAILED = "Cancellation failed for transfer process %s";

    /**
     * Starts a data request asynchronously.
     *
     * @param dataRequest              the data request instruction
     * @param transferProcessStarted   callback which is executed as soon as a request is being started
     * @param transferProcessCompleted callback which is executed after the request is finished
     * @param jobData                  of the BomLifecycle from the RegisterJob request
     * @return the initialization response, indicating the acceptance status of the transfer
     */
    TransferInitiateResponse initiateRequest(T dataRequest, Consumer<String> transferProcessStarted,
            Consumer<P> transferProcessCompleted, JobParameter jobData);

    void cancelRequest(String processId);
}
