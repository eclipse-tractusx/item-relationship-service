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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelFacadeTest {

    private final static String CONNECTOR_ENDPOINT = "https://connector.endpoint.com";
    private final static String DATAPLANE_URL = "https://edc.dataplane.test/public/submodel";
    private final static String ASSET_ID = "9300395e-c0a5-4e88-bc57-a3973fec4c26";

    private EdcSubmodelFacade testee;

    @Mock
    private EdcSubmodelClient client;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EdcConfiguration config;

    @BeforeEach
    public void beforeEach() {
        this.testee = new EdcSubmodelFacade(client, config);
    }

    @Nested
    @DisplayName("getSubmodelRawPayload")
    class GetSubmodelRawPayloadTests {

        @Test
        void shouldThrowExecutionExceptionForSubmodel() throws EdcClientException {
            // arrange
            final ExecutionException e = new ExecutionException(new EdcClientException("test"));
            final CompletableFuture<SubmodelDescriptor> future = CompletableFuture.failedFuture(e);
            when(client.getSubmodelPayload(any(), any(), any(), any())).thenReturn(future);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelPayload(CONNECTOR_ENDPOINT, DATAPLANE_URL,
                    ASSET_ID, "bpn");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldThrowEdcClientExceptionForSubmodel() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.getSubmodelPayload(any(), any(), any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelPayload(CONNECTOR_ENDPOINT, DATAPLANE_URL,
                    ASSET_ID, "bpn");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldRestoreInterruptOnInterruptExceptionForSubmodel()
                throws EdcClientException, ExecutionException, InterruptedException, TimeoutException {
            // arrange
            final CompletableFuture<SubmodelDescriptor> future = mock(CompletableFuture.class);
            final InterruptedException e = new InterruptedException();
            when(config.getAsyncTimeoutMillis()).thenReturn(1000L);
            when(future.get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS)).thenThrow(e);
            when(client.getSubmodelPayload(any(), any(), any(), any())).thenReturn(future);

            // act
            testee.getSubmodelPayload(CONNECTOR_ENDPOINT, DATAPLANE_URL, ASSET_ID, "bpn");

            // assert
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        void shouldAppendSubmodelSuffixPathCorrectly() throws EdcClientException {
            // Arrange
            final String connectorEndpoint = "https://connector.endpoint.com";
            final String dataplaneUrl = "https://edc.dataplane.test/public/submodel?content=value&extent=withBlobValue";
            final CompletableFuture<SubmodelDescriptor> future = mock(CompletableFuture.class);
            when(client.getSubmodelPayload(any(), any(), any(), any())).thenReturn(future);
            when(config.getSubmodel().getSubmodelSuffix()).thenReturn("/$value");
            when(config.getAsyncTimeoutMillis()).thenReturn(1000L);

            // Act
            testee.getSubmodelPayload(connectorEndpoint, dataplaneUrl, ASSET_ID, "bpn");

            // Assert
            final String expectedDataplaneUrl = "https://edc.dataplane.test/public/submodel/$value?content=value&extent=withBlobValue";
            verify(client, times(1)).getSubmodelPayload(connectorEndpoint, expectedDataplaneUrl, ASSET_ID, "bpn");
        }

        @ParameterizedTest
        @CsvSource(
                { "'https://edc.dataplane.test/public/submodel', '/$value', 'https://edc.dataplane.test/public/submodel/$value'",
                  "'https://edc.dataplane.test/public/submodel', '$value', 'https://edc.dataplane.test/public/submodel/$value'",
                  "'https://edc.dataplane.test/public/submodel/', '/$value', 'https://edc.dataplane.test/public/submodel/$value'",
                  "'https://edc.dataplane.test/public/submodel/', '$value', 'https://edc.dataplane.test/public/submodel/$value'",
                  "'https://edc.test/submodel?content=value&extent=withBlobValue', '/$value', 'https://edc.test/submodel/$value?content=value&extent=withBlobValue'",
                  "'https://edc.test/submodel?content=value&extent=withBlobValue', '$value', 'https://edc.test/submodel/$value?content=value&extent=withBlobValue'",
                  "'https://edc.test/submodel/?content=value&extent=withBlobValue', '/$value', 'https://edc.test/submodel/$value?content=value&extent=withBlobValue'",
                  "'https://edc.test/submodel/?content=value&extent=withBlobValue', '$value', 'https://edc.test/submodel/$value?content=value&extent=withBlobValue'"
                })
        void shouldAppendSubmodelSuffixPathCorrectly(final String dataplaneUrlFromHref, final String submodelSuffix,
                final String expectedDataplaneUrl) throws EdcClientException {
            // Arrange
            final String connectorEndpoint = "https://connector.endpoint.com";
            final CompletableFuture<SubmodelDescriptor> future = mock(CompletableFuture.class);
            when(client.getSubmodelPayload(any(), any(), any(), any())).thenReturn(future);
            when(config.getSubmodel().getSubmodelSuffix()).thenReturn(submodelSuffix);
            when(config.getAsyncTimeoutMillis()).thenReturn(1000L);

            // Act
            testee.getSubmodelPayload(connectorEndpoint, dataplaneUrlFromHref, ASSET_ID, "bpn");

            // Assert
            verify(client, times(1)).getSubmodelPayload(connectorEndpoint, expectedDataplaneUrl, ASSET_ID, "bpn");
        }

        @Test
        void shouldThrowEdcClientExceptionForInvalidHrefUrl() {
            // Arrange
            final String connectorEndpoint = "https://connector.endpoint.com";
            final String invalidDataplaneUrl = "://example.com";

            // Act & Assert
            assertThatThrownBy(() -> testee.getSubmodelPayload(connectorEndpoint, invalidDataplaneUrl, ASSET_ID,
                    "bpn")).isInstanceOf(EdcClientException.class)
                           .hasMessage("Invalid href URL '%s'".formatted(invalidDataplaneUrl));
        }
    }

    @Nested
    @DisplayName("sendNotification")
    class SendNotificationTests {

        @Test
        void shouldRestoreInterruptOnInterruptExceptionForNotification()
                throws EdcClientException, ExecutionException, InterruptedException, TimeoutException {
            // arrange
            final CompletableFuture<EdcNotificationResponse> future = mock(CompletableFuture.class);
            final InterruptedException e = new InterruptedException();
            when(config.getAsyncTimeoutMillis()).thenReturn(1000L);
            when(future.get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS)).thenThrow(e);
            when(client.sendNotification(any(), any(), any(), any())).thenReturn(future);

            // act
            testee.sendNotification("", "notify-request-asset", null, "bpn");

            // assert
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        void shouldThrowExecutionExceptionForNotification() throws EdcClientException {
            // arrange
            final ExecutionException e = new ExecutionException(new EdcClientException("test"));
            final CompletableFuture<EdcNotificationResponse> future = CompletableFuture.failedFuture(e);
            when(client.sendNotification(any(), any(), any(), any())).thenReturn(future);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.sendNotification("", "notify-request-asset", null,
                    "bpn");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldThrowEdcClientExceptionForNotification() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.sendNotification(any(), any(), any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.sendNotification("", "notify-request-asset", null,
                    "bpn");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }
    }

    @Nested
    @DisplayName("getEndpointReferencesForAsset")
    class GetEndpointReferencesForAssetTests {

        @Test
        void shouldThrowEdcClientExceptionForEndpointReference() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.getEndpointReferencesForRegistryAsset(any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getEndpointReferencesForRegistryAsset("", "");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldReturnFailedFuture() throws EdcClientException {

            // arrange
            when(client.getEndpointReferencesForRegistryAsset(any(), any())).thenReturn(
                    List.of(CompletableFuture.failedFuture(new EdcClientException("test"))));

            // act
            final List<CompletableFuture<EndpointDataReference>> results = testee.getEndpointReferencesForRegistryAsset(
                    "", "");

            // assert
            assertThat(results).hasSize(1);
            assertThatThrownBy(() -> results.get(0).get()).isInstanceOf(ExecutionException.class)
                                                          .extracting(Throwable::getCause)
                                                          .isInstanceOf(EdcClientException.class);
        }
    }

}