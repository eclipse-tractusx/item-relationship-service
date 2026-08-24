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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;

/**
 * Map-backed grant store for unit tests.
 */
public class InMemoryRecursiveChainOpeningGrantStore implements RecursiveChainOpeningGrantStore {

    private final Map<RecursiveChainOpeningGrantKey, RecursiveChainOpeningGrant> grants = new ConcurrentHashMap<>();

    @Override
    public void store(final RecursiveChainOpeningGrant grant) {
        grants.put(RecursiveChainOpeningGrantKey.of(grant), grant);
    }

    @Override
    public Optional<RecursiveChainOpeningGrant> find(final RecursiveChainOpeningGrantKey key) {
        return Optional.ofNullable(grants.get(key));
    }

    @Override
    public List<RecursiveChainOpeningGrant> findAll() {
        return List.copyOf(grants.values());
    }

    @Override
    public boolean remove(final RecursiveChainOpeningGrantKey key) {
        return grants.remove(key) != null;
    }
}
