/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipAspect;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.exceptions.JsonParseException;
import org.springframework.web.client.RestClientException;

/**
 * Builds relationship array for AAShell from previous step.
 * To build relationships AssemblyPartRelationship submodels
 * are being retrieved from EDC's components.
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

        final RelationshipAspect relationshipAspect = RelationshipAspect.from(jobData.getBomLifecycle(), jobData.getDirection());
        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(
            shell -> shell.findRelationshipEndpointAddresses(AspectType.fromValue(relationshipAspect.name())).forEach(address -> {
                try {
                    final List<Relationship> relationships = submodelFacade.getRelationships(address, relationshipAspect);
                    final List<String> childIds = getChildIds(relationships);

                    log.info("Processing Relationships with {} children", childIds.size());

                    aasTransferProcess.addIdsToProcess(childIds);
                    itemContainerBuilder.relationships(relationships);
                } catch (RestClientException | IllegalArgumentException e) {
                    log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                            address);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount, ProcessStep.SUBMODEL_REQUEST));
                } catch (JsonParseException e) {
                    log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount, ProcessStep.SUBMODEL_REQUEST));
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
