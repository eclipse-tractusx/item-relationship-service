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
package org.eclipse.tractusx.ess.bpn.validation;

import java.util.List;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.ess.service.SupplyChainImpacted;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

/**
 * Validation for BPNs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BPNIncidentValidation {

    /**
     * Extract BPNs from {@link AssetAdministrationShellDescriptor} of {@link Jobs}
     * and check if they are part of the incident BPNs.
     *
     * @param job          the Job to investigate
     * @param incidentBPNs the incident BPNs
     * @return {@link SupplyChainImpacted}. Yes, if one or more of the BPNs of the job matches the incident BPNs.
     * No, if none of the job BPNs matches the incident BPNs.
     * Unknown if an exception occurs while extracting the BPNs from the job.
     */
    public static SupplyChainImpacted jobContainsIncidentBPNs(final Jobs job, final List<String> incidentBPNs) {
        final boolean parentBPNAffected;
        final boolean childrenBPNsAffected;
        try {
            final AssetAdministrationShellDescriptor parentAAS = getParentAAS(job);
            final String parentBPN = getManufacturerIdFromShell(parentAAS);
            parentBPNAffected = incidentBPNs.contains(parentBPN);
            if (parentBPNAffected) {
                return SupplyChainImpacted.YES;
            }

            final List<AssetAdministrationShellDescriptor> childAASs = getChildAASs(job);
            final List<String> childrenBPNs = getBPNsFromShells(childAASs);
            childrenBPNsAffected = incidentBPNs.stream().anyMatch(childrenBPNs::contains);

            if (childrenBPNsAffected) {
                return SupplyChainImpacted.YES;
            }
        } catch (NoSuchElementException e) {
            return SupplyChainImpacted.UNKNOWN;
        }
        return SupplyChainImpacted.NO;
    }

    private static String getManufacturerIdFromShell(final AssetAdministrationShellDescriptor shellDescriptor) {

        return shellDescriptor.getSpecificAssetIds()
                              .stream()
                              .filter(specificAssetId -> "manufacturerId".equals(specificAssetId.getKey()))
                              .findFirst()
                              .orElseThrow()
                              .getValue();
    }

    private static List<String> getBPNsFromShells(final List<AssetAdministrationShellDescriptor> shellDescriptors) {
        return shellDescriptors.stream().map(BPNIncidentValidation::getManufacturerIdFromShell).toList();
    }

    private static AssetAdministrationShellDescriptor getParentAAS(final Jobs job) {
        final String globalAssetId = job.getJob().getGlobalAssetId().getGlobalAssetId();

        return job.getShells()
                  .stream()
                  .filter(shellDescriptor -> shellDescriptor.getGlobalAssetId().getValue().contains(globalAssetId))
                  .findFirst()
                  .orElseThrow();
    }

    private static List<AssetAdministrationShellDescriptor> getChildAASs(final Jobs job) {
        final String globalAssetId = job.getJob().getGlobalAssetId().getGlobalAssetId();

        return job.getShells()
                  .stream()
                  .filter(shellDescriptor -> !shellDescriptor.getGlobalAssetId().getValue().contains(globalAssetId))
                  .toList();
    }
}
