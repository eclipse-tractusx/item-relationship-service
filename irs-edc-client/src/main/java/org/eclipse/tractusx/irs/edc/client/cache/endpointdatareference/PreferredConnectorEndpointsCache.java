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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * In memory cache for preferred EDC endpoints
 */
@Service
public class PreferredConnectorEndpointsCache {

    private final ConcurrentHashMap<String, String> inMemoryCache = new ConcurrentHashMap<>();

    public void store(final String bpn, final String edcEndpoint) {
        inMemoryCache.put(bpn, edcEndpoint);
    }

    public Optional<String> findByBpn(final String bpn) {
        return Optional.ofNullable(inMemoryCache.get(bpn));
    }

    public void remove(final String bpn) {
        if (findByBpn(bpn).isPresent()) {
            inMemoryCache.remove(bpn);
        }
    }

}
