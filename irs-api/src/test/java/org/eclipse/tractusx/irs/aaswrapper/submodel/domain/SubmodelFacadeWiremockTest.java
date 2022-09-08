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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.retry.RetryRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class SubmodelFacadeWiremockTest {

    private final static String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
    private final JsonUtil jsonUtil = new JsonUtil();

    private final OutboundMeterRegistryService meterRegistry = mock(OutboundMeterRegistryService.class);
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    private final RestTemplate restTemplate = new RestTemplate();
    private WireMockServer wireMockServer;
    private SubmodelFacade submodelFacade;

    @BeforeEach
    void configureSystemUnderTest() {
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();
        configureFor(this.wireMockServer.port());
        SubmodelClient submodelClient = new SubmodelClientImpl(restTemplate, buildApiMethodUrl() + "/api/service",
                jsonUtil, meterRegistry, retryRegistry);
        this.submodelFacade = new SubmodelFacade(submodelClient);
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipAsString() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("assemblyPartRelationship.json")));

        // Act
        final String submodel = submodelFacade.getSubmodelRawPayload(url);

        // Assert
        assertThat(submodel).contains("\"catenaXId\": \"urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978\"");
    }

    @Test
    void shouldReturnMaterialForRecyclingAsString() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("materialForRecycling.json")));

        // Act
        final String submodel = submodelFacade.getSubmodelRawPayload(url);

        // Assert
        assertThat(submodel).contains("\"materialName\": \"Cooper\",");
    }

    @Test
    void shouldReturnObjectAsStringWhenResponseNotJSON() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("test")));

        // Act
        final String submodel = submodelFacade.getSubmodelRawPayload(url);

        // Assert
        assertThat(submodel).isEqualTo("test");
    }

    @Test
    void shouldThrowExceptionWhenResponse_400() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(400)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("{ error: '400'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelFacade.getSubmodelRawPayload(url);

        // Assert
        assertThatExceptionOfType(RestClientException.class).isThrownBy(throwingCallable);
    }

    @Test
    void shouldThrowExceptionWhenResponse_500() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(500)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("{ error: '500'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelFacade.getSubmodelRawPayload(url);

        // Assert
        assertThatExceptionOfType(RestClientException.class).isThrownBy(throwingCallable);
    }

    private String buildApiMethodUrl() {
        return String.format("http://localhost:%d", this.wireMockServer.port());
    }
}
