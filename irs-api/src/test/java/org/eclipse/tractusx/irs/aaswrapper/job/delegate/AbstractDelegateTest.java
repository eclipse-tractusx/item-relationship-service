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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractDelegateTest {

    @Mock
    private EdcSubmodelFacade submodelFacade;
    @Mock
    private ConnectorEndpointsService connectorEndpointsService;
    private SubmodelDelegate submodelDelegate;

    @BeforeEach
    void setUp() {
        submodelDelegate = new SubmodelDelegate(null, null, null, null, null);
    }

    @Test
    void shouldUseDspEndpointIfPresent() throws EdcClientException {
        // Arrange
        when(submodelFacade.getSubmodelRawPayload(any(), any(), any())).thenReturn("test");
        final Endpoint endpoint = Endpoint.builder()
                                          .protocolInformation(ProtocolInformation.builder()
                                                                                  .href("http://dataplane.test/123")
                                                                                  .subprotocolBody(
                                                                                          "id=123;dspEndpoint=http://edc.test")
                                                                                  .build())
                                          .build();
        final String bpn = "BPN123";

        // Act
        final String submodel = submodelDelegate.requestSubmodelAsString(submodelFacade, null, endpoint, bpn);

        // Assert
        assertThat(submodel).isEqualTo("test");
        verify(submodelFacade, times(1)).getSubmodelRawPayload("http://edc.test", "http://dataplane.test/123", "123");
    }

    @Test
    void shouldUseDiscoveryFinderIfDspEndpointNotPresent() throws EdcClientException {
        // Arrange
        final String connector1 = "http://edc.test1";
        final String connector2 = "http://edc.test2";
        when(submodelFacade.getSubmodelRawPayload(eq(connector1), any(), any())).thenThrow(
                new EdcClientException("test"));
        when(submodelFacade.getSubmodelRawPayload(eq(connector2), any(), any())).thenReturn("test");
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of(connector1, connector2));
        final String dataplaneUrl = "http://dataplane.test/123";
        final Endpoint endpoint = Endpoint.builder()
                                          .protocolInformation(ProtocolInformation.builder()
                                                                                  .href(dataplaneUrl)
                                                                                  .subprotocolBody("id=123")
                                                                                  .build())
                                          .build();
        final String bpn = "BPN123";

        // Act
        final String submodel = submodelDelegate.requestSubmodelAsString(submodelFacade, connectorEndpointsService,
                endpoint, bpn);

        // Assert
        assertThat(submodel).isEqualTo("test");
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector1, dataplaneUrl, "123");
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector2, dataplaneUrl, "123");
        verify(connectorEndpointsService, times(1)).fetchConnectorEndpoints(bpn);
    }

    @Test
    void shouldThrowPreserveUsagePolicyExceptionIfAllEndpointsThrowExceptions() throws EdcClientException {
        // Arrange
        final String connector1 = "http://edc.test1";
        final String connector2 = "http://edc.test2";
        when(submodelFacade.getSubmodelRawPayload(any(), any(), any())).thenThrow(new UsagePolicyException("test"));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of(connector1, connector2));
        final String dataplaneUrl = "http://dataplane.test/123";
        final Endpoint endpoint = Endpoint.builder()
                                          .protocolInformation(ProtocolInformation.builder()
                                                                                  .href(dataplaneUrl)
                                                                                  .subprotocolBody("id=123")
                                                                                  .build())
                                          .build();
        final String bpn = "BPN123";

        // Act
        assertThatExceptionOfType(UsagePolicyException.class).isThrownBy(
                () -> submodelDelegate.requestSubmodelAsString(submodelFacade, connectorEndpointsService, endpoint,
                        bpn));

        // Assert
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector1, dataplaneUrl, "123");
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector2, dataplaneUrl, "123");
        verify(connectorEndpointsService, times(1)).fetchConnectorEndpoints(bpn);
    }

    @Test
    void shouldThrowGeneralEdcClientExceptionIfAllEndpointsThrowExceptions() throws EdcClientException {
        // Arrange
        final String connector1 = "http://edc.test1";
        final String connector2 = "http://edc.test2";
        when(submodelFacade.getSubmodelRawPayload(any(), any(), any())).thenThrow(new EdcClientException("test"));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of(connector1, connector2));
        final String dataplaneUrl = "http://dataplane.test/123";
        final Endpoint endpoint = Endpoint.builder()
                                          .protocolInformation(ProtocolInformation.builder()
                                                                                  .href(dataplaneUrl)
                                                                                  .subprotocolBody("id=123")
                                                                                  .build())
                                          .build();
        final String bpn = "BPN123";

        // Act
        assertThatExceptionOfType(EdcClientException.class).isThrownBy(
                () -> submodelDelegate.requestSubmodelAsString(submodelFacade, connectorEndpointsService, endpoint,
                        bpn));

        // Assert
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector1, dataplaneUrl, "123");
        verify(submodelFacade, times(1)).getSubmodelRawPayload(connector2, dataplaneUrl, "123");
        verify(connectorEndpointsService, times(1)).fetchConnectorEndpoints(bpn);
    }
}