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

import java.util.Optional;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.job.RequestMetric;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.springframework.web.client.RestClientException;

/**
 * Builds bpns array for AAShell from previous steps.
 * To build bpns Business Partner service is called.
 */
@Slf4j
public class BpdmDelegate extends AbstractDelegate {

    private static final Pattern BPN_RGX = Pattern.compile("(BPN)[LSA][\\w\\d]{10}[\\w\\d]{2}");

    private final BpdmFacade bpdmFacade;

    public BpdmDelegate(final AbstractDelegate nextStep, final BpdmFacade bpdmFacade) {
        super(nextStep);
        this.bpdmFacade = bpdmFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final JobParameter jobData, final AASTransferProcess aasTransferProcess, final String itemId) {
        final RequestMetric requestMetric = new RequestMetric();
        requestMetric.setType(RequestMetric.RequestType.BPDM);

        try {
            itemContainerBuilder.build()
                                .getShells()
                                .stream()
                                .findFirst()
                                .ifPresent(
                                        shell -> lookupBPN(itemContainerBuilder, itemId, shell, jobData.isLookupBPNs(),
                                                requestMetric));
        } catch (final RestClientException e) {
            log.info("Business Partner endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            requestMetric.incrementFailed();
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount, ProcessStep.BPDM_REQUEST))
                                .metric(requestMetric);
        }

        if (expectedDepthOfTreeIsNotReached(jobData.getDepth(), aasTransferProcess.getDepth())) {
            return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
        }

        // depth reached - stop processing
        return itemContainerBuilder.build();
    }

    private void lookupBPN(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId,
            final AssetAdministrationShellDescriptor shell, final boolean bpnLookupEnabled,
            final RequestMetric metric) {
        if (bpnLookupEnabled) {
            log.debug("BPN Lookup enabled, collecting BPN information");
            shell.findManufacturerId()
                 .ifPresentOrElse(
                         manufacturerId -> bpnFromManufacturerId(itemContainerBuilder, manufacturerId, itemId, metric),
                         () -> {
                             final String message = String.format("Cannot find ManufacturerId for CatenaXId: %s",
                                     itemId);
                             log.warn(message);
                             metric.incrementFailed();
                             itemContainerBuilder.tombstone(
                                     Tombstone.from(itemId, null, new BpdmDelegateProcessingException(message), 0,
                                             ProcessStep.BPDM_REQUEST))
                                                 .metric(metric);
                         });
        } else {
            log.debug("BPN lookup disabled, no BPN information will be collected.");
        }
    }

    private void bpnFromManufacturerId(final ItemContainer.ItemContainerBuilder itemContainerBuilder,
            final String manufacturerId, final String itemId, final RequestMetric metric) {
        final Optional<String> manufacturerName = bpdmFacade.findManufacturerName(manufacturerId);
        manufacturerName.ifPresentOrElse(name -> {
            final Bpn bpn = Bpn.of(manufacturerId, name);
            if (BPN_RGX.matcher(bpn.getManufacturerId() + bpn.getManufacturerName()).find()) {
                metric.incrementCompleted();
                itemContainerBuilder.bpn(bpn).metric(metric);
            } else {
                final String message = String.format("BPN: \"%s\" for CatenaXId: %s is not valid.",
                        bpn.getManufacturerId() + bpn.getManufacturerName(), itemId);
                log.warn(message);
                metric.incrementFailed();
                itemContainerBuilder.tombstone(
                        Tombstone.from(itemId, null, new BpdmDelegateProcessingException(message), 0,
                                ProcessStep.BPDM_VALIDATION)).metric(metric);
            }
        }, () -> {
            final String message = String.format("BPN not exist for given ManufacturerId: %s and for CatenaXId: %s.",
                    manufacturerId, itemId);
            log.warn(message);
            metric.incrementFailed();
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, new BpdmDelegateProcessingException(message), 0,
                    ProcessStep.BPDM_REQUEST)).metric(metric);
        });
    }

    private boolean expectedDepthOfTreeIsNotReached(final int expectedDepth, final int currentDepth) {
        log.info("Expected tree depth is {}, current depth is {}", expectedDepth, currentDepth);
        return currentDepth < expectedDepth;
    }

}
