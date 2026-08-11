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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.service;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.recursive.RecursiveTestConstants.REMOTE_BPNL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcOrchestrator;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationDeliveryFailureReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcRecursiveNotificationSenderTest {

    private static final String NOTIFICATION_ASSET_ID = "provider-recursive-notification-api";
    private static final String FIRST_CONNECTOR = "http://connector-one";
    private static final String SECOND_CONNECTOR = "http://connector-two";
    private static final String FIRST_DSP_ENDPOINT = FIRST_CONNECTOR + "/api/v1/dsp";
    private static final String SECOND_DSP_ENDPOINT = SECOND_CONNECTOR + "/api/v1/dsp";

    @Mock
    private ConnectorEndpointsService connectorEndpointsService;
    @Mock
    private EdcOrchestrator edcOrchestrator;
    @Mock
    private EdcDataPlaneClient edcDataPlaneClient;

    private EdcRecursiveNotificationSender sender;

    @BeforeEach
    void setUp() {
        final EdcConfiguration edcConfiguration = new EdcConfiguration();
        edcConfiguration.setAsyncTimeout(Duration.ofSeconds(1));
        edcConfiguration.getControlplane().setProviderSuffix("/api/v1/dsp");
        sender = new EdcRecursiveNotificationSender(connectorEndpointsService, edcConfiguration, edcOrchestrator,
                edcDataPlaneClient);
    }

    @Test
    void shouldSendNotificationThroughLaterEndpointWhenEarlierEndpointFails() throws Exception {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder().build();
        final CatalogItem catalogItem = CatalogItem.builder()
                .itemId(NOTIFICATION_ASSET_ID)
                .connectorId(REMOTE_BPNL)
                .build();
        final EndpointDataReference endpointDataReference = endpointDataReference();
        final EdcNotificationResponse deliveredResponse = () -> true;

        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL))
                .thenReturn(List.of(FIRST_CONNECTOR, SECOND_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenThrow(new EdcClientException("catalog request failed"));
        when(edcOrchestrator.getCatalogItems(eq(SECOND_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(catalogItem));
        when(edcOrchestrator.getEndpointDataReference(SECOND_DSP_ENDPOINT, catalogItem))
                .thenReturn(completedFuture(endpointDataReference));
        when(edcDataPlaneClient.sendData(endpointDataReference, message)).thenReturn(deliveredResponse);

        sender.sendResponse(REMOTE_BPNL, message);

        verify(edcOrchestrator).getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL));
        verify(edcOrchestrator).getCatalogItems(eq(SECOND_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL));
        verify(edcDataPlaneClient).sendData(endpointDataReference, message);
    }

    @Test
    void shouldFindNotificationAssetByTypeAndVersion() throws Exception {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder().build();
        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId(NOTIFICATION_ASSET_ID)
                                                   .connectorId(REMOTE_BPNL)
                                                   .build();
        final EndpointDataReference endpointDataReference = endpointDataReference();
        final ArgumentCaptor<QuerySpec> querySpecCaptor = ArgumentCaptor.forClass(QuerySpec.class);

        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(catalogItem));
        when(edcOrchestrator.getEndpointDataReference(FIRST_DSP_ENDPOINT, catalogItem))
                .thenReturn(completedFuture(endpointDataReference));
        when(edcDataPlaneClient.sendData(endpointDataReference, message)).thenReturn(() -> true);

        sender.sendRequest(REMOTE_BPNL, message);

        verify(edcOrchestrator).getCatalogItems(eq(FIRST_DSP_ENDPOINT), querySpecCaptor.capture(), eq(REMOTE_BPNL));
        assertThat(querySpecCaptor.getValue().getFilterExpression()).containsExactly(
                new Criterion(EdcRecursiveNotificationSender.DCT_TYPE_ID, "=",
                        EdcRecursiveNotificationSender.NOTIFICATION_API_TYPE),
                new Criterion(EdcRecursiveNotificationSender.API_VERSION_PROPERTY, "=",
                        EdcRecursiveNotificationSender.NOTIFICATION_API_VERSION));
    }

    @Test
    void shouldThrowClassifiedFailureWhenNoDiscoveredEndpointDeliversNotification() throws Exception {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder().build();

        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL))
                .thenReturn(List.of(FIRST_CONNECTOR, SECOND_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenThrow(new EdcClientException("first catalog request failed"));
        when(edcOrchestrator.getCatalogItems(eq(SECOND_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenThrow(new EdcClientException("second catalog request failed"));

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, message))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception -> {
                    assertThat(exception.getReason())
                            .isEqualTo(RecursiveNotificationDeliveryFailureReason.CATALOG_REQUEST_FAILED);
                    assertThat(exception.getErrorRef()).isNotBlank();
                });
        verify(edcDataPlaneClient, never()).sendData(any(), eq(message));
    }

    @Test
    void shouldClassifyMissingDiscoveryResultAsNoConnectorEndpoint() {
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of());

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.NO_CONNECTOR_ENDPOINT));
    }

    @Test
    void shouldClassifyConnectorDiscoveryFailure() {
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL))
                .thenThrow(new IllegalStateException("discovery failed"));

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.CONNECTOR_DISCOVERY_FAILED));
    }

    @Test
    void shouldClassifyEmptyCatalogAsMissingNotificationAsset() throws Exception {
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.NOTIFICATION_ASSET_NOT_FOUND));
    }

    @Test
    void shouldRejectAmbiguousNotificationAssets() throws Exception {
        final CatalogItem firstCatalogItem = CatalogItem.builder().itemId("notification-api-one").build();
        final CatalogItem secondCatalogItem = CatalogItem.builder().itemId("notification-api-two").build();
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(firstCatalogItem, secondCatalogItem));

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.NOTIFICATION_ASSET_AMBIGUOUS));
        verify(edcOrchestrator, never()).getEndpointDataReference(any(), any(CatalogItem.class));
    }

    @Test
    void shouldClassifyFailedNegotiationAsContractNegotiationFailure() throws Exception {
        final CatalogItem catalogItem = CatalogItem.builder().itemId(NOTIFICATION_ASSET_ID).connectorId(REMOTE_BPNL).build();
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(catalogItem));
        when(edcOrchestrator.getEndpointDataReference(FIRST_DSP_ENDPOINT, catalogItem))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("negotiation failed")));

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.CONTRACT_NEGOTIATION_FAILED));
    }

    @Test
    void shouldClassifyRejectedDataPlaneDeliveryAsDataPlaneFailure() throws Exception {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder().build();
        final CatalogItem catalogItem = CatalogItem.builder().itemId(NOTIFICATION_ASSET_ID).connectorId(REMOTE_BPNL).build();
        final EndpointDataReference endpointDataReference = endpointDataReference();
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(catalogItem));
        when(edcOrchestrator.getEndpointDataReference(FIRST_DSP_ENDPOINT, catalogItem))
                .thenReturn(completedFuture(endpointDataReference));
        when(edcDataPlaneClient.sendData(endpointDataReference, message)).thenReturn(() -> false);

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, message))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.DATA_PLANE_DELIVERY_FAILED));
    }

    @Test
    void shouldClassifyDataPlaneClientFailure() throws Exception {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder().build();
        final CatalogItem catalogItem = CatalogItem.builder().itemId(NOTIFICATION_ASSET_ID).connectorId(REMOTE_BPNL).build();
        final EndpointDataReference endpointDataReference = endpointDataReference();
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL)).thenReturn(List.of(FIRST_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenReturn(List.of(catalogItem));
        when(edcOrchestrator.getEndpointDataReference(FIRST_DSP_ENDPOINT, catalogItem))
                .thenReturn(completedFuture(endpointDataReference));
        when(edcDataPlaneClient.sendData(endpointDataReference, message))
                .thenThrow(new IllegalStateException("data plane failed"));

        assertThatThrownBy(() -> sender.sendResponse(REMOTE_BPNL, message))
                .isInstanceOfSatisfying(RecursiveNotificationDeliveryException.class, exception ->
                        assertThat(exception.getReason())
                                .isEqualTo(RecursiveNotificationDeliveryFailureReason.EDC_NOTIFICATION_FAILED));
    }

    @Test
    void shouldTryConnectorEndpointsInDeterministicOrder() throws Exception {
        when(connectorEndpointsService.fetchConnectorEndpoints(REMOTE_BPNL))
                .thenReturn(List.of(SECOND_CONNECTOR, FIRST_CONNECTOR, SECOND_CONNECTOR));
        when(edcOrchestrator.getCatalogItems(any(), any(QuerySpec.class), eq(REMOTE_BPNL)))
                .thenThrow(new EdcClientException("catalog request failed"));

        assertThatThrownBy(() -> sender.sendRequest(REMOTE_BPNL, RecursiveNotificationMessage.builder().build()))
                .isInstanceOf(RecursiveNotificationDeliveryException.class);

        final InOrder inOrder = inOrder(edcOrchestrator);
        inOrder.verify(edcOrchestrator).getCatalogItems(eq(FIRST_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL));
        inOrder.verify(edcOrchestrator).getCatalogItems(eq(SECOND_DSP_ENDPOINT), any(QuerySpec.class), eq(REMOTE_BPNL));
        verify(edcOrchestrator, times(2)).getCatalogItems(any(), any(QuerySpec.class), eq(REMOTE_BPNL));
    }

    private EndpointDataReference endpointDataReference() {
        return EndpointDataReference.Builder.newInstance()
                .id("recursive-notification-edr")
                .endpoint("http://data-plane")
                .contractId("contract-id")
                .authKey("X-API-KEY")
                .authCode("secret")
                .build();
    }
}
