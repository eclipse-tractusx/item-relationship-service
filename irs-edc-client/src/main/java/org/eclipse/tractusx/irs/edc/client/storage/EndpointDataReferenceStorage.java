/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.storage;

import java.time.Duration;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Storage for endpoint data references. Either in-memory or in Redis, depending on config
 * Values are held either by assetId or contractAgreementId.
 */
@Service("irsEdcClientEndpointDataReferenceStorage")
@Slf4j
public class EndpointDataReferenceStorage {

    @SuppressWarnings({ "PMD.ImmutableField" })
    private ExpiringStorage<EndpointDataReference> localStorage;
    private final StringRedisTemplate redisTemplate;

    private final Duration duration;
    @Getter
    private final boolean useRedis;

    public EndpointDataReferenceStorage(
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

    public void put(final String contractNegotiationId, final EndpointDataReference dataReference) {
        if (useRedis) {
            redisTemplate.opsForValue().set(contractNegotiationId, StringMapper.mapToString(dataReference), duration);
        } else {
            localStorage.put(contractNegotiationId, dataReference);
        }
    }

    public Optional<EndpointDataReference> get(final String storageId) {
        if (useRedis) {
            final String value = redisTemplate.opsForValue().get(storageId);
            if (value != null) {
                return Optional.of(StringMapper.mapFromString(value, EndpointDataReference.class));
            } else {
                return Optional.empty();
            }
        } else {
            return localStorage.get(storageId);
        }
    }

    public void clear() {
        if (useRedis) {
            redisTemplate.execute((RedisCallback<?>) connection -> {
                connection.serverCommands().flushDb();
                return null;
            });
        } else {
            localStorage.clear();
        }
    }

    public boolean checkRedisConnection() {
        if (!useRedis || redisTemplate == null) {
            return false;
        }

        final RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        return connectionFactory != null && "PONG".equals(connectionFactory.getConnection().ping());
    }
}
