/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.ess.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class EdcDiscoveryFacadeTest {

    private final EdcDiscoveryClientLocalStub.EdcDiscoveryMockConfig edcDiscoveryMockConfig = mock(
            EdcDiscoveryClientLocalStub.EdcDiscoveryMockConfig.class);
    private final EdcDiscoveryFacade edcDiscoveryFacade = new EdcDiscoveryFacade(new EdcDiscoveryClientLocalStub(edcDiscoveryMockConfig));

    @Test
    void shouldReturnEdcBaseUrl() {
        final String bpn = "BPNS000000000DDD";
        final String url = "http://edc-url";
        when(edcDiscoveryMockConfig.getMockEdcAddress()).thenReturn(Map.of(bpn, url));

        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl(bpn);

        assertThat(edcBaseUrl).isNotEmpty().contains(url);
    }

    @Test
    void shouldReturnResponseWithEmptyConnectorEndpointList() {
        when(edcDiscoveryMockConfig.getMockEdcAddress()).thenReturn(Map.of());

        final Optional<String> edcBaseUrl = edcDiscoveryFacade.getEdcBaseUrl("not_existing");

        assertThat(edcBaseUrl).isEmpty();
    }

}
