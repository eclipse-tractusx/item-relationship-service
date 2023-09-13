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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Builds submodels array for AAShell from previous steps.
 * All submodels are being retrieved from EDC's components.
 * Additionally submodel descriptors from shell are being filtered to requested aspect types.
 */
@Slf4j
public class SubmodelDelegate extends AbstractDelegate {

    private final EdcSubmodelFacade submodelFacade;
    private final SemanticsHubFacade semanticsHubFacade;
    private final JsonValidatorService jsonValidatorService;
    private final JsonUtil jsonUtil;
    private final ConnectorEndpointsService connectorEndpointsService;

    public SubmodelDelegate(final EdcSubmodelFacade submodelFacade, final SemanticsHubFacade semanticsHubFacade,
            final JsonValidatorService jsonValidatorService, final JsonUtil jsonUtil,
            final ConnectorEndpointsService connectorEndpointsService) {
        super(null); // no next step
        this.submodelFacade = submodelFacade;
        this.semanticsHubFacade = semanticsHubFacade;
        this.jsonValidatorService = jsonValidatorService;
        this.jsonUtil = jsonUtil;
        this.connectorEndpointsService = connectorEndpointsService;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final JobParameter jobData, final AASTransferProcess aasTransferProcess,
            final PartChainIdentificationKey itemId) {

        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(shell -> {
            final List<SubmodelDescriptor> aasSubmodelDescriptors = shell.getSubmodelDescriptors();
            log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

            final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = shell.filterDescriptorsByAspectTypes(
                    jobData.getAspects());

            if (jobData.isCollectAspects()) {
                log.info("Collecting Submodels.");
                filteredSubmodelDescriptorsByAspectType.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                        getSubmodels(submodelDescriptor, itemContainerBuilder, itemId.getGlobalAssetId(),
                                itemId.getBpn())));
            }
            log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
            log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

            shell.setSubmodelDescriptors(filteredSubmodelDescriptorsByAspectType);

        });

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private List<Submodel> getSubmodels(final SubmodelDescriptor submodelDescriptor,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId, final String bpn) {
        final List<Submodel> submodels = new ArrayList<>();
        submodelDescriptor.getEndpoints().forEach(endpoint -> {

            if (StringUtils.isBlank(bpn)) {
                log.warn("Could not process item with id {} because no BPN was provided. Creating Tombstone.", itemId);
                itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(),
                        "Can't get submodel without a BPN", retryCount, ProcessStep.SUBMODEL_REQUEST));
                return;
            }

            try {
                final String jsonSchema = semanticsHubFacade.getModelJsonSchema(submodelDescriptor.getAspectType());
                final String submodelRawPayload = requestSubmodelAsString(submodelFacade, connectorEndpointsService,
                        endpoint, bpn);

                final ValidationResult validationResult = jsonValidatorService.validate(jsonSchema, submodelRawPayload);

                if (validationResult.isValid()) {
                    final Submodel submodel = Submodel.from(submodelDescriptor.getId(),
                            submodelDescriptor.getAspectType(), jsonUtil.fromString(submodelRawPayload, Map.class), itemId);
                    submodels.add(submodel);
                } else {
                    final String errors = String.join(", ", validationResult.getValidationErrors());
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(),
                            new IllegalArgumentException("Submodel payload validation failed. " + errors), 0,
                            ProcessStep.SCHEMA_VALIDATION));
                }
            } catch (final JsonParseException e) {
                itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e,
                        RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts(), ProcessStep.SCHEMA_VALIDATION));
                log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            } catch (final SchemaNotFoundException | InvalidSchemaException | RestClientException e) {
                itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e, 0,
                        ProcessStep.SCHEMA_REQUEST));
                log.info("Cannot load JSON schema for validation. Creating Tombstone.");
            } catch (final UsagePolicyException e) {
                log.info("Encountered usage policy exception: {}. Creating Tombstone.", e.getMessage());
                itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e, 0,
                        ProcessStep.USAGE_POLICY_VALIDATION));
            } catch (final EdcClientException e) {
                log.info("Submodel Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                itemContainerBuilder.tombstone(Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e, 0,
                        ProcessStep.SUBMODEL_REQUEST));
            }
        });
        return submodels;
    }

}
