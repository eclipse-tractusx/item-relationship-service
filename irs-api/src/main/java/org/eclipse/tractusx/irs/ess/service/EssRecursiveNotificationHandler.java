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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business logic for handle recursive investigation and calculate if it is already done
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EssRecursiveNotificationHandler {

    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache;
    private final BpnInvestigationJobCache bpnInvestigationJobCache;
    private final EdcNotificationSender edcNotificationSender;

    /* package */ void handleNotification(final UUID finishedJobId, final SupplyChainImpacted supplyChainImpacted) {

        final Optional<RelatedInvestigationJobs> relatedJobsId = relatedInvestigationJobsCache.findByRecursiveRelatedJobId(
                finishedJobId);

        relatedJobsId.ifPresentOrElse(relatedJobs -> {
            if (SupplyChainImpacted.YES.equals(supplyChainImpacted)) {
                log.debug("SupplyChain is impacted. Sending notification back to requestor.");
                edcNotificationSender.sendEdcNotification(relatedJobs.originalNotification(), supplyChainImpacted);
                relatedInvestigationJobsCache.remove(
                        relatedJobs.originalNotification().getHeader().getNotificationId());
            } else {
                log.debug(
                        "SupplyChainImpacted in state '{}'. Waiting for Jobs to complete to send notification back to requestor.",
                        supplyChainImpacted);
                sendNotificationAfterAllCompleted(relatedJobs);
            }
        }, () -> log.debug("No RelatedInvestigationJob found for id '{}'.", finishedJobId));
    }

    private void sendNotificationAfterAllCompleted(final RelatedInvestigationJobs relatedInvestigationJobs) {
        final List<BpnInvestigationJob> allInvestigationJobs = relatedInvestigationJobs.recursiveRelatedJobIds()
                                                                                       .stream()
                                                                                       .map(bpnInvestigationJobCache::findByJobId)
                                                                                       .flatMap(Optional::stream)
                                                                                       .toList();
        if (checkAllFinished(allInvestigationJobs)) {
            final SupplyChainImpacted finalResult = allInvestigationJobs.stream()
                                                                        .map(BpnInvestigationJob::getSupplyChainImpacted)
                                                                        .flatMap(Optional::stream)
                                                                        .reduce(SupplyChainImpacted.NO,
                                                                                SupplyChainImpacted::or);
            edcNotificationSender.sendEdcNotification(relatedInvestigationJobs.originalNotification(), finalResult);
        }

    }

    private boolean checkAllFinished(final List<BpnInvestigationJob> allInvestigationJobs) {
        return allInvestigationJobs.stream()
                                   .allMatch(bpnInvestigationJob -> bpnInvestigationJob.getSupplyChainImpacted()
                                                                                       .isPresent());
    }
}
