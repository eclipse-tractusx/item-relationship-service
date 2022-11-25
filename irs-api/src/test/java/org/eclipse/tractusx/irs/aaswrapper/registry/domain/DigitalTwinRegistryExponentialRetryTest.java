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
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class DigitalTwinRegistryExponentialRetryTest {

    @Autowired
    private DigitalTwinRegistryClient digitalTwinRegistryClient;

    @MockBean
    @Qualifier("oAuthRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    @Test
    void shouldRetryExecutionOfShellRetrievalMaxAttemptTimes() {
        // Arrange
        given(restTemplate.getForObject(any(), any())).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        // Act
        assertThrows(HttpServerErrorException.class,
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("urn:uuid:12345"));

        // Assert
        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).getForObject(any(), any());
    }

    @Test
    void shouldNotRetryExecutionOfShellRetrievalForNotFoundException() {
        // Arrange
        given(restTemplate.getForObject(any(), any())).willThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        // Act
        assertThrows(HttpClientErrorException.NotFound.class,
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("urn:uuid:12345"));

        // Assert
        verify(restTemplate, times(1)).getForObject(any(), any());
    }
}
