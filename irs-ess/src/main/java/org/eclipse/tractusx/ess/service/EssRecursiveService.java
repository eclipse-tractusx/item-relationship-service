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
package org.eclipse.tractusx.ess.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Business logic for create recursive investigations
 */
@Service
@Slf4j
public class EssRecursiveService {

    private final EssService essService;
    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache;
    private final String localBpn;
    private final EdcNotificationSender edcNotificationSender;

    /* package */ EssRecursiveService(final EssService essService,
            final RelatedInvestigationJobsCache relatedInvestigationJobsCache,
            @Value("${ess.localBpn}") final String localBpn, final EdcNotificationSender edcNotificationSender) {
        this.essService = essService;
        this.relatedInvestigationJobsCache = relatedInvestigationJobsCache;
        this.localBpn = localBpn;
        this.edcNotificationSender = edcNotificationSender;
    }

    public void handleNotification(final EdcNotification notification) {

        final Optional<String> incidentBpn = Optional.ofNullable(notification.getContent().get("incidentBpn"))
                                                     .map(Object::toString);

        final Optional<Object> concernedCatenaXIdsNotification = Optional.ofNullable(
                notification.getContent().get("concernedCatenaXIds"));

        if (incidentBpn.isPresent() && localBpn.equals(incidentBpn.get())) {
            edcNotificationSender.sendEdcNotification(notification, SupplyChainImpacted.YES);
        } else if (concernedCatenaXIdsNotification.isPresent() && concernedCatenaXIdsNotification.get() instanceof List
                && incidentBpn.isPresent()) {
            final String bpn = incidentBpn.get();
            final List<String> concernedCatenaXIds = getConcernedCatenaXIds(concernedCatenaXIdsNotification);

            final List<UUID> createdJobs = concernedCatenaXIds.stream()
                                                              .map(catenaXId -> startIrsJob(bpn, catenaXId))
                                                              .map(JobHandle::getId)
                                                              .toList();
            relatedInvestigationJobsCache.store(notification.getHeader().getNotificationId(),
                    new RelatedInvestigationJobs(notification, createdJobs));
        }
    }

    private JobHandle startIrsJob(final String bpn, final String catenaXId) {
        final var job = RegisterBpnInvestigationJob.builder()
                                                   .incidentBpns(List.of(bpn))
                                                   .key(PartChainIdentificationKey.builder()
                                                                                  .globalAssetId(catenaXId)
                                                                                  .bpn(localBpn)
                                                                                  .build())
                                                   .build();

        return essService.startIrsJob(job);
    }

    @NotNull
    private static List<String> getConcernedCatenaXIds(final Optional<Object> concernedCatenaXIdsNotification) {
        final List<String> concernedCatenaXIds = new ArrayList<>();
        concernedCatenaXIdsNotification.ifPresent(
                list -> concernedCatenaXIds.addAll(((List<String>) list).stream().toList()));
        return concernedCatenaXIds;
    }
}
