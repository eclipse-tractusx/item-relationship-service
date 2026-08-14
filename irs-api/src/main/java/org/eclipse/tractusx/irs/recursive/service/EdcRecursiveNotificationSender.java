/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.service;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcOrchestrator;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.util.UriPathJoiner;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationDeliveryFailureReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;

/**
 * Sends recursive IRS notifications through the EDC contract flow.
 */
@Slf4j
@RequiredArgsConstructor
public class EdcRecursiveNotificationSender implements RecursiveNotificationSender {

    /* package */ static final String DCT_TYPE_ID = "'" + JsonLdConfiguration.NAMESPACE_DCT + "type'.'@id'";
    /* package */ static final String NOTIFICATION_API_TYPE =
            JsonLdConfiguration.NAMESPACE_CX_TAXONOMY + "RecursiveIrsNotificationApi";
    /* package */ static final String API_VERSION_PROPERTY = JsonLdConfiguration.NAMESPACE_CX_ONTOLOGY + "version";
    /* package */ static final String NOTIFICATION_API_VERSION = "1.0";

    private final ConnectorEndpointsService connectorEndpointsService;
    private final EdcConfiguration edcConfiguration;
    private final EdcOrchestrator edcOrchestrator;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final PolicyCheckerService policyCheckerService;

    @Override
    public void sendRequest(final String receiverBpnl, final RecursiveNotificationMessage message) {
        send(receiverBpnl, message, RecursiveNotificationType.REQUEST);
    }

    @Override
    public void sendResponse(final String receiverBpnl, final RecursiveNotificationMessage message) {
        send(receiverBpnl, message, RecursiveNotificationType.RESPONSE);
    }

    private void send(final String receiverBpnl, final RecursiveNotificationMessage message,
            final RecursiveNotificationType notificationType) {
        final String errorRef = UUID.randomUUID().toString();
        final List<String> connectorEndpoints;
        try {
            connectorEndpoints = RecursiveEdcTargets.deterministicOrder(
                    connectorEndpointsService.fetchConnectorEndpoints(receiverBpnl));
        } catch (final RuntimeException exception) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.CONNECTOR_DISCOVERY_FAILED, errorRef,
                    notificationType, receiverBpnl, "discovery", exception);
        }
        if (connectorEndpoints.isEmpty()) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.NO_CONNECTOR_ENDPOINT, errorRef,
                    notificationType, receiverBpnl, "discovery", null);
        }

        RecursiveNotificationDeliveryException lastFailure = null;
        for (final String connectorEndpoint : connectorEndpoints) {
            try {
                log.info("Sending {} notification to {} via EDC connector {} (errorRef on failure: {})",
                        notificationType, RecursiveLogValue.of(receiverBpnl), RecursiveLogValue.of(connectorEndpoint),
                        errorRef);
                sendViaConnector(connectorEndpoint, receiverBpnl, message, notificationType, errorRef);
                return;
            } catch (final RecursiveNotificationDeliveryException exception) {
                lastFailure = exception;
            }
        }
        throw lastFailure;
    }

    /**
     * Runs the four delivery steps against one connector and classifies the first failing step.
     * The detailed log lines (partner, endpoint, step) are local-only diagnostics; the thrown
     * exception carries just the reason code and errorRef.
     */
    private void sendViaConnector(final String connectorEndpoint, final String receiverBpnl,
            final RecursiveNotificationMessage message, final RecursiveNotificationType notificationType,
            final String errorRef) {
        final String dspEndpointAddress;
        try {
            dspEndpointAddress = appendProviderSuffix(connectorEndpoint);
        } catch (final URISyntaxException exception) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.CONNECTOR_ENDPOINT_INVALID, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, exception);
        }

        final List<CatalogItem> catalogItems;
        try {
            catalogItems = edcOrchestrator.getCatalogItems(dspEndpointAddress, notificationAssetQuery(), receiverBpnl);
        } catch (final EdcClientException exception) {
            throw deliveryFailure(classifyCatalogFailure(exception), errorRef, notificationType, receiverBpnl,
                    connectorEndpoint, exception);
        }
        final CatalogItem catalogItem = selectCatalogItem(catalogItems, errorRef, notificationType, receiverBpnl,
                connectorEndpoint);

        final EndpointDataReference endpointDataReference;
        try {
            endpointDataReference = edcOrchestrator.getEndpointDataReference(dspEndpointAddress, catalogItem)
                    .get(edcConfiguration.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (final EdcClientException | InterruptedException | ExecutionException | TimeoutException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.CONTRACT_NEGOTIATION_FAILED, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, exception);
        }

        final EdcNotificationResponse response;
        try {
            response = edcDataPlaneClient.sendData(endpointDataReference, message);
        } catch (final RuntimeException exception) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.EDC_NOTIFICATION_FAILED, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, exception);
        }
        if (!response.deliveredSuccessfully()) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.DATA_PLANE_DELIVERY_FAILED, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, null);
        }
        log.info("{} notification delivered to {} via EDC asset {}", notificationType,
                RecursiveLogValue.of(receiverBpnl), RecursiveLogValue.of(catalogItem.getItemId()));
    }

    private RecursiveNotificationDeliveryFailureReason classifyCatalogFailure(final EdcClientException exception) {
        if (RecursiveFailureReasonMapper.policyReason(exception).isPresent()) {
            return RecursiveNotificationDeliveryFailureReason.NOTIFICATION_POLICY_REJECTED;
        }
        return RecursiveNotificationDeliveryFailureReason.CATALOG_REQUEST_FAILED;
    }

    private CatalogItem selectCatalogItem(final List<CatalogItem> catalogItems, final String errorRef,
            final RecursiveNotificationType notificationType, final String receiverBpnl,
            final String connectorEndpoint) {
        if (catalogItems.isEmpty()) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.NOTIFICATION_ASSET_NOT_FOUND, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, null);
        }
        final List<CatalogItem> completeCatalogItems = catalogItems.stream()
                .filter(this::isCompleteCatalogItem)
                .toList();
        if (completeCatalogItems.isEmpty()) {
            throw deliveryFailure(RecursiveNotificationDeliveryFailureReason.CATALOG_REQUEST_FAILED, errorRef,
                    notificationType, receiverBpnl, connectorEndpoint, null);
        }
        return completeCatalogItems.stream()
                .filter(catalogItem -> policyCheckerService.isValid(catalogItem.getPolicy(),
                        catalogItem.getConnectorId())
                        && !policyCheckerService.isExpired(catalogItem.getPolicy(), catalogItem.getConnectorId()))
                .sorted(Comparator.comparing(CatalogItem::getAssetPropId)
                        .thenComparing(CatalogItem::getOfferId))
                .findFirst()
                .orElseThrow(() -> deliveryFailure(
                        RecursiveNotificationDeliveryFailureReason.NOTIFICATION_POLICY_REJECTED, errorRef,
                        notificationType, receiverBpnl, connectorEndpoint, null));
    }

    private boolean isCompleteCatalogItem(final CatalogItem catalogItem) {
        return catalogItem != null
                && StringUtils.isNotBlank(catalogItem.getItemId())
                && StringUtils.isNotBlank(catalogItem.getAssetPropId())
                && StringUtils.isNotBlank(catalogItem.getOfferId())
                && catalogItem.getPolicy() != null
                && StringUtils.isNotBlank(catalogItem.getConnectorId());
    }

    private static QuerySpec notificationAssetQuery() {
        return QuerySpec.Builder.newInstance()
                                .filter(new Criterion(DCT_TYPE_ID, "=", NOTIFICATION_API_TYPE))
                                .filter(new Criterion(API_VERSION_PROPERTY, "=", NOTIFICATION_API_VERSION))
                                .build();
    }

    private RecursiveNotificationDeliveryException deliveryFailure(
            final RecursiveNotificationDeliveryFailureReason reason, final String errorRef,
            final RecursiveNotificationType notificationType, final String receiverBpnl,
            final String connectorEndpoint,
            final Exception cause) {
        if (cause == null) {
            log.error("Recursive {} notification delivery failed: errorRef={} reason={} partner={} endpoint={} cause=-",
                    notificationType, errorRef, reason, RecursiveLogValue.of(receiverBpnl),
                    RecursiveLogValue.of(connectorEndpoint));
        } else {
            log.error("Recursive {} notification delivery failed: errorRef={} reason={} partner={} endpoint={} "
                            + "causeType={}",
                    notificationType, errorRef, reason, RecursiveLogValue.of(receiverBpnl),
                    RecursiveLogValue.of(connectorEndpoint), cause.getClass().getName());
        }
        return new RecursiveNotificationDeliveryException(reason, errorRef,
                "Failed to send " + notificationType + " notification (reason=" + reason + ", errorRef=" + errorRef
                        + ")",
                cause);
    }

    private String appendProviderSuffix(final String connectorEndpoint) throws URISyntaxException {
        final String providerSuffix = edcConfiguration.getControlplane().getProviderSuffix();
        if (connectorEndpoint.endsWith(providerSuffix)) {
            return connectorEndpoint;
        }
        return UriPathJoiner.appendPath(connectorEndpoint, providerSuffix);
    }
}
