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
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Quantity;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.RelationshipSubmodel;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.relationships.RelationshipAspect;
import org.eclipse.tractusx.irs.recursive.model.ItemUnitEnumeration;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the BOM for a given globalAssetId and extracts partner BPNLs.
 *
 * <p>This service encapsulates the local traversal steps:
 * DTR lookup -> shell descriptor -> downward relationship submodel selected by the BOM lifecycle.
 * It intentionally does <em>not</em> reuse the existing delegate chain directly,
 * because the recursive use case needs only partner candidate extraction,
 * not full submodel collection.</p>
 */
@Slf4j
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
public class RecursiveTraversalService {

    private static final String SINGLE_LEVEL_BOM_AS_PLANNED_PREFIX =
            "urn:samm:io.catenax.single_level_bom_as_planned:";
    private static final int EXPECTED_BOM_DESCRIPTOR_COUNT = 1;

    private final TraversalResolver traversalResolver;

    /** Resolves one shell and its direct BOM children. */
    @FunctionalInterface
    private interface TraversalResolver {
        TraversalResult resolve(String globalAssetId, String bpnl, BomLifecycle bomLifecycle);
    }

    /** BOM children and the shell descriptor used to resolve them. */
    record TraversalResult(List<RecursiveBomChild> bomChildren, AssetAdministrationShellDescriptor shellDescriptor) {
    }

    public RecursiveTraversalService(final DigitalTwinRegistryService digitalTwinRegistryService,
            final EdcSubmodelFacade submodelFacade,
            final JsonUtil jsonUtil) {
        this.traversalResolver = (globalAssetId, bpnl, bomLifecycle) -> resolveTraversalViaIrsClients(
                digitalTwinRegistryService, submodelFacade, jsonUtil, globalAssetId, bpnl, bomLifecycle);
    }

    /* package */ TraversalResult resolve(final String globalAssetId, final String bpnl,
            final BomLifecycle bomLifecycle) {
        log.info("Resolving BOM children for globalAssetId={}, bpnl={}, bomLifecycle={}",
                RecursiveLogValue.of(globalAssetId), RecursiveLogValue.of(bpnl), bomLifecycle);
        final TraversalResult result = traversalResolver.resolve(globalAssetId, bpnl, bomLifecycle);
        return new TraversalResult(List.copyOf(result.bomChildren()), result.shellDescriptor());
    }

    /**
     * Extracts the set of unique partner BPNLs from BOM children.
     *
     * @param children resolved BOM children
     * @return unique partner BPNLs
     */
    public Set<String> extractPartnerBpnls(final List<RecursiveBomChild> children) {
        final Set<String> bpnls = new HashSet<>();
        for (final RecursiveBomChild child : children) {
            if (child.partnerBpnl() != null && !child.partnerBpnl().isBlank()) {
                bpnls.add(child.partnerBpnl());
            }
        }
        return bpnls;
    }

    /**
     * Resolves the shell descriptor from the DTR, selects the supported BOM relationship
     * submodel and loads it through the EDC submodel facade. Missing BOM descriptors are
     * treated as leaf nodes. Present but unsupported BOM versions, missing endpoints and
     * registry or EDC failures are converted into controlled recursive traversal errors.
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static TraversalResult resolveTraversalViaIrsClients(
            final DigitalTwinRegistryService digitalTwinRegistryService,
            final EdcSubmodelFacade submodelFacade,
            final JsonUtil jsonUtil,
            final String globalAssetId,
            final String bpnl,
            final BomLifecycle bomLifecycle) {
        try {
            final PartChainIdentificationKey itemKey = PartChainIdentificationKey.builder()
                    .globalAssetId(globalAssetId)
                    .bpn(bpnl)
                    .build();
            final Shell shell = digitalTwinRegistryService.fetchShell(itemKey)
                    .orElseThrow(() -> new RecursiveExternalCallException("SHELL_NOT_FOUND",
                            "Digital twin shell was not found while resolving BOM relationships.", null));

            final RelationshipAspect relationshipAspect = RelationshipAspect.from(bomLifecycle, Direction.DOWNWARD);
            final List<Endpoint> relationshipEndpoints = relationshipEndpoints(shell.payload(), bomLifecycle);

            final List<RecursiveBomChild> result = new ArrayList<>();
            for (final Endpoint endpoint : relationshipEndpoints) {
                final String payload = requestSubmodelPayload(submodelFacade, endpoint, bpnl);
                final RelationshipSubmodel relationshipSubmodel = jsonUtil.fromString(
                        payload, relationshipAspect.getSubmodelClazz());
                for (final Relationship relationship : relationshipSubmodel.asRelationships()) {
                    final String childGlobalAssetId = relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId();
                    result.add(new RecursiveBomChild(childGlobalAssetId, relationship.getBpn(),
                            quantityOf(relationship)));
                }
            }

            return new TraversalResult(result.stream().distinct().toList(), shell.payload());
        } catch (final RegistryServiceException e) {
            throw new RecursiveExternalCallException("DIGITAL_TWIN_REQUEST_FAILED",
                    "Digital twin registry request failed while resolving BOM relationships.", e);
        } catch (final EdcClientException e) {
            throw new RecursiveExternalCallException("SUBMODEL_REQUEST_FAILED",
                    "BOM relationship submodel request failed while resolving BOM relationships.", e);
        }
    }

    private static List<Endpoint> relationshipEndpoints(final AssetAdministrationShellDescriptor shellDescriptor,
            final BomLifecycle bomLifecycle) {
        return supportedBomDescriptor(shellDescriptor, bomLifecycle)
                .map(RecursiveTraversalService::descriptorEndpoints)
                .orElseGet(List::of);
    }

    private static Optional<SubmodelDescriptor> supportedBomDescriptor(
            final AssetAdministrationShellDescriptor shellDescriptor, final BomLifecycle bomLifecycle) {
        final List<SubmodelDescriptor> bomDescriptors = Optional.ofNullable(shellDescriptor.getSubmodelDescriptors())
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .filter(RecursiveTraversalService::isSingleLevelBomAsPlannedDescriptor)
                .toList();
        if (bomDescriptors.isEmpty()) {
            return Optional.empty();
        }

        final String supportedSemanticId = supportedBomSemanticId(bomLifecycle);
        final List<SubmodelDescriptor> matchingDescriptors = bomDescriptors.stream()
                .filter(descriptor -> hasSemanticId(descriptor, supportedSemanticId))
                .toList();
        if (matchingDescriptors.isEmpty()) {
            throw new RecursiveExternalCallException("BOM_SUBMODEL_NOT_SUPPORTED",
                    "Available BOM relationship submodel version is not supported.", null);
        }
        if (matchingDescriptors.size() > EXPECTED_BOM_DESCRIPTOR_COUNT) {
            throw new RecursiveExternalCallException("BOM_SUBMODEL_NOT_SUPPORTED",
                    "Multiple supported BOM relationship submodels are available.", null);
        }
        return Optional.of(matchingDescriptors.get(0));
    }

    private static List<Endpoint> descriptorEndpoints(final SubmodelDescriptor descriptor) {
        final List<Endpoint> endpoints = Optional.ofNullable(descriptor.getEndpoints())
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .toList();
        if (endpoints.isEmpty()) {
            throw new RecursiveExternalCallException("BOM_SUBMODEL_ENDPOINT_MISSING",
                    "Supported BOM relationship submodel does not provide an endpoint.", null);
        }
        return endpoints;
    }

    private static String supportedBomSemanticId(final BomLifecycle bomLifecycle) {
        if (BomLifecycle.AS_PLANNED.equals(bomLifecycle)) {
            return SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;
        }
        throw new RecursiveExternalCallException("BOM_SUBMODEL_NOT_SUPPORTED",
                "BOM lifecycle is not supported by recursive PURIS traversal.", null);
    }

    private static boolean isSingleLevelBomAsPlannedDescriptor(final SubmodelDescriptor descriptor) {
        return semanticIds(descriptor).anyMatch(value -> value.startsWith(SINGLE_LEVEL_BOM_AS_PLANNED_PREFIX));
    }

    private static boolean hasSemanticId(final SubmodelDescriptor descriptor, final String expectedSemanticId) {
        return semanticIds(descriptor).anyMatch(expectedSemanticId::equals);
    }

    private static Stream<String> semanticIds(final SubmodelDescriptor descriptor) {
        final Reference semanticId = descriptor.getSemanticId();
        if (semanticId == null || semanticId.getKeys() == null || semanticId.getKeys().isEmpty()) {
            return Stream.empty();
        }
        return semanticId.getKeys()
                         .stream()
                         .filter(Objects::nonNull)
                         .map(SemanticId::getValue)
                         .filter(Objects::nonNull);
    }

    private static RecursiveQuantity quantityOf(final Relationship relationship) {
        if (relationship.getLinkedItem() == null || relationship.getLinkedItem().getQuantity() == null) {
            return null;
        }
        final Quantity quantity = relationship.getLinkedItem().getQuantity();
        final ItemUnitEnumeration unit = quantity.getMeasurementUnit() == null
                ? null
                : ItemUnitEnumeration.findByValue(quantity.getMeasurementUnit().getLexicalValue()).orElse(null);
        return RecursiveResultTreeSanitizer.sanitizeQuantity(RecursiveQuantity.builder()
                .value(quantity.getQuantityNumber())
                .unit(unit)
                .build());
    }

    private static String requestSubmodelPayload(
            final EdcSubmodelFacade submodelFacade,
            final Endpoint digitalTwinRegistryEndpoint,
            final String bpnl) throws EdcClientException {
        final String subprotocolBody = digitalTwinRegistryEndpoint.getProtocolInformation().getSubprotocolBody();
        final Optional<String> dspEndpoint = extractDspEndpoint(subprotocolBody);

        if (dspEndpoint.isEmpty()) {
            throw new RecursiveExternalCallException("BOM_SUBMODEL_ENDPOINT_MISSING",
                    "BOM relationship endpoint does not contain " + DSP_ENDPOINT + ".", null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Using dspEndpoint of subprotocolBody '{}' to get submodel payload",
                    RecursiveLogValue.of(subprotocolBody));
        }
        return submodelFacade.getSubmodelPayload(
                dspEndpoint.get(),
                digitalTwinRegistryEndpoint.getProtocolInformation().getHref(),
                extractAssetId(subprotocolBody),
                bpnl).getPayload();
    }
}
