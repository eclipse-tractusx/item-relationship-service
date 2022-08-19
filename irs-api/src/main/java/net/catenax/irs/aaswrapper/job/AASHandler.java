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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.bpdm.BpdmFacade;
import net.catenax.irs.component.Bpn;
import net.catenax.irs.component.Submodel;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.exceptions.JsonParseException;
import net.catenax.irs.semanticshub.SemanticsHubFacade;
import net.catenax.irs.services.validation.InvalidSchemaException;
import net.catenax.irs.services.validation.JsonValidatorService;
import net.catenax.irs.services.validation.ValidationResult;
import net.catenax.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Class to process Shells and Submodels and fill a ItemContainer with Relationships, Shells,
 * Tombstones and Submodels.
 */
@Slf4j
@RequiredArgsConstructor
public class AASHandler {

    private final DigitalTwinRegistryFacade registryFacade;
    private final SubmodelFacade submodelFacade;
    private final SemanticsHubFacade semanticsHubFacade;
    private final BpdmFacade bpdmFacade;
    private final JsonValidatorService jsonValidatorService;
    private final JsonUtil jsonUtil;

    /**
     * @param jobData            The job parameters used for filtering
     * @param aasTransferProcess The transfer process which will be filled with childIds for
     *                           further processing
     * @param itemId             The id of the current item
     * @return The ItemContainer filled with Relationships, Shells, Submodels (if requested in jobData)
     * and Tombstones (if requests fail).
     */
    public ItemContainer collectShellAndSubmodels(final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        final ItemContainer.ItemContainerBuilder itemContainerBuilder = ItemContainer.builder();
        final int retryCount = RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts();

        try {
            final AssetAdministrationShellDescriptor aasShell = registryFacade.getAAShellDescriptor(itemId, jobData);
            final List<SubmodelDescriptor> aasSubmodelDescriptors = aasShell.getSubmodelDescriptors();

            log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

            aasShell.findAssemblyPartRelationshipEndpointAddresses().forEach(address -> {
                try {
                    final AssemblyPartRelationshipDTO submodel = submodelFacade.getAssemblyPartRelationshipSubmodel(
                            address, jobData);
                    processEndpoint(aasTransferProcess, itemContainerBuilder, submodel);
                } catch (RestClientException | IllegalArgumentException e) {
                    log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.", address);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                } catch (JsonParseException e) {
                    log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                }
            });
            final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = aasShell.filterDescriptorsByAspectTypes(
                    jobData.getAspectTypes());

            if (jobData.isCollectAspects()) {
                log.info("Collecting Submodels.");
                collectSubmodels(filteredSubmodelDescriptorsByAspectType, itemContainerBuilder, itemId);
            }
            log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
            log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

            itemContainerBuilder.shell(
                    aasShell.toBuilder().submodelDescriptors(filteredSubmodelDescriptorsByAspectType).build());

            aasShell.findManufacturerId().ifPresent(manufacturerId -> {
                final Optional<String> manufacturerName = bpdmFacade.findManufacturerName(manufacturerId);
                manufacturerName.ifPresent(name -> itemContainerBuilder.bpn(Bpn.of(manufacturerId, name)));
            });

        } catch (RestClientException e) {
            log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
        }
        return itemContainerBuilder.build();
    }

    private void collectSubmodels(final List<SubmodelDescriptor> submodelDescriptors,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        submodelDescriptors.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                getSubmodels(submodelDescriptor, itemContainerBuilder, itemId)));
    }

    private List<Submodel> getSubmodels(final SubmodelDescriptor submodelDescriptor,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        final List<Submodel> submodels = new ArrayList<>();
        submodelDescriptor.getEndpoints().forEach(endpoint -> {
            try {
                final String jsonSchema = semanticsHubFacade.getModelJsonSchema(submodelDescriptor.getAspectType());
                final String submodelRawPayload = requestSubmodelAsString(endpoint);

                final ValidationResult validationResult = jsonValidatorService.validate(jsonSchema, submodelRawPayload);

                if (validationResult.isValid()) {
                    final Submodel submodel = Submodel.from(submodelDescriptor.getIdentification(),
                            submodelDescriptor.getAspectType(), jsonUtil.fromString(submodelRawPayload, Map.class));
                    submodels.add(submodel);
                } else {
                    final String errors = String.join(", ", validationResult.getValidationErrors());
                    itemContainerBuilder.tombstone(
                            Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(),
                                    new IllegalArgumentException("Submodel payload validation failed. " + errors), 0));
                }
            } catch (JsonParseException e) {
                itemContainerBuilder.tombstone(
                        Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(), e,
                                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts()));
                log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            } catch (InvalidSchemaException | RestClientException e) {
                itemContainerBuilder.tombstone(
                        Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(), e, 0));
                log.info("Cannot load JSON schema for validation. Creating Tombstone.");
            }
        });
        return submodels;
    }

    private String requestSubmodelAsString(final Endpoint endpoint) {
        return submodelFacade.getSubmodelRawPayload(endpoint.getProtocolInformation().getEndpointAddress());
    }

    private void processEndpoint(final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainer, final AssemblyPartRelationshipDTO relationship) {
        final List<String> childIds = relationship.getChildParts()
                                                  .stream()
                                                  .map(ChildDataDTO::getChildCatenaXId)
                                                  .collect(Collectors.toList());
        log.info("Processing AssemblyPartRelationship with {} children", childIds.size());

        aasTransferProcess.addIdsToProcess(childIds);
        itemContainer.assemblyPartRelationship(relationship);
    }
}
