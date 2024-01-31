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
package org.eclipse.tractusx.irs.ess.controller.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.edc.client.model.notification.ResponseNotificationContent;
import org.eclipse.tractusx.irs.ess.discovery.EdcDiscoveryMockConfig;
import org.eclipse.tractusx.irs.ess.service.SupplyChainImpacted;
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
public class MockedNotificationReceiverController {

    private final EdcDiscoveryMockConfig edcDiscoveryMockConfig;
    private final EdcSubmodelFacade edcSubmodelFacade;

    @NotNull
    private static Optional<List<String>> getIncidentBPNSs(
            final EdcNotification<InvestigationNotificationContent> notification) {
        final InvestigationNotificationContent content = Optional.ofNullable(notification.getContent())
                                                                 .orElseThrow(() -> new ResponseStatusException(
                                                                         HttpStatus.BAD_REQUEST,
                                                                         "Malformed EDC Notification - no content is null."));
        return Optional.ofNullable(content.getIncidentBPNSs());
    }

    @PostMapping("/receive")
    public void receiveNotification(
            final @Valid @RequestBody EdcNotification<InvestigationNotificationContent> notification)
            throws EdcClientException {
        log.info("receiveNotification mock called");
        final List<String> incidentBPNSs = getIncidentBPNSs(notification).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Malformed EDC Notification - content without incidentBPN key."));
        log.info("Received notification request on mock with id {} and incidentBPNSs {}",
                notification.getHeader().getNotificationId(), incidentBPNSs);

        final Map<String, SupplyChainImpacted> mockEdcResult = edcDiscoveryMockConfig.getMockEdcResult();
        if (incidentBPNSs.stream().anyMatch(mockEdcResult::containsKey)) {
            final List<SupplyChainImpacted> list = incidentBPNSs.stream().map(mockEdcResult::get).toList();
            final SupplyChainImpacted supplyChainImpacted = list.stream()
                                                                .reduce(SupplyChainImpacted::or)
                                                                .orElse(SupplyChainImpacted.UNKNOWN);

            final String notificationId = UUID.randomUUID().toString();
            final String originalNotificationId = notification.getHeader().getNotificationId();
            final String senderEdc = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
            final String senderBpn = notification.getHeader().getRecipientBpn();
            final String recipientBpn = notification.getHeader().getSenderBpn();
            final String recipientUrl = notification.getHeader().getSenderEdc();

            final NotificationContent notificationContent = ResponseNotificationContent.builder()
                                                                                       .result(supplyChainImpacted.getDescription())
                                                                                       .build();

            final EdcNotification<NotificationContent> edcRequest = edcRequest(notificationId, originalNotificationId,
                    senderEdc, senderBpn, recipientBpn, notificationContent);

            final var response = edcSubmodelFacade.sendNotification(recipientUrl, "ess-response-asset", edcRequest);
            if (!response.deliveredSuccessfully()) {
                throw new EdcClientException(
                        "EDC Provider did not accept message with notificationId " + notificationId);
            }
            log.info("Successfully sent response notification.");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BPN " + incidentBPNSs + " not handled.");
        }

    }

    private EdcNotification<NotificationContent> edcRequest(final String notificationId, final String originalId,
            final String senderEdc, final String senderBpn, final String recipientBpn,
            final NotificationContent content) {
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
