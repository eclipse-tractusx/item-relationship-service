/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Value;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;

@Value
public class InMemoryBlobStore implements BlobPersistence {

    Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void putBlob(final String targetBlobName, final byte[] blob) {
        store.put(targetBlobName, blob);
    }

    @Override
    public Optional<byte[]> getBlob(final String sourceBlobName) {
        return Optional.ofNullable(store.get(sourceBlobName));
    }

    @Override
    public Map<String, byte[]> getAllBlobs() {
        return store;
    }

    @Override
    public Collection<byte[]> findBlobByPrefix(final String prefix) {
        return store.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
    }

    @Override
    public boolean delete(final String jobId, final List<String> processIds) {
        processIds.forEach(store::remove);
        return store.remove(jobId) != null;
    }
}
