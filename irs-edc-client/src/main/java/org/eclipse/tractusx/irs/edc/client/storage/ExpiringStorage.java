/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Generic in-memory storage. Storage duration can be set via storageDuration.
 */
public class ExpiringStorage<T> {
    private final Map<String, ExpiringContainer<T>> storageMap = new ConcurrentHashMap<>();
    private final Duration storageDuration;

    public ExpiringStorage(final Duration storageDuration) {
        this.storageDuration = storageDuration;
    }

    public void put(final String storageId, final T storedObject) {
        storageMap.put(storageId, new ExpiringContainer<>(Instant.now(), storedObject));
        cleanup();
    }

    /**
     * Cleans up all dangling references which were not collected after the storageDuration.
     */
    private void cleanup() {
        final Set<String> keys = new HashSet<>(storageMap.keySet());
        keys.forEach(key -> {
            final Instant creationTimestamp = storageMap.get(key).creationTimestamp();
            if (Instant.now().isAfter(creationTimestamp.plus(storageDuration))) {
                storageMap.remove(key);
            }
        });
    }

    public Optional<T> get(final String storageId) {
        return Optional.ofNullable(storageMap.get(storageId)).map(ExpiringContainer::storedObject);
    }

    public void clear() {
        storageMap.clear();
    }

    /**
     * Stores the object with its creation date.
     */
    private record ExpiringContainer<T>(Instant creationTimestamp, T storedObject) {
    }

}

