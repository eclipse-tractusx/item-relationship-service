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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.ess.bpn.validation.BPNIncidentValidation;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryFacade;
import org.eclipse.tractusx.ess.irs.IrsFacade;
import org.eclipse.tractusx.irs.common.JobProcessingFinishedEvent;
import org.eclipse.tractusx.irs.component.Jobs;
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
class JobProcessingEventListener {

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

            SupplyChainImpacted supplyChain = BPNIncidentValidation.jobContainsIncidentBPNs(completedJob.getShells(),
                    investigationJob.getIncidentBpns());

            final BpnInvestigationJob investigationJobUpdate = investigationJob.update(completedJob, supplyChain);
            if (supplyChainIsNotImpacted(supplyChain)) {
                supplyChain = SupplyChainImpacted.UNKNOWN;
                log.info("Send notifications"); // TODO

                List<String> bpns = List.of();
                bpns.forEach(
                        bpn -> {
                            List<String> notificationIds = sendEdcNotifications(bpn);
                            investigationJobUpdate.withNotifications(notificationIds);
                        }
                );
            }

            bpnInvestigationJobCache.store(completedJobId, investigationJobUpdate);
        });
    }

    /**
     * TODO
     * @param bpn number
     * @returnreturn notification ids
     */
    private List<String> sendEdcNotifications(final String bpn) {
        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl(bpn);
        edcBaseUrl.ifPresentOrElse(
                edcUrl -> {
                    try {
                        edcSubmodelFacade.sendNotification(edcUrl, EdcNotification.builder().build());
                    } catch (EdcClientException e) {
                        e.printStackTrace();
                    }
                },
                () -> { });
        return List.of();
    }

    private boolean supplyChainIsNotImpacted(final SupplyChainImpacted supplyChain) {
        return supplyChain.equals(SupplyChainImpacted.NO);
    }

}
