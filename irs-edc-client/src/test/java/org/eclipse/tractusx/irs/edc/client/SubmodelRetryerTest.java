/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createEdcTransformer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.storage.ContractNegotiationIdStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SubmodelExponentialRetryTest {

    public static final String SUBMODEL_DATAPLANE_URL = "https://edc.dataplane.test/public/submodel";
    public static final String PROVIDER_SUFFIX = "/ids";
    private final RetryRegistry retryRegistry = new InMemoryRetryRegistry();
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PolicyCheckerService policyCheckerService;
    @Mock
    private EndpointDataReferenceCacheService endpointDataReferenceCacheService;
    @Mock
    private ContractNegotiationIdStorage contractNegotiationIdStorage;
    private EdcSubmodelFacade testee;

    @BeforeEach
    void setUp() {
        final AsyncPollingService pollingService = new AsyncPollingService(Clock.systemUTC(),
                Executors.newSingleThreadScheduledExecutor());
        final EdcConfiguration config = new EdcConfiguration();
        config.getSubmodel().setUrnPrefix("/urn");
        config.getControlplane().setProviderSuffix(PROVIDER_SUFFIX);

        final EdcControlPlaneClient controlPlaneClient = new EdcControlPlaneClient(restTemplate, pollingService, config,
                createEdcTransformer());

        final EDCCatalogFacade catalogFacade = new EDCCatalogFacade(controlPlaneClient, config);

        final ContractNegotiationService negotiationService = new ContractNegotiationService(controlPlaneClient,
                policyCheckerService, config);
        final EdcDataPlaneClient dataPlaneClient = new EdcDataPlaneClient(restTemplate);
        final ExecutorService fixedThreadPoolExecutorService = Executors.newFixedThreadPool(2);
        final OngoingNegotiationStorage ongoingNegotiationStorage = new OngoingNegotiationStorage();
        final EdcOrchestrator edcOrchestrator = new EdcOrchestrator(config, negotiationService, pollingService,
                catalogFacade, endpointDataReferenceCacheService, contractNegotiationIdStorage, fixedThreadPoolExecutorService,
                ongoingNegotiationStorage);
        final EdcSubmodelClient client = new EdcSubmodelClientImpl(config, dataPlaneClient, edcOrchestrator,
                retryRegistry);
        testee = new EdcSubmodelFacade(client, config);
    }

    @Test
    void shouldRetryExecutionOfGetSubmodelOnClientMaxAttemptTimes() {
        // Arrange
        given(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(String.class))).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "EDC remote exception"));

        final String endpoint = "https://connector.endpoint.com";
        final String assetId = "9300395e-c0a5-4e88-bc57-a3973fec4c26";
        final String storageId = assetId + endpoint + PROVIDER_SUFFIX;
        when(endpointDataReferenceCacheService.getEndpointDataReference(storageId)).thenReturn(
                new EndpointDataReferenceStatus(null, EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW));

        // Act
        assertThatThrownBy(() -> testee.getSubmodelPayload(endpoint, SUBMODEL_DATAPLANE_URL, assetId, "bpn")).cause()
                                                                                                             .hasCauseInstanceOf(
                                                                                                                     HttpServerErrorException.class);

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).exchange(any(String.class),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void shouldRetryOnAnyRuntimeException() {
        // Arrange
        given(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(String.class))).willThrow(new RuntimeException("EDC remote exception"));

        final String endpoint = "https://connector.endpoint.com";
        final String assetId = "9300395e-c0a5-4e88-bc57-a3973fec4c26";
        final String storageId = assetId + endpoint + PROVIDER_SUFFIX;
        when(endpointDataReferenceCacheService.getEndpointDataReference(storageId)).thenReturn(
                new EndpointDataReferenceStatus(null, EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW));

        // Act
        assertThatThrownBy(() -> testee.getSubmodelPayload(endpoint, SUBMODEL_DATAPLANE_URL, assetId, "bpn")).cause()
                                                                                                             .hasCauseInstanceOf(
                                                                                                                     RuntimeException.class);

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).exchange(any(String.class),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

}