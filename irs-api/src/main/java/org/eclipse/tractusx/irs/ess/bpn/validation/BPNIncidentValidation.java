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
package org.eclipse.tractusx.irs.ess.bpn.validation;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.ess.service.SupplyChainImpacted;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.partasplanned.PartAsPlanned;
import org.eclipse.tractusx.irs.component.partsiteinformationasplanned.PartSiteInformationAsPlanned;

/**
 * Validation for BPNs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BPNIncidentValidation {

    /**
     * Extract BPNs from {@link AssetAdministrationShellDescriptor} of {@link Jobs}
     * and check if they are part of the incident BPNs.
     *
     * @param incidentBPNs the incident BPNs
     * @param shells the shell from which to take the BPNs
     * @return Yes, if one or more of the BPNs of the job matches the incident BPNs.
     * No, if none of the job BPNs matches the incident BPNs.
     * Unknown if job contains no AssetAdministrationShellDescriptors
     * or there was an error extracting the BPN.
     */
    public static SupplyChainImpacted jobContainsIncidentBPNs(final List<AssetAdministrationShellDescriptor> shells,
            final List<String> incidentBPNs) {
        try {
            final List<String> bpnsFromShells = getBPNsFromShells(shells);
            if (bpnsFromShells.isEmpty()) {
                return SupplyChainImpacted.UNKNOWN;
            }
            if (incidentBPNs.stream().anyMatch(bpnsFromShells::contains)) {
                return SupplyChainImpacted.YES;
            }
        } catch (NoSuchElementException e) {
            return SupplyChainImpacted.UNKNOWN;
        }
        return SupplyChainImpacted.NO;
    }

    /**
     * Check if siteIds of PartAsPlanned are part of the incident BPNs.
     *
     * @param incidentBPNs                 the incident BPNs
     * @param partSiteInformationAsPlanned The {@link PartSiteInformationAsPlanned} to extract the
     *                                     siteIds from
     * @return Yes, if one or more of the siteIds of PartAsPlanned matches the incident BPNs.
     * No, if none of the job siteIds matches the incident BPNs.
     * Unknown if PartAsPlanned contains no siteIds.
     * @throws InvalidAspectTypeFormatException if any of the sites does not contain a catenaXSiteId
     */
    public static SupplyChainImpacted jobContainsIncidentBPNSs(
            final PartSiteInformationAsPlanned partSiteInformationAsPlanned, final List<String> incidentBPNs)
            throws InvalidAspectTypeFormatException {
        if (partSiteInformationAsPlanned.sites().stream().anyMatch(site -> site.catenaXSiteId() == null)) {
            throw new InvalidAspectTypeFormatException("'PartSiteInformationAsPlanned' exists, but catenaXSiteId could not be found.");
        }
        final List<String> siteIds = partSiteInformationAsPlanned.getCatenaXSiteId();
        try {
            if (siteIds.isEmpty()) {
                return SupplyChainImpacted.UNKNOWN;
            }
            if (incidentBPNs.stream().anyMatch(siteIds::contains)) {
                return SupplyChainImpacted.YES;
            }
        } catch (NoSuchElementException e) {
            return SupplyChainImpacted.UNKNOWN;
        }
        return SupplyChainImpacted.NO;
    }

    /**
     * Validates the validityPeriod of {@link PartAsPlanned}.
     *
     * @param partAsPlanned PartAsPlanned aspect to check the validityPeriod
     * @return Yes, if current time is outside the range of validFrom to validTo.
     * No, if current time is between the range of validFrom to validTo.
     * Else Unknown.
     */
    public static SupplyChainImpacted partAsPlannedValidity(final PartAsPlanned partAsPlanned) {
        final ZonedDateTime validFrom = partAsPlanned.validityPeriod().validFrom();
        final ZonedDateTime validTo = partAsPlanned.validityPeriod().validTo();
        final ZonedDateTime now = ZonedDateTime.now();
        if (validTo.isAfter(now) && validFrom.isBefore(now)) {
            return SupplyChainImpacted.NO;
        } else if (validTo.isBefore(now) || validFrom.isAfter(now)) {
            return SupplyChainImpacted.YES;
        }
        return SupplyChainImpacted.UNKNOWN;
    }

    private static List<String> getBPNsFromShells(final List<AssetAdministrationShellDescriptor> shellDescriptors) {
        return shellDescriptors.stream().flatMap(shell -> shell.findManufacturerId().stream()).toList();
    }
}
