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
package org.eclipse.tractusx.edc;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.internal.InMemoryRetryRegistry;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles(profiles = { "test" })
//@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class SubmodelExponentialRetryTest {

    @Mock
    private EdcSubmodelFacade submodelClient;

    @Mock
//    @Qualifier(EDC_REST_TEMPLATE)
    private RestTemplate restTemplate;

//    @Autowired
    private RetryRegistry retryRegistry = new InMemoryRetryRegistry();


//    @Test
//    void shouldRetryExecutionOfGetSubmodelOnClientMaxAttemptTimes() {
//        // Arrange
//        given(restTemplate.exchange(any(), any(), any(), eq(Catalog.class), any(), any(), any())).willThrow(
//                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "AASWrapper remote exception"));
//
//        // Act
//        assertThatThrownBy(() -> submodelClient.getSubmodelRawPayload(
//                "http://test.com/urn:uuid:12345/submodel?content=value")).hasCauseInstanceOf(
//                HttpServerErrorException.class);
//
//        // Assert
//        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).exchange(any(), any(), any(),
//                eq(Catalog.class), any(), any(), any());
//    }
//
//    @Test
//    void shouldRetryOnAnyRuntimeException() {
//        // Arrange
//        given(restTemplate.exchange(any(), any(), any(), eq(Catalog.class), any(), any(), any())).willThrow(
//                new RuntimeException("AASWrapper remote exception"));
//
//        // Act
//        assertThatThrownBy(() -> submodelClient.getSubmodelRawPayload(
//                "http://test.com/urn:uuid:12345/submodel?content=value")).hasCauseInstanceOf(RuntimeException.class);
//
//        // Assert
//        verify(restTemplate, times(retryRegistry.getDefaultConfig().getMaxAttempts())).exchange(any(), any(), any(),
//                eq(Catalog.class), any(), any(), any());
//    }



}
