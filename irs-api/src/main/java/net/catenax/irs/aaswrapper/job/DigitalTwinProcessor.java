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

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.dto.JobParameter;
import org.springframework.web.client.RestClientException;

/**
 * Retrieves AAShell
 */
@Slf4j
public class DigitalTwinProcessor extends AbstractProcessor {

    private final DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    public DigitalTwinProcessor(final AbstractProcessor nextStep,
            final DigitalTwinRegistryFacade digitalTwinRegistryFacade) {
        super(nextStep);
        this.digitalTwinRegistryFacade = digitalTwinRegistryFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        try {
            final AssetAdministrationShellDescriptor aasShell = digitalTwinRegistryFacade.getAAShellDescriptor(itemId, jobData);

            itemContainerBuilder.shell(aasShell);
        } catch (RestClientException e) {
            log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
        }

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

}
