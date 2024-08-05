/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.ProcessingError;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyExpiredException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyPermissionException;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.client.RestClientException;

/**
 * Builds submodels array for AAShell from previous steps.
 * All submodels are being retrieved from EDC's components.
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
            final List<SubmodelDescriptor> aasSubmodelDescriptors = shell.payload().getSubmodelDescriptors();
            log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

            if (jobData.isCollectAspects()) {
                log.info("Collecting Submodels.");
                final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = shell.payload()
                                                                                              .filterDescriptorsByAspectTypes(
                                                                                                      jobData.getAspects());

                filteredSubmodelDescriptorsByAspectType.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                        getSubmodels(submodelDescriptor, itemContainerBuilder, itemId.getGlobalAssetId(),
                                itemId.getBpn(), jobData.isAuditContractNegotiation())));

                log.trace("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
                log.trace("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);
            }
        });

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private List<Submodel> getSubmodels(final SubmodelDescriptor submodelDescriptor,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId, final String bpn,
            final boolean auditContractNegotiation) {

        final List<Submodel> submodels = new ArrayList<>();
        submodelDescriptor.getEndpoints().forEach(endpoint -> {

            final String endpointURL = endpoint.getProtocolInformation().getHref();
            if (StringUtils.isBlank(bpn)) {
                log.warn("Could not process item with id {} because no BPN was provided. Creating Tombstone.", itemId);
                final ProcessingError error = createProcessingError(ProcessStep.SUBMODEL_REQUEST, retryCount,
                        "Can't get submodel without a BPN");
                itemContainerBuilder.tombstone(createTombstone(itemId, null, endpointURL, error));
                return;
            }

            try {

                final String jsonSchema = semanticsHubFacade.getModelJsonSchema(submodelDescriptor.getAspectType());
                final org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor submodel = requestSubmodel(
                        submodelFacade, connectorEndpointsService, endpoint, bpn);
                final String submodelRawPayload = submodel.getPayload();
                final String contractAgreementId = getContractAgreementId(auditContractNegotiation, submodel);

                final ValidationResult validationResult = jsonValidatorService.validate(jsonSchema, submodelRawPayload);

                if (validationResult.isValid()) {
                    submodels.add(Submodel.from(submodelDescriptor.getId(), submodelDescriptor.getAspectType(),
                            contractAgreementId, jsonUtil.fromString(submodelRawPayload, Map.class)));

                } else {
                    final String errorDetail = "Submodel payload validation failed. %s".formatted(
                            String.join(", ", validationResult.getValidationErrors()));
                    final ProcessingError error = createProcessingError(ProcessStep.SCHEMA_VALIDATION, 0, errorDetail);
                    final Tombstone tombstone = createTombstone(itemId, bpn, endpointURL, error);
                    itemContainerBuilder.tombstone(tombstone);
                }

            } catch (final JsonParseException e) {
                log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                final ProcessingError error = createProcessingError(ProcessStep.SCHEMA_VALIDATION, retryCount,
                        e.getMessage());
                final Tombstone tombstone = createTombstone(itemId, bpn, endpointURL, error);
                itemContainerBuilder.tombstone(tombstone);

            } catch (final SchemaNotFoundException | InvalidSchemaException | RestClientException e) {
                log.info("Cannot load JSON schema for validation. Creating Tombstone.");
                final ProcessingError error = createProcessingError(ProcessStep.SCHEMA_REQUEST, 0, e.getMessage());
                itemContainerBuilder.tombstone(createTombstone(itemId, bpn, endpointURL, error));

            } catch (final UsagePolicyPermissionException | UsagePolicyExpiredException e) {
                log.info("Encountered usage policy permission exception: {}. Creating Tombstone.", e.getMessage());
                final Map<String, Object> policy = jsonUtil.asMap(e.getPolicy());
                final ProcessingError error = createProcessingError(ProcessStep.USAGE_POLICY_VALIDATION, 0,
                        e.getMessage());
                final Tombstone tombstone = Tombstone.builder()
                                                     .endpointURL(endpointURL)
                                                     .catenaXId(itemId)
                                                     .processingError(error)
                                                     .businessPartnerNumber(e.getBusinessPartnerNumber())
                                                     .policy(policy)
                                                     .build();
                itemContainerBuilder.tombstone(tombstone);

            } catch (final EdcClientException e) {
                log.info("Submodel Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                final ProcessingError error = createProcessingError(ProcessStep.SUBMODEL_REQUEST, 0, e.getMessage());
                final Tombstone tombstone = createTombstone(itemId, bpn, endpointURL, error);
                itemContainerBuilder.tombstone(tombstone);
            }
        });

        return submodels;
    }

    private Tombstone createTombstone(final String itemId, final String bpn, final String endpointURL,
            final ProcessingError error) {
        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(itemId)
                        .processingError(error)
                        .businessPartnerNumber(bpn)
                        .build();
    }

    private ProcessingError createProcessingError(final ProcessStep processStep, final int retryCount,
            final String errorDetail) {
        return ProcessingError.builder()
                              .withProcessStep(processStep)
                              .withRetryCounterAndLastAttemptNow(retryCount)
                              .withErrorDetail(errorDetail)
                              .build();
    }

    @Nullable
    private String getContractAgreementId(final boolean auditContractNegotiation,
            final org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor submodel) {
        return auditContractNegotiation ? submodel.getCid() : null;
    }

}
