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
package org.eclipse.tractusx.ess.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.edc.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.ess.bpn.validation.BPNIncidentValidation;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Listens for {@link JobProcessingFinishedEvent} and calling callbackUrl with notification.
 * Execution is done in a separate thread.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class InvestigationJobProcessingEventListener {

    private final IrsFacade irsFacade;
    private final EdcDiscoveryFacade edcDiscoveryFacade;
    private final EdcSubmodelFacade edcSubmodelFacade;
    private final BpnInvestigationJobCache bpnInvestigationJobCache;

    @Async
    @EventListener
    public void handleJobProcessingFinishedEvent(final JobProcessingFinishedEvent jobProcessingFinishedEvent) {
        final UUID completedJobId = UUID.fromString(jobProcessingFinishedEvent.getJobId());
        final Optional<BpnInvestigationJob> bpnInvestigationJob = bpnInvestigationJobCache.findByJobId(completedJobId);

        bpnInvestigationJob.ifPresent(investigationJob -> {
            log.info("Job is completed. Starting SupplyChainImpacted processing for job {}.", completedJobId);

            final Jobs completedJob = irsFacade.getIrsJob(completedJobId.toString());

            final SupplyChainImpacted localSupplyChain = BPNIncidentValidation.jobContainsIncidentBPNs(
                    completedJob.getShells(), investigationJob.getIncidentBpns());
            log.info("Local validation of BPN was done for job {}. with result {}.", completedJobId, localSupplyChain);
            final BpnInvestigationJob investigationJobUpdate = investigationJob.update(completedJob, localSupplyChain);

            if (supplyChainIsNotImpacted(localSupplyChain)) {
                // Map<BPN, List<GlobalAssetID>>
                final Map<String, List<String>> bpns = getBPNsFromShells(completedJob.getShells());
                final Stream<Optional<String>> edcAddresses = bpns.keySet()
                                                                  .stream()
                                                                  .map(edcDiscoveryFacade::getEdcBaseUrl);

                if (thereIsUnresolvableEdcAddress(edcAddresses)) {
                    log.info(
                            "One of EDC address cant be resolved with DiscoveryService, updating SupplyChainImpacted to {}",
                            SupplyChainImpacted.UNKNOWN);
                    investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN);
                } else {
                    bpns.forEach((bpn, globalAssetIds) -> {
                        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl(bpn);
                        edcBaseUrl.ifPresentOrElse(url -> {
                            try {
                                final String notificationId = sendEdcNotification(bpn, url,
                                        investigationJobUpdate.getIncidentBpns(), globalAssetIds);
                                investigationJobUpdate.withNotifications(Collections.singletonList(notificationId));
                            } catch (final EdcClientException e) {
                                log.error("Exception during sending EDC notification.", e);
                                investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN);
                            }
                        }, () -> investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN));
                    });
                }
            }

            bpnInvestigationJobCache.store(completedJobId, investigationJobUpdate);
        });
    }

    private String sendEdcNotification(final String bpn, final String url, final List<String> incidentBpns,
            final List<String> globalAssetIds) throws EdcClientException {
        final String notificationId = UUID.randomUUID().toString();

        final var response = edcSubmodelFacade.sendNotification(url, "notify-request-asset",
                edcRequest(notificationId, url, bpn, incidentBpns, globalAssetIds));
        if (!response.deliveredSuccessfully()) {
            throw new EdcClientException("EDC Provider did not accept message with notificationId " + notificationId);
        }

        return notificationId;

    }

    private boolean thereIsUnresolvableEdcAddress(final Stream<Optional<String>> edcAddresses) {
        return !edcAddresses.filter(Optional::isEmpty).toList().isEmpty();
    }

    private EdcNotification edcRequest(final String notificationId, final String edcAddress, final String recipientBpn,
            final List<String> incidentBpns, final List<String> globalAssetIds) {
        final EdcNotificationHeader edcNotificationHeader = EdcNotificationHeader.builder()
                                                                                 .notificationId(notificationId)
                                                                                 .senderBpn(
                                                                                         "SystemBPNFromConfig") // TODO
                                                                                 .recipientBpn(recipientBpn)
                                                                                 .senderEdc(edcAddress)
                                                                                 .replyAssetId("ess-response-asset")
                                                                                 .replyAssetSubPath("")
                                                                                 .notificationType(
                                                                                         "ess-supplier-request")
                                                                                 .build();
        final Map<String, Object> edcNotificationContent = Map.of("incidentBpn", incidentBpns.get(0),
                "concernedCatenaXIds", globalAssetIds);

        return EdcNotification.builder().header(edcNotificationHeader).content(edcNotificationContent).build();
    }

    private static Map<String, List<String>> getBPNsFromShells(
            final List<AssetAdministrationShellDescriptor> shellDescriptors) {
        return shellDescriptors.stream()
                               .collect(Collectors.groupingBy(shell -> shell.findManufacturerId().orElseThrow(),
                                       Collectors.mapping(shell -> shell.getGlobalAssetId().getValue().get(0),
                                               Collectors.toList())));
    }

    private boolean supplyChainIsNotImpacted(final SupplyChainImpacted supplyChain) {
        return supplyChain.equals(SupplyChainImpacted.NO);
    }

}
