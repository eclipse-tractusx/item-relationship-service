/********************************************************************************
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

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.NoArgsConstructor;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.springframework.stereotype.Service;

/**
 * Service class for managing ongoing negotiations.
 * Stores and manages CompletableFuture instances representing ongoing negotiations for assets.
 */
@Service
@NoArgsConstructor
public class OngoingNegotiationStorage {
    private final ConcurrentMap<String, CompletableFuture<EndpointDataReference>> ongoingNegotiations = new ConcurrentHashMap<>();

    /**
     * Adds a new ongoing negotiation for the specified asset ID.
     *
     * @param assetId           The ID of the asset for which the negotiation is ongoing.
     * @param completableFuture The CompletableFuture representing the ongoing negotiation.
     */
    public void addToOngoingNegotiations(final String assetId,
            final CompletableFuture<EndpointDataReference> completableFuture) {
        ongoingNegotiations.put(assetId, completableFuture);
    }

    /**
     * Removes the ongoing negotiation for the specified asset ID.
     *
     * @param assetId The ID of the asset whose negotiation should be removed.
     */
    public void removeFromOngoingNegotiations(final String assetId) {
        ongoingNegotiations.remove(assetId);
    }

    /**
     * Retrieves the ongoing negotiation for the specified asset ID.
     *
     * @param assetId The ID of the asset whose negotiation should be retrieved.
     * @return The CompletableFuture representing the ongoing negotiation, or null if none exists.
     */
    public CompletableFuture<EndpointDataReference> getOngoingNegotiation(final String assetId) {
        return ongoingNegotiations.get(assetId);
    }

    /**
     * Checks if there is an ongoing negotiation for the specified asset ID.
     *
     * @param assetId The ID of the asset to check.
     * @return {@code true} if a negotiation is ongoing for the asset ID, {@code false} otherwise.
     */
    public boolean isNegotiationOngoing(final String assetId) {
        return ongoingNegotiations.containsKey(assetId);
    }

    /**
     * Retrieves a set of asset IDs for which negotiations are currently ongoing.
     *
     * @return A set of asset IDs with ongoing negotiations.
     */
    public Set<String> getOngoingNegotiations() {
        return ongoingNegotiations.keySet();
    }
}
