/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisHealthIndicatorTest {

    @Test
    void shouldReturnStatusUpWhenRedisIsUp() {
        // given
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        final EndpointDataReferenceStorage dataReferenceStorage = new EndpointDataReferenceStorage(null, true, redisTemplate);
        final RedisHealthIndicator redisHealthIndicator = new RedisHealthIndicator(dataReferenceStorage);

        // when
        final Health health = redisHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnStatusDownWhenRedisIsDown() {
        // given
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn(null);

        final EndpointDataReferenceStorage dataReferenceStorage = new EndpointDataReferenceStorage(null, true, redisTemplate);
        final RedisHealthIndicator redisHealthIndicator = new RedisHealthIndicator(dataReferenceStorage);

        // when
        final Health health = redisHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void shouldReturnStatusUpWhenRedisIsUnused() {
        // given
        final EndpointDataReferenceStorage dataReferenceStorage = new EndpointDataReferenceStorage(null, false, null);
        final RedisHealthIndicator redisHealthIndicator = new RedisHealthIndicator(dataReferenceStorage);

        // when
        final Health health = redisHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("status")).isEqualTo("not in use");
    }

}
