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
package org.eclipse.tractusx.irs.registryclient.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConnectorEndpointsServiceTest {

    private final DiscoveryFinderClient essDiscoveryFinderClient = Mockito.mock(DiscoveryFinderClient.class);
    private final ConnectorEndpointsService service = new ConnectorEndpointsService(essDiscoveryFinderClient);

    @Test
    void shouldFindConnectorEndpoints() {
        // given
        final String bpn = "BPN123";
        given(essDiscoveryFinderClient.findDiscoveryEndpoints(any())).willReturn(
                new DiscoveryResponse(List.of(createEndpoint("address1"), createEndpoint("address2"))));
        given(essDiscoveryFinderClient.findConnectorEndpoints(eq("address1"), any())).willReturn(
                List.of(createResult(List.of("connector1", "connector2"))));
        given(essDiscoveryFinderClient.findConnectorEndpoints(eq("address2"), any())).willReturn(
                List.of(createResult(List.of("connector3", "connector4"))));

        // when
        final List<String> actualConnectors = service.fetchConnectorEndpoints(bpn);

        // then
        assertThat(actualConnectors).containsExactly("connector1", "connector2", "connector3", "connector4");
    }

    @Test
    void shouldReturnEmptyListOnMissingBpn() {
        // when
        final List<String> actualConnectors = service.fetchConnectorEndpoints(null);

        // then
        assertThat(actualConnectors).isNotNull().isEmpty();
    }

    private DiscoveryEndpoint createEndpoint(final String endpointAddress) {
        return new DiscoveryEndpoint("test-endpoint", "desc", endpointAddress, "docs", "resId");
    }

    private EdcDiscoveryResult createResult(final List<String> connectors) {
        return new EdcDiscoveryResult("BPN123", connectors);
    }

}