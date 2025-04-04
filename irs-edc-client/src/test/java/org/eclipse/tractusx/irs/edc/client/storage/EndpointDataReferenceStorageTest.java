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

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class EndpointDataReferenceStorageTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
            .withExposedPorts(6379);

    private StringRedisTemplate redisTemplate;

    private EndpointDataReference sampleData() {
        return EndpointDataReference.Builder.newInstance()
                                            .id("sample-id")
                                            .endpoint("http://example.com")
                                            .authKey("Authorization")
                                            .authCode("token")
                                            .contractId("asdf")
                                            .build();
    }

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
        EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(
                Duration.ofSeconds(5), true, redisTemplate);

        EndpointDataReference data = sampleData();
        storage.put("redis-key", data);

        Optional<EndpointDataReference> result = storage.get("redis-key");

        assertThat(result).isPresent();
        assertThat(result.get().getEndpoint()).isEqualTo(data.getEndpoint());
    }

    @Test
    void shouldExpireInRedis() throws InterruptedException {
        EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(
                Duration.ofSeconds(1), true, redisTemplate);

        storage.put("temp-redis", sampleData());

        Thread.sleep(2000);

        Optional<EndpointDataReference> result = storage.get("temp-redis");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldClearRedis() {
        var storage = new EndpointDataReferenceStorage(Duration.ofSeconds(10), true, redisTemplate);

        storage.put("key1", sampleData());
        storage.put("key2", sampleData());

        assertThat(storage.get("key1")).isPresent();
        assertThat(storage.get("key2")).isPresent();

        storage.clear();

        assertThat(storage.get("key1")).isEmpty();
        assertThat(storage.get("key2")).isEmpty();
    }

    @Test
    void shouldStoreAndRetrieveInMemory() {
        EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(
                Duration.ofSeconds(5), false, null);

        EndpointDataReference data = sampleData();
        storage.put("memory-key", data);

        Optional<EndpointDataReference> result = storage.get("memory-key");

        assertThat(result).isPresent();
        assertThat(result.get().getAuthCode()).isEqualTo(data.getAuthCode());
    }

    @Test
    void shouldClearInMemoryStorage() {
        var storage = new EndpointDataReferenceStorage(Duration.ofSeconds(5), false, null);

        storage.put("mem1", sampleData());
        storage.put("mem2", sampleData());

        assertThat(storage.get("mem1")).isPresent();
        assertThat(storage.get("mem2")).isPresent();

        storage.clear();

        assertThat(storage.get("mem1")).isEmpty();
        assertThat(storage.get("mem2")).isEmpty();
    }
}