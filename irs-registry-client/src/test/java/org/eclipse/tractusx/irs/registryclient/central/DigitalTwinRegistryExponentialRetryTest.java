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
package org.eclipse.tractusx.irs.registryclient.central;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryExponentialRetryTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final String CONFIG_NAME = "registry";
    private DigitalTwinRegistryClient digitalTwinRegistryClient;

    @Mock
    private RestTemplate restTemplate;

    private final RetryRegistry retryRegistry = new InMemoryRetryRegistry();

    @BeforeEach
    void setUp() {
        retryRegistry.addConfiguration(CONFIG_NAME, RetryConfig.from(RetryConfig.ofDefaults())
                                                               .maxAttempts(MAX_ATTEMPTS)
                                                               .ignoreExceptions(
                                                                       HttpClientErrorException.NotFound.class)
                                                               .build());

        digitalTwinRegistryClient = new DigitalTwinRegistryClientImpl(restTemplate, "http://localhost/{aasIdentifier}",
                "http://localhost?{assetIds}");
    }

    @Test
    void shouldRetryExecutionOfShellRetrievalMaxAttemptTimes() {
        // Arrange
        given(restTemplate.getForObject(any(), any())).willThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));

        // Act
        final var retry = retryRegistry.retry(CONFIG_NAME, CONFIG_NAME);
        assertThrows(HttpServerErrorException.class, () -> retry.executeCallable(
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("urn:uuid:12345")));

        // Assert
        verify(restTemplate, times(retryRegistry.getConfiguration(CONFIG_NAME)
                                                .map(RetryConfig::getMaxAttempts)
                                                .orElse(MAX_ATTEMPTS))).getForObject(any(), any());
    }

    @Test
    void shouldNotRetryExecutionOfShellRetrievalForNotFoundException() {
        // Arrange
        given(restTemplate.getForObject(any(), any())).willThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", new HttpHeaders(), new byte[0],
                        StandardCharsets.UTF_8));

        // Act
        final var retry = retryRegistry.retry(CONFIG_NAME, CONFIG_NAME);
        assertThrows(HttpClientErrorException.NotFound.class, () -> retry.executeCallable(
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("urn:uuid:12345")));

        // Assert
        verify(restTemplate, times(1)).getForObject(any(), any());
    }
}
