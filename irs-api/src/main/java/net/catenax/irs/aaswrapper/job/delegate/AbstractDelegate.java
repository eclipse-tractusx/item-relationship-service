//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job.delegate;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.dto.JobParameter;

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
