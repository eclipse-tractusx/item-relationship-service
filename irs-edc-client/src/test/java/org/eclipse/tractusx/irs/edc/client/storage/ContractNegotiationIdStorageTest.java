/********************************************************************************
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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ContractNegotiationIdStorageTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
            .withExposedPorts(6379);

    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setupRedisTemplate() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                redisContainer.getHost(), redisContainer.getFirstMappedPort());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(factory);
    }

    @Test
    void shouldStoreAndRetrieveUsingRedis() {
        ContractNegotiationIdStorage storage = new ContractNegotiationIdStorage(
                Duration.ofSeconds(5), true, redisTemplate);

        storage.put("redis-key", "test-data");

        Optional<String> result = storage.get("redis-key");

        assertThat(result).isPresent();
        assertThat(result.get()).contains("test-data");
    }

    @Test
    void shouldExpireInRedis() {
        ContractNegotiationIdStorage storage = new ContractNegotiationIdStorage(
                Duration.ofSeconds(1), true, redisTemplate);

        storage.put("temp-redis", "test-data");

        Awaitility.await()
                  .atMost(2, TimeUnit.SECONDS)
                  .until(() -> storage.get("temp-redis").isEmpty());

        Optional<String> result = storage.get("temp-redis");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldStoreAndRetrieveInMemory() {
        ContractNegotiationIdStorage storage = new ContractNegotiationIdStorage(
                Duration.ofSeconds(5), false, null);

        storage.put("memory-key", "test-data");

        Optional<String> result = storage.get("memory-key");

        assertThat(result).isPresent();
        assertThat(result.get()).contains("test-data");
    }
}