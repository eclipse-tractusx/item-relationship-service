/********************************************************************************
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PreferredConnectorEndpointsCacheTest {

    private final PreferredConnectorEndpointsCache preferredConnectorEndpointsCache = new PreferredConnectorEndpointsCache();

    @Test
    void shouldStoreEdcUrlByBpn() {
        // given
        final String bpn = "bpn";
        final String edcUrl = "edcUrl";

        // when
        preferredConnectorEndpointsCache.store(bpn, edcUrl);

        // then
        final String storedEdcUrl = preferredConnectorEndpointsCache.findByBpn(bpn).orElse(null);
        assertThat(edcUrl).isEqualTo(storedEdcUrl);
    }

    @Test
    void shouldRemoveEdcUrlByBpn() {
        // given
        final String bpn = "bpn";
        final String edcUrl = "edcUrl";
        preferredConnectorEndpointsCache.store(bpn, edcUrl);

        // when
        preferredConnectorEndpointsCache.remove(bpn);

        // then
        final String storedEdcUrl = preferredConnectorEndpointsCache.findByBpn(bpn).orElse(null);
        assertThat(storedEdcUrl).isNull();
    }
}