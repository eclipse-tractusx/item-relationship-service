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
package org.eclipse.tractusx.irs.edc;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Value;
import org.springframework.stereotype.Service;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

/**
 * InMemory storage for endpoint data references.
 */
@Service
public class EndpointDataReferenceStorage {

    private static final Duration STORAGE_DURATION = Duration.ofHours(1);

    private final Map<String, ExpiringContainer> storageMap = new ConcurrentHashMap<>();

    public void put(final String contractAgreementId, final EndpointDataReference dataReference) {
        storageMap.put(contractAgreementId, new ExpiringContainer(Instant.now(), dataReference));
        cleanup();
    }

    /**
     * Cleans up all dangling references which were not collected after the STORAGE_DURATION.
     */
    private void cleanup() {
        final Set<String> keys = new HashSet<>(storageMap.keySet());
        keys.forEach(key -> {
            final Instant creationTimestamp = storageMap.get(key).getCreationTimestamp();
            if (Instant.now().isAfter(creationTimestamp.plus(STORAGE_DURATION))) {
                storageMap.remove(key);
            }
        });
    }

    public Optional<EndpointDataReference> remove(final String contractAgreementId) {
        return Optional.ofNullable(storageMap.remove(contractAgreementId)).map(ExpiringContainer::getDataReference);
    }

    /**
     * Stores the data reference with its creation date.
     */
    @Value
    private static class ExpiringContainer {
        private final Instant creationTimestamp;
        private final EndpointDataReference dataReference;
    }

}

