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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.policystore.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.springframework.stereotype.Service;

/**
 * Persists and loads the policy data from the BLOB storage.
 */
@Service
@RequiredArgsConstructor
public class PolicyPersistence {
    private final BlobPersistence persistence;

    private final ObjectMapper mapper;

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;
    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void save(final String bpn, final Policy policy) {
        final var policies = readAll(bpn);
        if (policies.stream().map(Policy::policyId).anyMatch(policy.policyId()::equals)) {
            throw new PolicyStoreException("Policy with id '" + policy.policyId() + "' already exists!");
        }
        policies.add(policy);
        save(bpn, policies);
    }

    public void delete(final String bpn, final String policyId) {
        final var policies = readAll(bpn);
        final var modifiedPolicies = policies.stream().filter(p -> !p.policyId().equals(policyId)).toList();
        save(bpn, modifiedPolicies);
    }

    private void save(final String bpn, final List<Policy> modifiedPolicies) {
        writeLock(() -> {
            try {
                persistence.putBlob(bpn, mapper.writeValueAsBytes(modifiedPolicies));
            } catch (BlobPersistenceException | JsonProcessingException e) {
                throw new PolicyStoreException("Unable to store policy data", e);
            }
        });
    }

    public List<Policy> readAll(final String bpn) {
        try {
            return persistence.getBlob(bpn).map(blob -> {
                try {
                    return mapper.readerForListOf(Policy.class).<List<Policy>>readValue(blob);
                } catch (IOException e) {
                    throw new PolicyStoreException("Could not read the policies from the store", e);
                }
            }).map(ArrayList::new).orElseGet(ArrayList::new);

        } catch (BlobPersistenceException e) {
            throw new PolicyStoreException("Unable to read policy data", e);
        }

    }

    private void writeLock(final Runnable work) {
        try {
            if (!lock.writeLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new PolicyStoreException("Timeout acquiring write lock");
            }
            try {
                work.run();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyStoreException("Interrupted while storing policy data", e);
        }
    }
}
