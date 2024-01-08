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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelFacadeTest {

    private final static String CONNECTOR_ENDPOINT = "https://connector.endpoint.com";
    private final static String SUBMODEL_SUFIX = "/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel";
    private final static String ASSET_ID = "9300395e-c0a5-4e88-bc57-a3973fec4c26";

    @InjectMocks
    private EdcSubmodelFacade testee;

    @Mock
    private EdcSubmodelClient client;

    @Nested
    @DisplayName("getSubmodelRawPayload")
    class GetSubmodelRawPayloadTests {

        @Test
        void shouldThrowExecutionExceptionForSubmodel() throws EdcClientException {
            // arrange
            final ExecutionException e = new ExecutionException(new EdcClientException("test"));
            final CompletableFuture<String> future = CompletableFuture.failedFuture(e);
            when(client.getSubmodelRawPayload(any(), any(), any())).thenReturn(future);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelRawPayload(CONNECTOR_ENDPOINT,
                    SUBMODEL_SUFIX, ASSET_ID);

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldThrowEdcClientExceptionForSubmodel() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.getSubmodelRawPayload(any(), any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getSubmodelRawPayload(CONNECTOR_ENDPOINT,
                    SUBMODEL_SUFIX, ASSET_ID);

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldRestoreInterruptOnInterruptExceptionForSubmodel()
                throws EdcClientException, ExecutionException, InterruptedException {
            // arrange
            final CompletableFuture<String> future = mock(CompletableFuture.class);
            final InterruptedException e = new InterruptedException();
            when(future.get()).thenThrow(e);
            when(client.getSubmodelRawPayload(any(), any(), any())).thenReturn(future);

            // act
            testee.getSubmodelRawPayload(CONNECTOR_ENDPOINT, SUBMODEL_SUFIX, ASSET_ID);

            // assert
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

    }

    @Nested
    @DisplayName("sendNotification")
    class SendNotificationTests {

        @Test
        void shouldRestoreInterruptOnInterruptExceptionForNotification()
                throws EdcClientException, ExecutionException, InterruptedException {
            // arrange
            final CompletableFuture<EdcNotificationResponse> future = mock(CompletableFuture.class);
            final InterruptedException e = new InterruptedException();
            when(future.get()).thenThrow(e);
            when(client.sendNotification(any(), any(), any())).thenReturn(future);

            // act
            testee.sendNotification("", "notify-request-asset", null);

            // assert
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        void shouldThrowExecutionExceptionForNotification() throws EdcClientException {
            // arrange
            final ExecutionException e = new ExecutionException(new EdcClientException("test"));
            final CompletableFuture<EdcNotificationResponse> future = CompletableFuture.failedFuture(e);
            when(client.sendNotification(any(), any(), any())).thenReturn(future);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.sendNotification("", "notify-request-asset", null);

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldThrowEdcClientExceptionForNotification() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.sendNotification(any(), any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.sendNotification("", "notify-request-asset", null);

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }
    }

    @Nested
    @DisplayName("getEndpointReferenceForAsset")
    class GetEndpointReferenceForAssetTests {

        @Test
        void shouldThrowEdcClientExceptionForEndpointReference() throws EdcClientException {
            // arrange
            final EdcClientException e = new EdcClientException("test");
            when(client.getEndpointReferenceForAsset(any(), any(), any())).thenThrow(e);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getEndpointReferenceForAsset("", "", "");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldThrowExecutionExceptionForEndpointReference() throws EdcClientException {
            // arrange
            final ExecutionException e = new ExecutionException(new EdcClientException("test"));
            final CompletableFuture<EndpointDataReference> future = CompletableFuture.failedFuture(e);
            when(client.getEndpointReferenceForAsset(any(), any(), any())).thenReturn(future);

            // act
            ThrowableAssert.ThrowingCallable action = () -> testee.getEndpointReferenceForAsset("", "", "");

            // assert
            assertThatThrownBy(action).isInstanceOf(EdcClientException.class);
        }

        @Test
        void shouldRestoreInterruptOnInterruptExceptionForEndpointReference()
                throws EdcClientException, ExecutionException, InterruptedException {
            // arrange
            final CompletableFuture<EndpointDataReference> future = mock(CompletableFuture.class);
            final InterruptedException e = new InterruptedException();
            when(future.get()).thenThrow(e);
            when(client.getEndpointReferenceForAsset(any(), any(), any())).thenReturn(future);

            // act
            testee.getEndpointReferenceForAsset("", "", "");

            // assert
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }
    }

}