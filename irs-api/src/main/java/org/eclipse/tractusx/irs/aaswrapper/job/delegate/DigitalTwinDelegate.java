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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.github.resilience4j.core.functions.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.common.ExceptionUtils;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;

/**
 * Retrieves AAShell from Digital Twin Registry service and storing it inside {@link ItemContainer}.
 * This shell is later used in further processing by other delegates.
 */
@Slf4j
public class DigitalTwinDelegate extends AbstractDelegate {

    private final DigitalTwinRegistryService digitalTwinRegistryService;

    public DigitalTwinDelegate(final AbstractDelegate nextStep,
            final DigitalTwinRegistryService digitalTwinRegistryService) {
        super(nextStep);
        this.digitalTwinRegistryService = digitalTwinRegistryService;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final JobParameter jobData, final AASTransferProcess aasTransferProcess,
            final PartChainIdentificationKey itemId) {

        if (StringUtils.isBlank(itemId.getBpn())) {
            log.warn("Could not process item with id {} because no BPN was provided. Creating Tombstone.",
                    itemId.getGlobalAssetId());
            return itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), null, "Can't get relationship without a BPN", 0,
                            ProcessStep.DIGITAL_TWIN_REQUEST)).build();
        }

        try {
            final var dtrKeys = List.of(new DigitalTwinRegistryKey(itemId.getGlobalAssetId(), itemId.getBpn()));
            final var eithers = digitalTwinRegistryService.fetchShells(dtrKeys);
            final var shell = eithers.stream()
                                     // we use findFirst here,  because we query only for one
                                     // DigitalTwinRegistryKey here
                                     .map(Either::getOrNull)
                                     .filter(Objects::nonNull)
                                     .findFirst()
                                     .orElseThrow(() -> shellNotFound(eithers));

            itemContainerBuilder.shell(
                    jobData.isAuditContractNegotiation() ? shell : shell.withoutContractAgreementId());
        } catch (final RegistryServiceException | RuntimeException e) {
            // catching generic exception is intended here,
            // otherwise Jobs stay in state RUNNING forever
            log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(
                    Tombstone.from(itemId.getGlobalAssetId(), null, e, e.getSuppressed(), retryCount,
                            ProcessStep.DIGITAL_TWIN_REQUEST));
        }

        if (expectedDepthOfTreeIsNotReached(jobData.getDepth(), aasTransferProcess.getDepth())) {
            return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
        }

        // depth reached - stop processing
        return itemContainerBuilder.build();
    }

    private static RegistryServiceException shellNotFound(final Collection<Either<Exception, Shell>> eithers) {
        final RegistryServiceException shellNotFound = new RegistryServiceException("Shell not found");
        ExceptionUtils.addSuppressedExceptions(eithers, shellNotFound);
        return shellNotFound;
    }

    private boolean expectedDepthOfTreeIsNotReached(final int expectedDepth, final int currentDepth) {
        log.info("Expected tree depth is {}, current depth is {}", expectedDepth, currentDepth);
        return currentDepth < expectedDepth;
    }

}
