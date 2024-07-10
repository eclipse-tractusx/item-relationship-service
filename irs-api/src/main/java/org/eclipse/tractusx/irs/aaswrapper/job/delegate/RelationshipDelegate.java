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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyExpiredException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyPermissionException;
import org.eclipse.tractusx.irs.edc.client.relationships.RelationshipAspect;
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
            final JobParameter jobData, final AASTransferProcess aasTransferProcess,
            final PartChainIdentificationKey itemId) {

        final RelationshipAspect relationshipAspect = RelationshipAspect.from(jobData.getBomLifecycle(),
                jobData.getDirection());
        itemContainerBuilder.build()
                            .getShells()
                            .stream()
                            .findFirst()
                            .ifPresent(shell -> shell.payload()
                                                     .findRelationshipEndpointAddresses(
                                                             AspectType.fromValue(relationshipAspect.getName()))
                                                     .forEach(endpoint -> processEndpoint(endpoint, relationshipAspect,
                                                             aasTransferProcess, itemContainerBuilder, itemId)));

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private void processEndpoint(final Endpoint endpoint, final RelationshipAspect relationshipAspect,
            final AASTransferProcess aasTransferProcess, final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final PartChainIdentificationKey itemId) {

        if (StringUtils.isBlank(itemId.getBpn())) {
            log.warn("Could not process item with id {} because no BPN was provided. Creating Tombstone.",
                    itemId.getGlobalAssetId());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(),
                            "Can't get relationship without a BPN", retryCount, ProcessStep.SUBMODEL_REQUEST));
            return;
        }

        try {
            final String submodelRawPayload = requestSubmodel(submodelFacade, connectorEndpointsService, endpoint,
                    itemId.getBpn()).getPayload();

            final var relationships = jsonUtil.fromString(submodelRawPayload, relationshipAspect.getSubmodelClazz())
                                              .asRelationships();

            final List<PartChainIdentificationKey> idsToProcess = getIdsToProcess(relationships,
                    relationshipAspect.getDirection());

            log.info("Processing Relationships with {} items", idsToProcess.size());

            aasTransferProcess.addIdsToProcess(idsToProcess);
            itemContainerBuilder.relationships(relationships);
            itemContainerBuilder.bpns(getBpnsFrom(relationships));
        } catch (final UsagePolicyPermissionException | UsagePolicyExpiredException e) {
            log.info("Encountered usage policy exception: {}. Creating Tombstone.", e.getMessage());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(), e, 0,
                            ProcessStep.USAGE_POLICY_VALIDATION, e.getBusinessPartnerNumber(),
                            jsonUtil.asMap(e.getPolicy())));
        } catch (final EdcClientException e) {
            log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                    endpoint.getProtocolInformation().getHref());
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(), e, 0,
                            ProcessStep.SUBMODEL_REQUEST));
        } catch (final JsonParseException e) {
            log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), endpoint.getProtocolInformation().getHref(), e, 0,
                            ProcessStep.SUBMODEL_REQUEST));
        }
    }

    private static List<Bpn> getBpnsFrom(final List<Relationship> relationships) {
        return relationships.stream()
                            .map(Relationship::getBpn)
                            .filter(StringUtils::isNotBlank)
                            .map(Bpn::withManufacturerId)
                            .toList();
    }

    private List<PartChainIdentificationKey> getIdsToProcess(final List<Relationship> relationships,
            final Direction direction) {
        return switch (direction) {
            case DOWNWARD -> getChildIds(relationships);
            case UPWARD -> getParentIds(relationships);
        };
    }

    private List<PartChainIdentificationKey> getParentIds(final List<Relationship> relationships) {
        return relationships.stream()
                            .map(relationship -> PartChainIdentificationKey.builder()
                                                                           .globalAssetId(relationship.getCatenaXId()
                                                                                                      .getGlobalAssetId())
                                                                           .bpn(relationship.getBpn())
                                                                           .build())
                            .toList();
    }

    private List<PartChainIdentificationKey> getChildIds(final List<Relationship> relationships) {
        return relationships.stream()
                            .map(relationship -> PartChainIdentificationKey.builder()
                                                                           .globalAssetId(relationship.getLinkedItem()
                                                                                                      .getChildCatenaXId()
                                                                                                      .getGlobalAssetId())
                                                                           .bpn(relationship.getBpn())
                                                                           .build())
                            .toList();
    }
}
