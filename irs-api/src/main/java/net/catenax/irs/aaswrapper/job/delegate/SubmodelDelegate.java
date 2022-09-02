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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.Submodel;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.exceptions.JsonParseException;
import net.catenax.irs.semanticshub.SemanticsHubFacade;
import net.catenax.irs.services.validation.InvalidSchemaException;
import net.catenax.irs.services.validation.JsonValidatorService;
import net.catenax.irs.services.validation.ValidationResult;
import net.catenax.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Builds submodels array for AAShell from previous steps.
 * All submodels are being retrieved from EDC's components.
 * Additionally submodel descriptors from shell are being filtered to requested aspect types.
 */
@Slf4j
public class SubmodelDelegate extends AbstractDelegate {

    private final SubmodelFacade submodelFacade;
    private final SemanticsHubFacade semanticsHubFacade;
    private final JsonValidatorService jsonValidatorService;
    private final JsonUtil jsonUtil;

    public SubmodelDelegate(final AbstractDelegate nextStep,
            final SubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade,
            final JsonValidatorService jsonValidatorService,
            final JsonUtil jsonUtil) {
        super(nextStep);
        this.submodelFacade = submodelFacade;
        this.semanticsHubFacade = semanticsHubFacade;
        this.jsonValidatorService = jsonValidatorService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(
            shell -> {
                try {
                    final List<SubmodelDescriptor> aasSubmodelDescriptors = shell.getSubmodelDescriptors();
                    log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

                    final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = shell.filterDescriptorsByAspectTypes(
                            jobData.getAspectTypes());

                    if (jobData.isCollectAspects()) {
                        log.info("Collecting Submodels.");
                        filteredSubmodelDescriptorsByAspectType.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                                getSubmodels(submodelDescriptor, itemContainerBuilder, itemId)));
                    }
                    log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
                    log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

                    shell.setSubmodelDescriptors(filteredSubmodelDescriptorsByAspectType);

                } catch (RestClientException e) {
                    log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
                }
            }
        );

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
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
}
