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
package org.eclipse.tractusx.ess.notification.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.EdcSubmodelFacade;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.edc.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.ess.discovery.EdcDiscoveryMockConfig;
import org.eclipse.tractusx.ess.service.EssService;
import org.eclipse.tractusx.ess.service.SupplyChainImpacted;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Endpoint for receiving EDC notifications and responding with Mocked BPN results.
 */
@RestController
@RequestMapping("/ess/mock/notification")
@RequiredArgsConstructor
@Validated
@Hidden
@Slf4j
public class MockedNotificationReceiverEndpoint {

    private final EdcDiscoveryMockConfig edcDiscoveryMockConfig;
    private final EdcSubmodelFacade edcSubmodelFacade;

    private final EssService essService;

    @PostMapping("/receive")
    public void receiveNotification(final @Valid @RequestBody EdcNotification notification) throws EdcClientException {
        log.info("receiveNotification mock called");
        final Optional<String> incidentBpn = Optional.ofNullable(notification.getContent().get("incidentBpn"))
                                                     .map(Object::toString);

        if (incidentBpn.isPresent()) {
            final String bpn = incidentBpn.get();
            log.info("Received notification request on mock with id {} and incidentBpn {}",
                    notification.getHeader().getNotificationId(), bpn);

            if (edcDiscoveryMockConfig.getMockEdcResult().containsKey(bpn)) {
                final SupplyChainImpacted supplyChainImpacted = edcDiscoveryMockConfig.getMockEdcResult().get(bpn);

                final String notificationId = UUID.randomUUID().toString();
                final String originalNotificationId = notification.getHeader().getNotificationId();
                final String senderEdc = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
                final String senderBpn = notification.getHeader().getRecipientBpn();
                final String recipientBpn = notification.getHeader().getSenderBpn();
                final String recipientUrl = notification.getHeader().getSenderEdc();
                final Map<String, Object> notificationContent = Map.of("result", supplyChainImpacted.getDescription());

                final EdcNotification edcRequest = edcRequest(notificationId, originalNotificationId, senderEdc,
                        senderBpn, recipientBpn, notificationContent);

                final var response = edcSubmodelFacade.sendNotification(recipientUrl, "ess-response-asset", edcRequest);
                if (!response.deliveredSuccessfully()) {
                    throw new EdcClientException(
                            "EDC Provider did not accept message with notificationId " + notificationId);
                }
                log.info("Successfully sent response notification.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BPN " + bpn + " not handled.");
            }

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Malformed EDC Notification - content without incidentBPN key.");
        }
    }

    @PostMapping("/receive-recursive")
    public void receiveRecursiveNotification(final @Valid @RequestBody EdcNotification notification) throws EdcClientException {
        log.info("receive recursive Notification mock called");
        final Optional<String> incidentBpn = Optional.ofNullable(notification.getContent().get("incidentBpn"))
                                                     .map(Object::toString);

        Optional<Object> concernedCatenaXIdsNotification = Optional.ofNullable(notification.getContent().get("concernedCatenaXIds"));

        if (concernedCatenaXIdsNotification.isPresent() && concernedCatenaXIdsNotification.get() instanceof List && incidentBpn.isPresent()) {
            final String bpn = incidentBpn.get();

            if (edcDiscoveryMockConfig.getMockEdcResult().containsKey(bpn)) {
                final SupplyChainImpacted supplyChainImpacted = edcDiscoveryMockConfig.getMockEdcResult().get(bpn);
                if (SupplyChainImpacted.NO.equals(supplyChainImpacted)) {
                    final List<String> concernedCatenaXIds = getConcernedCatenaXIds(concernedCatenaXIdsNotification);

                    List<UUID> createdJobs = concernedCatenaXIds.stream()
                                                                .map(catenaXId -> essService.startIrsJob(
                                                                        RegisterBpnInvestigationJob.builder()
                                                                                                   .incidentBpns(
                                                                                                           List.of(bpn))
                                                                                                   .globalAssetId(
                                                                                                           catenaXId)
                                                                                                   .build()))
                                                                .map(JobHandle::getId)
                                                                .toList();
                }
            }
        }
    }

    @NotNull
    private static List<String> getConcernedCatenaXIds(final Optional<Object> concernedCatenaXIdsNotification) {
        return ((List<String>) concernedCatenaXIdsNotification.get()).stream().collect(Collectors.toList());
    }

    private EdcNotification edcRequest(final String notificationId, final String originalId, final String senderEdc,
            final String senderBpn, final String recipientBpn, final Map<String, Object> content) {
        final EdcNotificationHeader header = EdcNotificationHeader.builder()
                                                                  .notificationId(notificationId)
                                                                  .senderEdc(senderEdc)
                                                                  .senderBpn(senderBpn)
                                                                  .recipientBpn(recipientBpn)
                                                                  .originalNotificationId(originalId)
                                                                  .replyAssetId("")
                                                                  .replyAssetSubPath("")
                                                                  .notificationType("ess-supplier-response")
                                                                  .build();

        return EdcNotification.builder().header(header).content(content).build();
    }

}
