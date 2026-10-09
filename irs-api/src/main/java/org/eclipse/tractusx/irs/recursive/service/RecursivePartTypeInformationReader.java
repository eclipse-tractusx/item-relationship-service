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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.service.RecursiveSubmodelFetcher.SubmodelCollectionResult;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Reads material number and material name of the local material from its PartTypeInformation submodel.
 *
 * <p>Missing, unreachable or incomplete part type information never fails the node: it is reported
 * as a metadata tombstone and the missing material fields stay empty.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursivePartTypeInformationReader {

    /* package */ static final String PART_TYPE_INFORMATION_SEMANTIC_ID =
            "urn:samm:io.catenax.part_type_information:1.0.0#PartTypeInformation";

    private final RecursiveSubmodelFetcher submodelFetcher;

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    /* package */ MaterialMetadata collectMaterialMetadataSafely(final List<SubmodelDescriptor> descriptors,
            final String localBpnl, final String globalAssetId, final List<RecursiveTombstone> tombstones) {
        try {
            return collectMaterialMetadata(descriptors, localBpnl, globalAssetId, tombstones);
        } catch (final Exception exception) {
            log.warn("Could not collect recursive material metadata for asset '{}' at bpn '{}': causeType={}",
                    RecursiveLogValue.of(globalAssetId), RecursiveLogValue.of(localBpnl),
                    exception.getClass().getName());
            tombstones.add(requestFailedTombstone());
            return new MaterialMetadata(null, null);
        }
    }

    private MaterialMetadata collectMaterialMetadata(final List<SubmodelDescriptor> descriptors,
            final String localBpnl, final String globalAssetId, final List<RecursiveTombstone> tombstones) {
        final Optional<SubmodelDescriptor> descriptor = descriptors.stream()
                .filter(Objects::nonNull)
                .filter(RecursivePartTypeInformationReader::isPartTypeInformation)
                .findFirst();
        if (descriptor.isEmpty() || descriptor.get().getEndpoints() == null
                || descriptor.get().getEndpoints().isEmpty()) {
            tombstones.add(notAvailableTombstone());
            return new MaterialMetadata(null, null);
        }

        final SubmodelCollectionResult result = submodelFetcher.collectFromEndpoints(
                PART_TYPE_INFORMATION_SEMANTIC_ID, descriptor.get().getEndpoints(), localBpnl, globalAssetId);
        if (result.failureReason() != null) {
            tombstones.add(requestFailedTombstone());
            return new MaterialMetadata(null, null);
        }

        final Map<String, Object> partTypeInformation = RecursiveSubmodelFetcher.payloadMap(
                result.payload().get("partTypeInformation")).orElse(result.payload());
        final String materialNumber = stringValue(partTypeInformation.get("manufacturerPartId"));
        final String materialName = stringValue(partTypeInformation.get("nameAtManufacturer"));
        if (materialNumber == null || materialName == null) {
            tombstones.add(metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE,
                    "Part type information is incomplete."));
        }
        return new MaterialMetadata(materialNumber, materialName);
    }

    private static boolean isPartTypeInformation(final SubmodelDescriptor descriptor) {
        return PART_TYPE_INFORMATION_SEMANTIC_ID.equals(RecursiveSubmodelFetcher.aspectType(descriptor))
                || "PartTypeInformation".equalsIgnoreCase(descriptor.getIdShort());
    }

    private static String stringValue(final Object value) {
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            return null;
        }
        return stringValue;
    }

    /* package */ static RecursiveTombstone requestFailedTombstone() {
        return metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_REQUEST_FAILED,
                "Part type information could not be retrieved.");
    }

    /* package */ static RecursiveTombstone notAvailableTombstone() {
        return metadataTombstone(RecursiveTombstoneReason.PART_TYPE_INFORMATION_NOT_AVAILABLE,
                "Part type information is not available on the digital twin.");
    }

    private static RecursiveTombstone metadataTombstone(final RecursiveTombstoneReason reason, final String detail) {
        return RecursiveTombstones.local(null, reason, detail);
    }

    /** Material number and name of the local material, {@code null} where the value is missing. */
    record MaterialMetadata(String materialNumber, String materialName) {
    }
}
