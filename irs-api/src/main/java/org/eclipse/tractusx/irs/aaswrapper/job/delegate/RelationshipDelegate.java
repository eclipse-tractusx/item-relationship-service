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

import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractAssetId;
import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractSuffix;

import java.net.URISyntaxException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.RelationshipAspect;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.util.JsonUtil;

/**
 * Builds relationship array for AAShell from previous step.
 * To build relationships traversal submodels
 * are being retrieved from EDC's components.
 */
@Slf4j
public class RelationshipDelegate extends AbstractDelegate {

    private final EdcSubmodelFacade submodelFacade;
    private final ConnectorEndpointsService connectorEndpointsService;
    private final JsonUtil jsonUtil;

    public RelationshipDelegate(final AbstractDelegate nextStep, final EdcSubmodelFacade submodelFacade,
            final ConnectorEndpointsService connectorEndpointsService, final JsonUtil jsonUtil) {
        super(nextStep);
        this.submodelFacade = submodelFacade;
        this.connectorEndpointsService = connectorEndpointsService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final JobParameter jobData, final AASTransferProcess aasTransferProcess, final String itemId) {

        final RelationshipAspect relationshipAspect = RelationshipAspect.from(jobData.getBomLifecycle(),
                jobData.getDirection());
        itemContainerBuilder.build()
                            .getShells()
                            .stream()
                            .findFirst()
                            .ifPresent(shell -> shell.findRelationshipEndpointAddresses(
                                                             AspectType.fromValue(relationshipAspect.getName()))
                                                     .forEach(endpoint -> processEndpoint(endpoint, jobData,
                                                             relationshipAspect, aasTransferProcess,
                                                             itemContainerBuilder, itemId)));

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private void processEndpoint(final Endpoint endpoint, final JobParameter jobData,
            final RelationshipAspect relationshipAspect, final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        try {
            log.info("Requesting bpn {} from bean {}", jobData.getBpn(), connectorEndpointsService);
            final String connectorEndpoint = connectorEndpointsService.fetchConnectorEndpoints(jobData.getBpn())
                                                                      .stream()
                                                                      .findFirst()
                                                                      .orElseThrow();
            final var submodelRawPayload = submodelFacade.getSubmodelRawPayload(connectorEndpoint,
                    extractSuffix(endpoint.getProtocolInformation().getHref()),
                    extractAssetId(endpoint.getProtocolInformation().getSubprotocolBody()));

            final var relationships = jsonUtil.fromString(submodelRawPayload, relationshipAspect.getSubmodelClazz())
                                              .asRelationships();

            final List<String> idsToProcess = getIdsToProcess(relationships, relationshipAspect.getDirection());

            log.info("Processing Relationships with {} items", idsToProcess.size());

            aasTransferProcess.addIdsToProcess(idsToProcess);
            itemContainerBuilder.relationships(relationships);
            itemContainerBuilder.bpns(getBpnsFrom(relationships));
        } catch (final EdcClientException | URISyntaxException e) {
            log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                    endpoint.getProtocolInformation().getHref());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e, retryCount,
                            ProcessStep.SUBMODEL_REQUEST));
        } catch (final JsonParseException e) {
            log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId, endpoint.getProtocolInformation().getHref(), e, retryCount,
                            ProcessStep.SUBMODEL_REQUEST));
        }
    }

    private static List<Bpn> getBpnsFrom(final List<Relationship> relationships) {
        return relationships.stream().map(Relationship::getBpn).map(Bpn::withManufacturerId).toList();
    }

    private List<String> getIdsToProcess(final List<Relationship> relationships, final Direction direction) {
        return switch (direction) {
            case DOWNWARD -> getChildIds(relationships);
            case UPWARD -> getParentIds(relationships);
        };
    }

    private List<String> getParentIds(final List<Relationship> relationships) {
        return relationships.stream()
                            .map(Relationship::getCatenaXId)
                            .map(GlobalAssetIdentification::getGlobalAssetId)
                            .toList();
    }

    private List<String> getChildIds(final List<Relationship> relationships) {
        return relationships.stream()
                            .map(Relationship::getLinkedItem)
                            .map(LinkedItem::getChildCatenaXId)
                            .map(GlobalAssetIdentification::getGlobalAssetId)
                            .toList();
    }
}
