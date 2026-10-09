/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.service.RecursivePartTypeInformationReader.MaterialMetadata;
import org.eclipse.tractusx.irs.recursive.service.RecursiveSubmodelFetcher.SubmodelCollectionResult;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;

/**
 * Collects material metadata and anonymized PURIS aspects for one recursive node.
 *
 * <p>Submodel payloads are loaded by {@link RecursiveSubmodelFetcher}, material number and name by
 * {@link RecursivePartTypeInformationReader}.</p>
 */
@Slf4j
public class RecursiveSubmodelCollector {

    private final DigitalTwinRegistryService digitalTwinRegistryService;
    private final RecursiveSubmodelFetcher submodelFetcher;
    private final RecursivePartTypeInformationReader partTypeInformationReader;

    public RecursiveSubmodelCollector(final DigitalTwinRegistryService digitalTwinRegistryService,
            final EdcSubmodelFacade submodelFacade, final ObjectMapper objectMapper) {
        this.digitalTwinRegistryService = digitalTwinRegistryService;
        this.submodelFetcher = new RecursiveSubmodelFetcher(submodelFacade, objectMapper);
        this.partTypeInformationReader = new RecursivePartTypeInformationReader(submodelFetcher);
    }

    public RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
            final List<String> requestedAspects) {
        return collect(globalAssetId, localBpnl, requestedAspects, null);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    /* package */ RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
            final List<String> requestedAspects, final AssetAdministrationShellDescriptor resolvedShell) {
        final List<RecursiveAspectItem> items = new ArrayList<>();
        final List<RecursiveTombstone> tombstones = new ArrayList<>();
        final List<RecursiveAspect> aspectsToCollect = supportedAspects(requestedAspects, tombstones);
        if (aspectsToCollect.isEmpty()) {
            return node(null, null, items, tombstones);
        }

        final AssetAdministrationShellDescriptor shell;
        try {
            shell = resolvedShell == null ? fetchShell(globalAssetId, localBpnl) : resolvedShell;
        } catch (final Exception exception) {
            tombstones.add(RecursivePartTypeInformationReader.requestFailedTombstone());
            for (final RecursiveAspect aspect : aspectsToCollect) {
                tombstones.add(aspectTombstone(aspect.getSemanticId(),
                        RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED,
                        RecursiveFailureDetails.anonymizedDetail(exception)));
            }
            return node(null, null, items, tombstones);
        }

        if (shell == null || shell.getSubmodelDescriptors() == null) {
            addMissingShellTombstones(aspectsToCollect, tombstones);
            return node(null, null, items, tombstones);
        }

        final List<SubmodelDescriptor> descriptors = shell.getSubmodelDescriptors();
        final MaterialMetadata materialMetadata = partTypeInformationReader.collectMaterialMetadataSafely(
                descriptors, localBpnl, globalAssetId, tombstones);
        collectRequestedAspects(descriptors, localBpnl, globalAssetId, aspectsToCollect, items, tombstones);
        return node(materialMetadata.materialNumber(), materialMetadata.materialName(), items, tombstones);
    }

    private List<RecursiveAspect> supportedAspects(final List<String> requestedAspects,
            final List<RecursiveTombstone> tombstones) {
        final List<RecursiveAspect> aspects = new ArrayList<>();
        for (final String requestedAspect : requestedAspects == null ? List.<String>of() : requestedAspects) {
            final Optional<RecursiveAspect> aspect = RecursiveAspect.fromSemanticId(requestedAspect);
            if (aspect.isEmpty()) {
                tombstones.add(aspectTombstone(requestedAspect, RecursiveTombstoneReason.UNSUPPORTED_ANONYMIZED_ASPECT,
                        "Aspect is not supported by the recursive use case."));
            } else if (!aspects.contains(aspect.get())) {
                aspects.add(aspect.get());
            }
        }
        return aspects;
    }

    private AssetAdministrationShellDescriptor fetchShell(final String globalAssetId, final String localBpnl)
            throws RegistryServiceException {
        if (digitalTwinRegistryService == null) {
            return null;
        }
        return digitalTwinRegistryService.fetchShell(PartChainIdentificationKey.builder()
                        .globalAssetId(globalAssetId)
                        .bpn(localBpnl)
                        .build())
                .map(shell -> shell.payload())
                .orElse(null);
    }

    private void addMissingShellTombstones(final List<RecursiveAspect> aspects,
            final List<RecursiveTombstone> tombstones) {
        tombstones.add(RecursivePartTypeInformationReader.notAvailableTombstone());
        for (final RecursiveAspect aspect : aspects) {
            tombstones.add(aspectTombstone(aspect.getSemanticId(), RecursiveTombstoneReason.LOCAL_ASPECT_NOT_AVAILABLE,
                    "No digital twin shell found for requested aspect."));
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void collectRequestedAspects(final List<SubmodelDescriptor> descriptors, final String localBpnl,
            final String globalAssetId, final List<RecursiveAspect> aspects,
            final List<RecursiveAspectItem> items, final List<RecursiveTombstone> tombstones) {
        for (final RecursiveAspect aspect : aspects) {
            try {
                collectRequestedAspect(descriptors, localBpnl, globalAssetId, aspect, items, tombstones);
            } catch (final Exception exception) {
                log.warn("Could not collect recursive aspect '{}' for asset '{}' at bpn '{}': causeType={}",
                        RecursiveLogValue.of(aspect.getSemanticId()), RecursiveLogValue.of(globalAssetId),
                        RecursiveLogValue.of(localBpnl), exception.getClass().getName());
                tombstones.add(aspectTombstone(aspect.getSemanticId(),
                        RecursiveFailureReasonMapper.localAspectFailureReason(exception),
                        RecursiveFailureDetails.anonymizedDetail(exception)));
            }
        }
    }

    private void collectRequestedAspect(final List<SubmodelDescriptor> descriptors, final String localBpnl,
            final String globalAssetId, final RecursiveAspect aspect, final List<RecursiveAspectItem> items,
            final List<RecursiveTombstone> tombstones) {
        final Optional<SubmodelDescriptor> descriptor = descriptors.stream()
                .filter(Objects::nonNull)
                .filter(candidate -> aspect.matchesDescriptor(RecursiveSubmodelFetcher.aspectType(candidate),
                        candidate.getIdShort()))
                .findFirst();
        if (descriptor.isEmpty() || descriptor.get().getEndpoints() == null
                || descriptor.get().getEndpoints().isEmpty()) {
            tombstones.add(aspectTombstone(aspect.getSemanticId(), RecursiveTombstoneReason.LOCAL_ASPECT_NOT_AVAILABLE,
                    "Requested aspect is not available on the digital twin."));
            return;
        }

        final SubmodelCollectionResult result = submodelFetcher.collectFromEndpoints(aspect.getSemanticId(),
                descriptor.get().getEndpoints(), localBpnl, globalAssetId);
        if (result.failureReason() == null) {
            items.add(RecursiveAspectItem.builder()
                                         .aspect(aspect.getSemanticId())
                                         .items(result.payload())
                                         .build());
        } else {
            tombstones.add(aspectTombstone(aspect.getSemanticId(), result.failureReason(), result.failureDetail()));
        }
    }

    private RecursiveTombstone aspectTombstone(final String aspect, final RecursiveTombstoneReason reason,
            final String detail) {
        return RecursiveTombstones.local(aspect, reason, detail);
    }

    private RecursiveChildItem node(final String materialNumber, final String materialName,
            final List<RecursiveAspectItem> items, final List<RecursiveTombstone> tombstones) {
        return RecursiveChildItem.builder()
                                 .materialNumber(materialNumber)
                                 .materialName(materialName)
                                 .items(List.copyOf(items))
                                 .tombstones(List.copyOf(tombstones))
                                 .childItems(List.of())
                                 .build();
    }
}
