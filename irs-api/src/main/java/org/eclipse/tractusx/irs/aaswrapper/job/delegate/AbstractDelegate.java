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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.dto.JobParameter;

/**
 * Abstract base class to process Shells, Submodels, Bpns and store them inside {@link ItemContainer}
 */
@RequiredArgsConstructor
public abstract class AbstractDelegate {

    protected final AbstractDelegate nextStep;

    protected final int retryCount = RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts();

    /**
     * @param itemContainerBuilder Collecting data from delegates
     * @param jobData The job parameters used for filtering
     * @param aasTransferProcess The transfer process which will be filled with childIds
     *                           for further processing
     * @param itemId The id of the current item
     * @return The ItemContainer filled with Relationships, Shells, Submodels (if requested in jobData)
     *         and Tombstones (if requests fail).
     */
    public abstract ItemContainer process(ItemContainer.ItemContainerBuilder itemContainerBuilder, JobParameter jobData,
            AASTransferProcess aasTransferProcess, String itemId);

    /**
     * Delegates processing to next step if exists or returns filled {@link ItemContainer}
     *
     * @param itemContainerBuilder Collecting data from delegates
     * @param jobData The job parameters used for filtering
     * @param aasTransferProcess The transfer process which will be filled with childIds
     *                           for further processing
     * @param itemId The id of the current item
     * @return item container with filled data
     */
    protected ItemContainer next(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {
        if (this.nextStep != null) {
            return this.nextStep.process(itemContainerBuilder, jobData, aasTransferProcess, itemId);
        }

        return itemContainerBuilder.build();
    }

}
