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
package org.eclipse.tractusx.irs.policystore.persistence;

import static org.eclipse.tractusx.irs.policystore.config.PolicyConfiguration.POLICY_BLOB_PERSISTENCE;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceRuntimeException;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Persists and loads the policy data from the BLOB storage.
 */
@Service
public class PolicyPersistence {

    private final BlobPersistence policyStorePersistence;

    private final ObjectMapper mapper;

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;
    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public PolicyPersistence(@Qualifier(POLICY_BLOB_PERSISTENCE) final BlobPersistence policyStorePersistence,
            final ObjectMapper mapper) {
        this.policyStorePersistence = policyStorePersistence;
        this.mapper = mapper;
    }

    public void save(final List<String> bpns, final Policy policy) {
        if (bpns == null) {
            final List<Policy> policies = readAll(BlobPersistence.DEFAULT_BLOB_NAME);
            checkIfPolicyAlreadyExists(policy, policies);
            policies.add(policy);
            save(BlobPersistence.DEFAULT_BLOB_NAME, policies);
        } else {
            final Map<String, List<Policy>> storedBpnToPolicies = bpns.stream()
                                                                      .collect(Collectors.toMap(bpn -> bpn,
                                                                              this::readAll));
            storedBpnToPolicies.forEach((bpn, policies) -> {
                checkIfPolicyAlreadyExists(policy, policies);
                policies.add(policy);
                save(bpn, policies);
            });
        }
    }

    private static void checkIfPolicyAlreadyExists(final Policy policy, final List<Policy> policies) {
        if (policies.stream().map(Policy::getPolicyId).anyMatch(policy.getPolicyId()::equals)) {
            throw new PolicyStoreException("Policy with id '" + policy.getPolicyId() + "' already exists!");
        }
    }

    public void delete(final String bpn, final String policyId) {
        final var policies = readAll(bpn);
        final var modifiedPolicies = policies.stream().filter(p -> !p.getPolicyId().equals(policyId)).toList();
        if (policies.size() == modifiedPolicies.size()) {
            throw new PolicyStoreException("Policy with id '" + policyId + "' doesn't exists!");
        }
        save(bpn, modifiedPolicies);
    }

    private void save(final String bpn, final List<Policy> modifiedPolicies) {
        writeLock(() -> {
            try {
                policyStorePersistence.putBlob(bpn, mapper.writeValueAsBytes(modifiedPolicies));
            } catch (BlobPersistenceException | JsonProcessingException e) {
                throw new PolicyStoreException("Unable to store policy data", e);
            }
        });
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public List<Policy> readAll(final String bpn) {
        try {
            return policyStorePersistence.getBlob(bpn).map(blob -> {
                try {
                    return mapper.readerForListOf(Policy.class).<List<Policy>>readValue(blob);
                } catch (IOException | RuntimeException e) {
                    throw new PolicyStoreException("Could not read the policies from the store", e);
                }
            }).map(ArrayList::new).orElseGet(ArrayList::new);

        } catch (BlobPersistenceException e) {
            throw new PolicyStoreException("Unable to read policy data", e);
        }

    }

    public Map<String, List<Policy>> readAll() {
        try {


            return policyStorePersistence.getAllBlobs().entrySet().stream().map(entry -> {
                try {
                    return new AbstractMap.SimpleEntry<>(entry.getKey(),
                            mapper.readerForListOf(Policy.class).<List<Policy>>readValue(entry.getValue()));
                } catch (IOException e) {
                    throw new PolicyStoreException("Could not read the policies from the store", e);
                }
            }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        } catch (BlobPersistenceException | BlobPersistenceRuntimeException e) {
            throw new PolicyStoreException("Could not read the policies from the store", e);
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
