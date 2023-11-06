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
package org.eclipse.tractusx.irs.ess.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Notification;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.ess.bpn.validation.IncidentValidation;
import org.eclipse.tractusx.irs.ess.bpn.validation.InvestigationResult;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Listens for {@link JobProcessingFinishedEvent} and calling callbackUrl with notification.
 * Execution is done in a separate thread.
 */
@Slf4j
@Service
@SuppressWarnings({ "PMD.TooManyMethods",
                    "PMD.ExcessiveImports"
})
class InvestigationJobProcessingEventListener {

    private final IrsItemGraphQueryService irsItemGraphQueryService;
    private final ConnectorEndpointsService connectorEndpointsService;
    private final EdcSubmodelFacade edcSubmodelFacade;
    private final BpnInvestigationJobCache bpnInvestigationJobCache;
    private final JobStore jobStore;
    private final String localBpn;
    private final String localEdcEndpoint;
    private final List<String> mockRecursiveEdcAssets;
    private final EssRecursiveNotificationHandler recursiveNotificationHandler;

    /* package */ InvestigationJobProcessingEventListener(final IrsItemGraphQueryService irsItemGraphQueryService,
            final ConnectorEndpointsService connectorEndpointsService, final EdcSubmodelFacade edcSubmodelFacade,
            final BpnInvestigationJobCache bpnInvestigationJobCache, final JobStore jobStore,
            @Value("${ess.localBpn}") final String localBpn,
            @Value("${ess.localEdcEndpoint}") final String localEdcEndpoint,
            @Value("${ess.discovery.mockRecursiveEdcAsset}") final List<String> mockRecursiveEdcAssets,
            final EssRecursiveNotificationHandler recursiveNotificationHandler) {
        this.irsItemGraphQueryService = irsItemGraphQueryService;
        this.connectorEndpointsService = connectorEndpointsService;
        this.edcSubmodelFacade = edcSubmodelFacade;
        this.bpnInvestigationJobCache = bpnInvestigationJobCache;
        this.jobStore = jobStore;
        this.localBpn = localBpn;
        this.localEdcEndpoint = localEdcEndpoint;
        this.mockRecursiveEdcAssets = mockRecursiveEdcAssets;
        this.recursiveNotificationHandler = recursiveNotificationHandler;
    }

    @Async
    @EventListener
    public void handleJobProcessingFinishedEvent(final JobProcessingFinishedEvent jobProcessingFinishedEvent) {
        final UUID completedJobId = UUID.fromString(jobProcessingFinishedEvent.jobId());
        final Optional<BpnInvestigationJob> bpnInvestigationJob = bpnInvestigationJobCache.findByJobId(completedJobId);

        bpnInvestigationJob.ifPresent(investigationJob -> {
            log.info("Job is completed. Starting SupplyChainImpacted processing for job {}.", completedJobId);

            final Optional<MultiTransferJob> multiTransferJob = jobStore.find(completedJobId.toString());
            multiTransferJob.ifPresent(job -> {
                final Jobs completedJob = irsItemGraphQueryService.getJobForJobId(job, false);

                final InvestigationResult investigationResult = IncidentValidation.getResult(investigationJob, completedJob,
                        completedJobId);

                final BpnInvestigationJob investigationJobUpdate = investigationJob.update(
                        investigationResult.completedJob(), investigationResult.supplyChainImpacted(), job.getJobParameter().getBpn());

                if (leafNodeIsReached(investigationResult.completedJob()) || supplyChainIsImpacted(
                        investigationResult.supplyChainImpacted())) {
                    bpnInvestigationJobCache.store(completedJobId, investigationJobUpdate.complete());
                    recursiveNotificationHandler.handleNotification(investigationJob.getJobSnapshot().getJob().getId(),
                            investigationResult.supplyChainImpacted(), job.getJobParameter().getBpn());
                } else {
                    triggerInvestigationOnNextLevel(investigationResult.completedJob(), investigationJobUpdate, job.getJobParameter().getBpn());
                    bpnInvestigationJobCache.store(completedJobId, investigationJobUpdate);
                }
            });

        });
    }

    private static Map<String, List<String>> getBPNsFromRelationships(final List<Relationship> relationships) {
        return relationships.stream()
                            .filter(relationship -> relationship.getBpn() != null)
                            .collect(Collectors.groupingBy(Relationship::getBpn, Collectors.mapping(
                                    relationship -> relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId(),
                                    Collectors.toList())));
    }

    @NotNull
    private static List<Map.Entry<String, List<String>>> getNotResolvedBPNs(
            final Map<String, List<String>> edcAddresses) {
        return edcAddresses.entrySet().stream().filter(t -> t.getValue().isEmpty()).toList();
    }

    private static boolean anyBpnIsMissingFromRelationship(final Jobs completedJob) {
        return completedJob.getRelationships().stream().anyMatch(relationship -> relationship.getBpn() == null);
    }

    private boolean leafNodeIsReached(final Jobs completedJob) {
        return completedJob.getRelationships().isEmpty() && completedJob.getTombstones().isEmpty();
    }

    private void triggerInvestigationOnNextLevel(final Jobs completedJob,
            final BpnInvestigationJob investigationJobUpdate, final String jobBpn) {
        log.debug("Triggering investigation on the next level.");
        if (anyBpnIsMissingFromRelationship(completedJob)) {
            log.error("One or more Relationship items did not contain a BPN.");
            investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN, jobBpn);
        }
        final Map<String, List<String>> bpns = getBPNsFromRelationships(completedJob.getRelationships());
        log.debug("Extracted BPNs '{}'", bpns);

        final HashMap<String, List<String>> resolvedBPNs = new HashMap<>();
        bpns.keySet().forEach(bpn -> resolvedBPNs.put(bpn, connectorEndpointsService.fetchConnectorEndpoints(bpn)));
        log.debug("Found Endpoints to BPNs '{}'", resolvedBPNs);

        if (thereIsUnresolvableEdcAddress(resolvedBPNs)) {
            final List<String> unresolvedBPNs = getNotResolvedBPNs(resolvedBPNs).stream()
                                                                                .map(Map.Entry::getKey)
                                                                                .toList();
            log.debug("BPNs '{}' could not be resolved to an EDC address using DiscoveryService.", unresolvedBPNs);
            log.info("Some EDC addresses could not be resolved with DiscoveryService. "
                    + "Updating SupplyChainImpacted to {}", SupplyChainImpacted.UNKNOWN);
            investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN, jobBpn);
            recursiveNotificationHandler.handleNotification(investigationJobUpdate.getJobSnapshot().getJob().getId(),
                    SupplyChainImpacted.UNKNOWN, jobBpn);
        } else if (resolvedBPNs.isEmpty()) {
            log.info("No BPNs could not be found. Updating SupplyChainImpacted to {}", SupplyChainImpacted.UNKNOWN);
            investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN, jobBpn);
            recursiveNotificationHandler.handleNotification(investigationJobUpdate.getJobSnapshot().getJob().getId(),
                    SupplyChainImpacted.UNKNOWN, jobBpn);
        } else {
            log.debug("Sending notification for BPNs '{}'", bpns);
            sendNotifications(completedJob, investigationJobUpdate, bpns);
        }
    }

    private void sendNotifications(final Jobs completedJob, final BpnInvestigationJob investigationJobUpdate,
            final Map<String, List<String>> bpns) {
        bpns.forEach((bpn, globalAssetIds) -> {
            final List<String> edcBaseUrl = connectorEndpointsService.fetchConnectorEndpoints(bpn);
            if (edcBaseUrl.isEmpty()) {
                log.warn("No EDC URL found for BPN '{}'. Setting investigation result to '{}'", bpn,
                        SupplyChainImpacted.UNKNOWN);
                investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN, bpn);
            }
            edcBaseUrl.forEach(url -> {
                try {
                    final String notificationId = sendEdcNotification(bpn, url,
                            investigationJobUpdate.getIncidentBpns(), globalAssetIds);
                    investigationJobUpdate.withUnansweredNotifications(Collections.singletonList(new Notification(notificationId, bpn)));
                } catch (final EdcClientException e) {
                    log.error("Exception during sending EDC notification.", e);
                    investigationJobUpdate.update(completedJob, SupplyChainImpacted.UNKNOWN, bpn);
                }
            });
        });
    }

    private String sendEdcNotification(final String bpn, final String url, final List<String> incidentBpns,
            final List<String> globalAssetIds) throws EdcClientException {
        final String notificationId = UUID.randomUUID().toString();

        final boolean isRecursiveMockAsset = mockRecursiveEdcAssets.contains(bpn);
        final boolean isNotMockAsset = mockRecursiveEdcAssets.isEmpty();
        final EdcNotification<NotificationContent> notification = edcRequest(notificationId, bpn, incidentBpns,
                globalAssetIds);
        log.debug("Sending Notification '{}'", notification);
        final EdcNotificationResponse response;
        if (isRecursiveMockAsset || isNotMockAsset) {
            log.debug("Sending recursive notification");
            response = edcSubmodelFacade.sendNotification(url, "notify-request-asset-recursive", notification);
        } else {
            log.debug("Sending mock recursive notification");
            response = edcSubmodelFacade.sendNotification(url, "notify-request-asset", notification);
        }
        if (response.deliveredSuccessfully()) {
            log.info("Successfully sent notification with id '{}' to EDC endpoint '{}'.", notificationId, url);
        } else {
            throw new EdcClientException("EDC Provider did not accept message with notificationId " + notificationId);
        }

        return notificationId;
    }

    private boolean thereIsUnresolvableEdcAddress(final Map<String, List<String>> edcAddresses) {
        return !getNotResolvedBPNs(edcAddresses).isEmpty();
    }

    private EdcNotification<NotificationContent> edcRequest(final String notificationId, final String recipientBpn,
            final List<String> incidentBpns, final List<String> globalAssetIds) {
        final var header = EdcNotificationHeader.builder()
                                                .notificationId(notificationId)
                                                .recipientBpn(recipientBpn)
                                                .senderBpn(localBpn)
                                                .senderEdc(localEdcEndpoint)
                                                .replyAssetId("ess-response-asset")
                                                .replyAssetSubPath("")
                                                .notificationType("ess-supplier-request")
                                                .build();
        final NotificationContent content = InvestigationNotificationContent.builder()
                                                                            .concernedCatenaXIds(globalAssetIds)
                                                                            .incidentBPNSs(incidentBpns)
                                                                            .build();

        return EdcNotification.builder().header(header).content(content).build();
    }

    private boolean supplyChainIsImpacted(final SupplyChainImpacted supplyChain) {
        return !SupplyChainImpacted.NO.equals(supplyChain);
    }

}
