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
package org.eclipse.tractusx.irs.registryclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.registryclient.TestMother.endpointDataReference;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClient;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.PreferredConnectorEndpointsCache;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.decentral.EdcRetrieverException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class DefaultConfigurationTest {

    private final DefaultConfiguration testee = new DefaultConfiguration();

    private final EdcConfiguration edcConfiguration = new EdcConfiguration();
    private final String descriptorTemplate = "descriptor/{aasIdentifier}";
    private final String shellLookupTemplate = "shell?{assetIds}";
    private final PreferredConnectorEndpointsCache preferredConnectorEndpointsCache = new PreferredConnectorEndpointsCache();

    @Test
    void centralDigitalTwinRegistryService() {
        final var service = testee.centralDigitalTwinRegistryService(
                testee.digitalTwinRegistryClientImpl(new RestTemplate(), descriptorTemplate, shellLookupTemplate));

        assertThat(service).isNotNull();
    }

    @Test
    void decentralDigitalTwinRegistryService() {
        final EdcSubmodelFacade facadeMock = mock(EdcSubmodelFacade.class);
        final var service = testee.decentralDigitalTwinRegistryService(
                testee.connectorEndpointsService(testee.discoveryFinderClient(new RestTemplate(), "finder"), "bpnl"),
                testee.endpointDataForConnectorsService(facadeMock, preferredConnectorEndpointsCache),
                testee.decentralDigitalTwinRegistryClient(new RestTemplate(), descriptorTemplate, shellLookupTemplate),
                edcConfiguration, new PreferredConnectorEndpointsCache());

        assertThat(service).isNotNull();
    }

    @Test
    void edcSubmodelFacade() {
        final EdcSubmodelClient facadeMock = mock(EdcSubmodelClient.class);
        final EdcSubmodelFacade edcSubmodelFacade = testee.edcSubmodelFacade(facadeMock, edcConfiguration);

        assertThat(edcSubmodelFacade).isNotNull();
    }

    @Test
    void endpointDataForConnectorsService() throws EdcClientException {

        // ARRANGE
        final var mock = mock(EdcSubmodelFacade.class);
        final var endpointAddress = "endpointaddress";
        final String contractAgreementId = "contractAgreementId";
        final var endpointDataReference = endpointDataReference(contractAgreementId, endpointAddress);
        when(mock.getEndpointReferencesForRegistryAsset(eq(endpointAddress), any())).thenReturn(
                List.of(CompletableFuture.completedFuture(endpointDataReference)));

        // ACT
        final var endpointDataForConnectorsService = testee.endpointDataForConnectorsService(mock, preferredConnectorEndpointsCache);

        endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(List.of(endpointAddress), "bpn") //
                                        .forEach(future -> {
                                            try {
                                                future.get();
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                throw new RuntimeException(e);
                                            } catch (ExecutionException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });

        // ASSERT
        verify(mock).getEndpointReferencesForRegistryAsset(eq(endpointAddress), any());
    }

    @Test
    void endpointDataForConnectorsService_withException() throws EdcClientException {
        final var mock = mock(EdcSubmodelFacade.class);
        when(mock.getEndpointReferencesForRegistryAsset(any(), any())).thenThrow(new EdcClientException("test"));

        final var endpointDataForConnectorsService = testee.endpointDataForConnectorsService(mock, preferredConnectorEndpointsCache);
        final var dummyEndpoints = List.of("test");
        endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(dummyEndpoints, "bpn").forEach(future -> {
            assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class)
                                           .extracting(Throwable::getCause)
                                           .isInstanceOf(EdcRetrieverException.class);
        });
    }
}