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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * In-memory storage to map between contract negotiation id and contract agreement id.
 */
@Service("irsEdcClientContractNegotiationIdStorage")
public class ContractNegotiationIdStorage {

    @SuppressWarnings({ "PMD.ImmutableField" })
    private ExpiringStorage<String> localStorage;
    private final StringRedisTemplate redisTemplate;

    private final Duration duration;
    private final boolean useRedis;

    public ContractNegotiationIdStorage(
            @Value("${irs-edc-client.controlplane.datareference.storage.duration}") final Duration duration,
            @Value("${irs-edc-client.controlplane.datareference.storage.useRedis:false}") final boolean useRedis,
            @Autowired(required = false) final StringRedisTemplate redisTemplate) {
        this.duration = duration;
        this.useRedis = useRedis;
        this.redisTemplate = redisTemplate;

        if (!useRedis) {
            this.localStorage = new ExpiringStorage<>(duration);
        }
    }

    public void put(final String contractNegotiationId, final String contractAgreementId) {
        if (useRedis) {
            redisTemplate.opsForValue().set(contractNegotiationId, contractAgreementId, duration);
        } else {
            localStorage.put(contractNegotiationId, contractAgreementId);
        }
    }

    public Optional<String> get(final String contractNegotiationId) {
        if (useRedis) {
            return Optional.ofNullable(redisTemplate.opsForValue().get(contractNegotiationId));
        } else {
            return localStorage.get(contractNegotiationId);
        }
    }
}
