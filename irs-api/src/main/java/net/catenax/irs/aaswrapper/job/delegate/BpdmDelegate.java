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

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.bpdm.BpdmFacade;
import net.catenax.irs.component.Bpn;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.dto.JobParameter;
import org.springframework.web.client.RestClientException;

/**
 * Builds bpns array for AAShell from previous step.
 * To build bpns Business Partner service is called.
 */
@Slf4j
public class BpdmDelegate extends AbstractDelegate {

    private final BpdmFacade bpdmFacade;

    public BpdmDelegate(final BpdmFacade bpdmFacade) {
        super(null); // no next step
        this.bpdmFacade = bpdmFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        try {
            itemContainerBuilder.build()
                .getShells()
                .stream()
                .findFirst()
                .flatMap(AssetAdministrationShellDescriptor::findManufacturerId)
                .ifPresent(manufacturerId -> {
                    final Optional<String> manufacturerName = bpdmFacade.findManufacturerName(manufacturerId);
                    manufacturerName.ifPresent(name -> itemContainerBuilder.bpn(Bpn.of(manufacturerId, name)));
                });
        } catch (RestClientException e) {
            log.info("Business Partner endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
        }

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

}
