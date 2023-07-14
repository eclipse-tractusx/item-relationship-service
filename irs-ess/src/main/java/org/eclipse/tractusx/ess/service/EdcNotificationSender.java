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

import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Helper service to send EDC notification on recursive flow
 */
@Service
@Slf4j
public class EdcNotificationSender {

    private final EdcSubmodelFacade edcSubmodelFacade;
    private final String localBpn;
    private final String essLocalEdcEndpoint;
    private final ConnectorEndpointsService connectorEndpointsService;

    public EdcNotificationSender(final EdcSubmodelFacade edcSubmodelFacade,
            @Value("${ess.localBpn}") final String localBpn, @Value("${ess.localEdcEndpoint}") final String essLocalEdcEndpoint,
            final ConnectorEndpointsService connectorEndpointsService) {
        this.edcSubmodelFacade = edcSubmodelFacade;
        this.localBpn = localBpn;
        this.essLocalEdcEndpoint = essLocalEdcEndpoint;
        this.connectorEndpointsService = connectorEndpointsService;
    }

    public void sendEdcNotification(final EdcNotification originalEdcNotification, final SupplyChainImpacted supplyChainImpacted) {
        final String notificationId = UUID.randomUUID().toString();
        final String originalNotificationId = originalEdcNotification.getHeader().getNotificationId();
        final String recipientBpn = originalEdcNotification.getHeader().getSenderBpn();
        connectorEndpointsService.fetchConnectorEndpoints(recipientBpn).forEach(connectorEndpoint -> {
            try {
                log.info("Edc Notification will be send to connector endpoint: {}", connectorEndpoint);
                final Map<String, Object> notificationContent = Map.of("result", supplyChainImpacted.getDescription());
                final EdcNotification edcRequest = edcRequest(notificationId, originalNotificationId,
                        essLocalEdcEndpoint, localBpn, recipientBpn, notificationContent);

                final var response = edcSubmodelFacade.sendNotification(connectorEndpoint, "ess-response-asset", edcRequest);
                if (!response.deliveredSuccessfully()) {
                    throw new EdcClientException("EDC Provider did not accept message with notificationId " + notificationId);
                }
                log.info("Successfully sent response notification.");
            } catch (EdcClientException exception) {
                log.error("Cannot send edc notification", exception);
            }
        });

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
