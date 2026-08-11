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

import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.DSP_ENDPOINT;
import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractAssetId;
import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractDspEndpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;

/** Collects material metadata and anonymized PURIS aspects for one recursive node. */
@Slf4j
@RequiredArgsConstructor
public class RecursiveSubmodelCollector {

    /* package */ static final String PART_TYPE_INFORMATION_SEMANTIC_ID =
            "urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation";

    private final DigitalTwinRegistryService digitalTwinRegistryService;
    private final EdcSubmodelFacade submodelFacade;
    private final ObjectMapper objectMapper;

    public RecursiveChildItem collect(final String globalAssetId, final String localBpnl,
            final List<String> requestedAspects) {
        return collect(globalAssetId, localBpnl, requestedAspects, null);
    }

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
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                    "Part type information could not be retrieved."));
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
        final MaterialMetadata materialMetadata = collectMaterialMetadataSafely(
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
        tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE,
                "Part type information is not available on the digital twin."));
        for (final RecursiveAspect aspect : aspects) {
            tombstones.add(aspectTombstone(aspect.getSemanticId(), RecursiveTombstoneReason.LOCAL_ASPECT_NOT_AVAILABLE,
                    "No digital twin shell found for requested aspect."));
        }
    }

    private MaterialMetadata collectMaterialMetadata(final List<SubmodelDescriptor> descriptors,
            final String localBpnl, final String globalAssetId, final List<RecursiveTombstone> tombstones) {
        final Optional<SubmodelDescriptor> descriptor = descriptors.stream()
                .filter(Objects::nonNull)
                .filter(this::isPartTypeInformation)
                .findFirst();
        if (descriptor.isEmpty() || descriptor.get().getEndpoints() == null
                || descriptor.get().getEndpoints().isEmpty()) {
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE,
                    "Part type information is not available on the digital twin."));
            return new MaterialMetadata(null, null);
        }

        final SubmodelCollectionResult result = collectFromEndpoints(PART_TYPE_INFORMATION_SEMANTIC_ID,
                descriptor.get().getEndpoints(), localBpnl, globalAssetId);
        if (result.failureReason() != null) {
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                    "Part type information could not be retrieved."));
            return new MaterialMetadata(null, null);
        }

        final Map<String, Object> partTypeInformation = payloadMap(
                result.payload().get("partTypeInformation")).orElse(result.payload());
        final String materialNumber = stringValue(partTypeInformation.get("manufacturerPartId"));
        final String materialName = stringValue(partTypeInformation.get("nameAtManufacturer"));
        if (materialNumber == null || materialName == null) {
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE,
                    "Part type information is incomplete."));
        }
        return new MaterialMetadata(materialNumber, materialName);
    }

    private MaterialMetadata collectMaterialMetadataSafely(final List<SubmodelDescriptor> descriptors,
            final String localBpnl, final String globalAssetId, final List<RecursiveTombstone> tombstones) {
        try {
            return collectMaterialMetadata(descriptors, localBpnl, globalAssetId, tombstones);
        } catch (final Exception exception) {
            log.warn("Could not collect recursive material metadata for asset '{}' at bpn '{}': causeType={}",
                    RecursiveLogValue.of(globalAssetId), RecursiveLogValue.of(localBpnl),
                    exception.getClass().getName());
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                    "Part type information could not be retrieved."));
            return new MaterialMetadata(null, null);
        }
    }

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
                tombstones.add(aspectTombstone(aspect.getSemanticId(), failureReason(exception),
                        RecursiveFailureDetails.anonymizedDetail(exception)));
            }
        }
    }

    private void collectRequestedAspect(final List<SubmodelDescriptor> descriptors, final String localBpnl,
            final String globalAssetId, final RecursiveAspect aspect, final List<RecursiveAspectItem> items,
            final List<RecursiveTombstone> tombstones) {
        final Optional<SubmodelDescriptor> descriptor = descriptors.stream()
                .filter(Objects::nonNull)
                .filter(candidate -> aspect.matchesDescriptor(aspectType(candidate), candidate.getIdShort()))
                .findFirst();
        if (descriptor.isEmpty() || descriptor.get().getEndpoints() == null
                || descriptor.get().getEndpoints().isEmpty()) {
            tombstones.add(aspectTombstone(aspect.getSemanticId(), RecursiveTombstoneReason.LOCAL_ASPECT_NOT_AVAILABLE,
                    "Requested aspect is not available on the digital twin."));
            return;
        }

        final SubmodelCollectionResult result = collectFromEndpoints(aspect.getSemanticId(),
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

    private boolean isPartTypeInformation(final SubmodelDescriptor descriptor) {
        return PART_TYPE_INFORMATION_SEMANTIC_ID.equals(aspectType(descriptor))
                || "PartTypeInformation".equalsIgnoreCase(descriptor.getIdShort());
    }

    private String aspectType(final SubmodelDescriptor descriptor) {
        final Reference semanticId = descriptor.getSemanticId();
        if (semanticId == null || semanticId.getKeys() == null || semanticId.getKeys().isEmpty()) {
            return null;
        }
        final SemanticId firstKey = semanticId.getKeys().get(0);
        return firstKey == null ? null : firstKey.getValue();
    }

    private SubmodelCollectionResult collectFromEndpoints(final String aspect, final List<Endpoint> endpoints,
            final String localBpnl, final String globalAssetId) {
        Exception lastFailure = null;
        for (final Endpoint endpoint : endpoints) {
            try {
                return new SubmodelCollectionResult(requestSubmodelPayload(endpoint, localBpnl), null, null);
            } catch (final Exception exception) {
                lastFailure = exception;
                log.warn("Could not collect recursive aspect '{}' from asset '{}' at bpn '{}' via one of {} "
                                + "endpoint(s): causeType={}",
                        RecursiveLogValue.of(aspect), RecursiveLogValue.of(globalAssetId),
                        RecursiveLogValue.of(localBpnl), endpoints.size(), exception.getClass().getName());
            }
        }
        return new SubmodelCollectionResult(Map.of(), failureReason(lastFailure),
                RecursiveFailureDetails.anonymizedDetail(lastFailure));
    }

    private Map<String, Object> readPayload(final String payload) throws Exception {
        final Object parsedPayload = objectMapper.readValue(payload, Object.class);
        return payloadMap(parsedPayload)
                .orElseThrow(() -> new IllegalArgumentException("Submodel payload must be a JSON object."));
    }

    private Optional<Map<String, Object>> payloadMap(final Object payload) {
        if (!(payload instanceof Map<?, ?> rawMap)) {
            return Optional.empty();
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> {
            if (key != null) {
                result.put(key.toString(), value);
            }
        });
        return Optional.of(Collections.unmodifiableMap(result));
    }

    private Map<String, Object> requestSubmodelPayload(final Endpoint endpoint, final String localBpnl)
            throws Exception {
        final String body = endpoint.getProtocolInformation().getSubprotocolBody();
        final Optional<String> dsp = extractDspEndpoint(body);
        if (dsp.isEmpty()) {
            throw new EdcClientException("Submodel descriptor endpoint does not contain " + DSP_ENDPOINT + ".");
        }
        return readPayload(submodelFacade.getSubmodelPayload(
                dsp.get(), endpoint.getProtocolInformation().getHref(), extractAssetId(body), localBpnl).getPayload());
    }

    private RecursiveTombstoneReason failureReason(final Exception exception) {
        final Optional<RecursiveTombstoneReason> policyReason = RecursiveFailureReasonMapper.policyReason(exception);
        if (policyReason.isPresent()) {
            return policyReason.get();
        }
        if (exception instanceof RecursiveExternalCallException externalCallException) {
            return RecursiveTombstoneReason.fromInternalReason(externalCallException.getReason());
        }
        return RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED;
    }

    private RecursiveTombstone aspectTombstone(final String aspect, final RecursiveTombstoneReason reason,
            final String detail) {
        return RecursiveTombstones.local(aspect, reason, detail);
    }

    private RecursiveTombstone metadataTombstone(final RecursiveTombstoneReason reason, final String detail) {
        return RecursiveTombstones.local(null, reason, detail);
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

    private String stringValue(final Object value) {
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            return null;
        }
        return stringValue;
    }

    private record SubmodelCollectionResult(Map<String, Object> payload, RecursiveTombstoneReason failureReason,
                                            String failureDetail) {
    }

    private record MaterialMetadata(String materialNumber, String materialName) {
    }
}
