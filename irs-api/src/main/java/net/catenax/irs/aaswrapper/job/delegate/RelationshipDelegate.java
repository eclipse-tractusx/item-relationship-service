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

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.LinkedItem;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.exceptions.JsonParseException;
import org.springframework.web.client.RestClientException;

/**
 * Builds relationship array for AAShell from previous step.
 * To build relationships AssemblyPartRelationship submodels are being retrieved from EDC's components.
 */
@Slf4j
public class RelationshipDelegate extends AbstractDelegate {

    private final SubmodelFacade submodelFacade;

    public RelationshipDelegate(final AbstractDelegate nextStep,
            final SubmodelFacade submodelFacade) {
        super(nextStep);
        this.submodelFacade = submodelFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(
            shell -> shell.findAssemblyPartRelationshipEndpointAddresses().forEach(address -> {
                try {
                    final List<Relationship> relationships = submodelFacade.getRelationships(address, jobData);
                    final List<String> childIds = getChildIds(relationships);

                    log.info("Processing Relationships with {} children", childIds.size());

                    aasTransferProcess.addIdsToProcess(childIds);
                    itemContainerBuilder.relationships(relationships);
                } catch (RestClientException | IllegalArgumentException e) {
                    log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                            address);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                } catch (JsonParseException e) {
                    log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                }
            })
        );

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private List<String> getChildIds(final List<Relationship> relationships) {
        return relationships.stream()
                           .map(Relationship::getLinkedItem)
                           .map(LinkedItem::getChildCatenaXId)
                           .map(GlobalAssetIdentification::getGlobalAssetId)
                           .collect(Collectors.toList());
    }
}
