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
package org.eclipse.tractusx.irs.connector.job;

import java.util.stream.Stream;

import org.eclipse.tractusx.irs.component.enums.IntegrityState;

/**
 * Interface for extensions to provide the logic to build jobs with
 * custom logic to run multiple transfers.
 */
public interface RecursiveJobHandler<T extends DataRequest, P extends TransferProcess> {
    /**
     * Start the recursive process by creating any number of transfers.
     *
     * @param job job definition.
     * @return a stream of {@DataRequest}. One data transfer will be initiated for each item.
     */
    Stream<T> initiate(MultiTransferJob job);

    /**
     * Continue the recursive process by creating any number of transfers from
     * the result of a completed transfer.
     *
     * @param job             job definition.
     * @param transferProcess completed transfer.
     * @return a stream of {@DataRequest}. One data transfer will be initiated for each item.
     */
    Stream<T> recurse(MultiTransferJob job, P transferProcess);

    /**
     * Called when all transfers in the job have completed.
     * @param job job definition.
     * @return integrity chain state
     */
    IntegrityState complete(MultiTransferJob job);
}
