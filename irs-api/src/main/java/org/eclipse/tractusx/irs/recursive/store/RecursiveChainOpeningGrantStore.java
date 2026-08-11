/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.store;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;

/**
 * Abstraction for persisting and querying recursive chain opening grants.
 *
 * <p>Implementations may use MinIO/blob storage for production
 * or lightweight test doubles for unit tests.</p>
 */
public interface RecursiveChainOpeningGrantStore {

    /**
     * Stores a grant. If a grant with the same {@link RecursiveChainOpeningGrantKey} exists,
     * it is replaced.
     *
     * @param grant the grant to persist
     */
    void store(RecursiveChainOpeningGrant grant);

    /**
     * Finds the grant for a specific grant key.
     *
     * @param key the grant key (openingId + globalAssetId + requesterBpn + useCase)
     * @return the matching grant, if present
     */
    Optional<RecursiveChainOpeningGrant> find(RecursiveChainOpeningGrantKey key);

    /**
     * Returns all stored grants.
     *
     * @return all persisted chain opening grants
     */
    List<RecursiveChainOpeningGrant> findAll();

    /**
     * Removes the grant for a specific grant key.
     *
     * @param key the grant key (openingId + globalAssetId + requesterBpn + useCase)
     * @return true if a grant was removed
     */
    boolean remove(RecursiveChainOpeningGrantKey key);
}
