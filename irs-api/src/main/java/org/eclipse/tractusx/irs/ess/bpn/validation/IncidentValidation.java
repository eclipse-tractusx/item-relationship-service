/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.ess.bpn.validation;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.component.partasplanned.PartAsPlanned;
import org.eclipse.tractusx.irs.component.partsiteinformationasplanned.PartSiteInformationAsPlanned;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.ess.service.BpnInvestigationJob;
import org.eclipse.tractusx.irs.ess.service.SupplyChainImpacted;

/**
 * Validation for BPN incidents.
 */
@Slf4j
public final class IncidentValidation {
    private IncidentValidation() {
    }

    /**
     * Investigates the Incident based on a {@link Jobs}.
     *
     * @param investigationJob the {@link BpnInvestigationJob} where
     * @param job              the {@link Jobs} Job where aspect models are extracted from
     * @param completedJobId   id of the completed Job
     * @return {@link InvestigationResult} containing the {@link SupplyChainImpacted} as well as the
     * completed {@link Jobs} filled with potential tombstones.
     */
    public static InvestigationResult getResult(final BpnInvestigationJob investigationJob, final Jobs job,
            final UUID completedJobId) {
        SupplyChainImpacted partAsPlannedValidity;
        Jobs completedJob = job;
        try {
            partAsPlannedValidity = validatePartAsPlanned(completedJob);
        } catch (final AspectTypeNotFoundException e) {
            completedJob = createTombstone(e, completedJob);
            partAsPlannedValidity = SupplyChainImpacted.UNKNOWN;
        }
        log.info("Local validation of PartAsPlanned Validity was done for job {}. with result {}.", completedJobId,
                partAsPlannedValidity);

        SupplyChainImpacted partSiteInformationAsPlannedValidity;
        try {
            partSiteInformationAsPlannedValidity = validatePartSiteInformationAsPlanned(investigationJob, completedJob);
        } catch (final ValidationException e) {
            completedJob = createTombstone(e, completedJob);
            partSiteInformationAsPlannedValidity = SupplyChainImpacted.UNKNOWN;
        }
        log.info("Local validation of PartSiteInformationAsPlanned Validity was done for job {}. with result {}.",
                completedJobId, partSiteInformationAsPlannedValidity);

        final SupplyChainImpacted supplyChainImpacted = partAsPlannedValidity.or(partSiteInformationAsPlannedValidity);
        log.debug("Supply Chain Validity result of {} and {} resulted in {}", partAsPlannedValidity,
                partSiteInformationAsPlannedValidity, supplyChainImpacted);
        return new InvestigationResult(completedJob, supplyChainImpacted);
    }

    private static SupplyChainImpacted validatePartSiteInformationAsPlanned(final BpnInvestigationJob investigationJob,
            final Jobs completedJob) throws AspectTypeNotFoundException, InvalidAspectTypeFormatException {
        final PartSiteInformationAsPlanned partSiteInformation = getPartSiteInformationAsPlanned(completedJob);
        return BPNIncidentValidation.jobContainsIncidentBPNSs(partSiteInformation, investigationJob.getIncidentBpns());
    }

    private static SupplyChainImpacted validatePartAsPlanned(final Jobs completedJob)
            throws AspectTypeNotFoundException {
        final PartAsPlanned partAsPlanned = getPartAsPlanned(completedJob);
        return BPNIncidentValidation.partAsPlannedValidity(partAsPlanned);
    }

    private static PartSiteInformationAsPlanned getPartSiteInformationAsPlanned(final Jobs job)
            throws AspectTypeNotFoundException {
        final String value = getAspectTypeFromJob(job, AspectType.PART_SITE_INFORMATION_AS_PLANNED);
        return StringMapper.mapFromString(value, PartSiteInformationAsPlanned.class);
    }

    private static PartAsPlanned getPartAsPlanned(final Jobs job) throws AspectTypeNotFoundException {
        final String value = getAspectTypeFromJob(job, AspectType.PART_AS_PLANNED);
        return StringMapper.mapFromString(value, PartAsPlanned.class);
    }

    private static String getAspectTypeFromJob(final Jobs job, final AspectType aspectType)
            throws AspectTypeNotFoundException {
        log.debug("Searching for AspectType '{}'", aspectType.toString());
        return StringMapper.mapToString(job.getSubmodels()
                                           .stream()
                                           .filter(submodel -> submodel.getAspectType().endsWith(aspectType.toString()))
                                           .findFirst()
                                           .orElseThrow(() -> new AspectTypeNotFoundException(
                                                   "AspectType '%s' not found in Job.".formatted(
                                                           aspectType.toString())))
                                           .getPayload());
    }

    private static Jobs createTombstone(final ValidationException exception, final Jobs completedJob) {
        log.warn("Validation failed. {}", exception.getMessage());
        final Tombstone tombstone = Tombstone.from(completedJob.getJob().getGlobalAssetId().getGlobalAssetId(), null, exception,
                0, ProcessStep.ESS_VALIDATION);
        return completedJob.toBuilder().tombstone(tombstone).build();
    }
}
