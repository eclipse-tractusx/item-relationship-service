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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.IntegrityAspect;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.util.JsonUtil;

/**
 * Retrieving Integrity Aspect
 */
@Slf4j
public class IntegrityDelegate extends AbstractDelegate {

    public static final String DATA_INTEGRITY_ASPECT = "DataIntegrity";

    private final EdcSubmodelFacade submodelFacade;
    private final ConnectorEndpointsService connectorEndpointsService;
    private final JsonUtil jsonUtil;

    public IntegrityDelegate(final EdcSubmodelFacade submodelFacade,
            final ConnectorEndpointsService connectorEndpointsService, final JsonUtil jsonUtil) {
        super(null); // no next step
        this.submodelFacade = submodelFacade;
        this.connectorEndpointsService = connectorEndpointsService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final JobParameter jobData, final AASTransferProcess aasTransferProcess,
            final PartChainIdentificationKey itemId) {

        if (jobData.isIntegrityCheck()) {
            itemContainerBuilder.build()
                                .getShells()
                                .stream()
                                .findFirst()
                                .ifPresent(shell -> shell.filterDescriptorsByAspectTypes(List.of(DATA_INTEGRITY_ASPECT))
                                                         .stream()
                                                         .peek(x -> log.debug("Number of endpoints: {}, {} ", x.getAspectType(), x.getEndpoints().size()))
                                                         .map(SubmodelDescriptor::getEndpoints)
                                                         .flatMap(Collection::stream)
                                                         .forEach(endpoint -> getIntegrityAspect(endpoint,
                                                                 itemContainerBuilder, itemId).ifPresent(
                                                                 itemContainerBuilder::integrity)));
        } else {
            log.debug("Integrity chain validation disabled.");
        }

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private Optional<IntegrityAspect> getIntegrityAspect(final Endpoint endpoint, final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final PartChainIdentificationKey itemId) {

        if (StringUtils.isBlank(itemId.getBpn())) {
            log.warn("Could not process item with id {} because no BPN was provided. Creating Tombstone.",
                    itemId.getGlobalAssetId());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(),
                            "Can't get Data Integrity aspect without a BPN", retryCount, ProcessStep.DATA_INTEGRITY_CHECK));
            return Optional.empty();
        }

        try {
            final String submodelRawPayload = requestSubmodelAsString(submodelFacade, connectorEndpointsService,
                    endpoint, itemId.getBpn());
            log.debug("Found DataIntegrity aspect for item: {}", itemId.getGlobalAssetId());

            return Optional.ofNullable(jsonUtil.fromString(submodelRawPayload, IntegrityAspect.class));

        } catch (final EdcClientException e) {
            log.warn("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                    endpoint.getProtocolInformation().getHref());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(), e,
                            retryCount, ProcessStep.DATA_INTEGRITY_CHECK));
        } catch (final JsonParseException e) {
            log.warn("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(), e,
                            retryCount, ProcessStep.DATA_INTEGRITY_CHECK));
        }

        return Optional.empty();
    }
}
