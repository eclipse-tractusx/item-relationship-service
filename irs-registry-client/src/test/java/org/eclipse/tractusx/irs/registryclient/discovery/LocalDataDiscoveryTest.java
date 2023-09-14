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
package org.eclipse.tractusx.irs.registryclient.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class LocalDataDiscoveryTest {

    private final LocalDataDiscovery testee = new LocalDataDiscovery(Map.of("bpn1", "endpoint1", "bpn2", "endpoint2"));

    @Test
    void findDiscoveryEndpoints() {
        final var result = testee.findDiscoveryEndpoints(new DiscoveryFinderRequest(List.of()));

        assertThat(result.endpoints()).hasSize(1);
    }

    @Test
    void shouldFindNoEndpointsForEmptyBpnList() {
        final var result = testee.findConnectorEndpoints("", List.of(""));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindOneEndpointForOneBpn() {
        final var result = testee.findConnectorEndpoints("", List.of("bpn1"));

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFindTwoEndpointForTwoBpns() {
        final var result = testee.findConnectorEndpoints("", List.of("bpn1", "bpn2"));

        assertThat(result).hasSize(2);
    }
}