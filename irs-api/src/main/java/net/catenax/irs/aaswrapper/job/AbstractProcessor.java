//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.dto.JobParameter;

/**
 *
 */
@RequiredArgsConstructor
public abstract class AbstractProcessor {

    protected final AbstractProcessor nextStep;

    protected final int retryCount = RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts();

    public abstract ItemContainer process(ItemContainer.ItemContainerBuilder itemContainerBuilder, JobParameter jobData,
            AASTransferProcess aasTransferProcess, String itemId);

    protected ItemContainer next(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {
        if (this.nextStep != null) {
            return this.nextStep.process(itemContainerBuilder, jobData, aasTransferProcess, itemId);
        }

        return itemContainerBuilder.build();
    }

}
