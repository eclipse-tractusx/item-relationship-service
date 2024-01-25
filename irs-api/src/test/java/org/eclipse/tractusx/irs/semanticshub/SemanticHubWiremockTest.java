/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.semanticshub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.eclipse.tractusx.irs.configuration.SemanticsHubConfiguration;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

@WireMockTest
class SemanticHubWiremockTest {
    private static final String PROXY_SERVER_HOST = "127.0.0.1";
    private SemanticsHubFacade semanticsHubFacade;

    private static MappingBuilder getAllModels200() {
        return get(urlPathEqualTo("/models")).withHost(equalTo("semantic.hub"))
                                             .willReturn(responseWithStatus(200).withBodyFile("all-models-page.json"));
    }

    private static void verifyGetAllModels(final int times) {
        verify(exactly(times), getRequestedFor(urlPathEqualTo("/models")));
    }

    @BeforeEach
    void configureSystemUnderTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        final RestTemplate restTemplate = restTemplateProxy(PROXY_SERVER_HOST, wireMockRuntimeInfo.getHttpPort());

        final SemanticsHubConfiguration config = new SemanticsHubConfiguration();
        config.setPageSize(10);
        config.setUrl("http://semantic.hub/models");
        config.setModelJsonSchemaEndpoint("http://semantic.hub/models/{urn}/json-schema");

        final SemanticsHubClient semanticsHubClient = new SemanticsHubClientImpl(restTemplate, config);
        semanticsHubFacade = new SemanticsHubFacade(semanticsHubClient);
    }

    @Test
    void shouldReturn1Page() throws SchemaNotFoundException {
        givenThat(getAllModels200());

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).isNotEmpty();
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
        verifyGetAllModels(1);
    }

    @Test
    void shouldReturn2Pages() throws SchemaNotFoundException {
        givenThat(get(urlPathEqualTo("/models")).withHost(equalTo("semantic.hub"))
                                                .withQueryParam("page", equalTo("0"))
                                                .withQueryParam("pageSize", equalTo("10"))
                                                .willReturn(
                                                        responseWithStatus(200).withBodyFile("all-models-page1.json")));
        givenThat(get(urlPathEqualTo("/models")).withHost(equalTo("semantic.hub"))
                                                .withQueryParam("page", equalTo("1"))
                                                .withQueryParam("pageSize", equalTo("10"))
                                                .willReturn(
                                                        responseWithStatus(200).withBodyFile("all-models-page2.json")));

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).hasSize(20);
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
        verifyGetAllModels(2);
    }

    @Test
    void shouldReturnJsonSchema() throws SchemaNotFoundException {
        // Arrange
        stubFor(get(urlPathMatching("/models/urn:samm:io.catenax.batch:2.0.0%23Batch/json-schema")).withHost(
                                                                                                           equalTo("semantic.hub"))
                                                                                                   .willReturn(
                                                                                                           responseWithStatus(
                                                                                                                   200).withBodyFile(
                                                                                                                   "semantichub/batch-2.0.0-schema.json")));

        // Act
        final String modelJsonSchema = semanticsHubFacade.getModelJsonSchema("urn:samm:io.catenax.batch:2.0.0#Batch");

        // Assert
        assertThat(modelJsonSchema).contains("urn_samm_io.catenax.batch_2.0.0_CatenaXIdTrait")
                                   .contains("A batch is a quantity of (semi-) finished products or (raw) material");
        verify(exactly(1),
                getRequestedFor(urlPathMatching("/models/urn:samm:io.catenax.batch:2.0.0%23Batch/json-schema")));
    }

    @Test
    void shouldThrowSchemaExceptionWhenSchemaNotFound() {
        // Arrange
        final String url = "/models/%s/json-schema".formatted(
                "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0%23SingleLevelBomAsBuilt");
        final String errorBody = """
                {
                  "timestamp": "2024-01-24T12:06:23.390+00:00",
                  "status": 500,
                  "error": "Internal Server Error",
                  "path": "/api/v1/models/urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt/json-schema"
                }
                """;
        System.out.println(url);
        stubFor(get(urlPathEqualTo(url)).withHost(equalTo("semantic.hub"))
                                        .willReturn(responseWithStatus(500).withBody(errorBody)));

        // Act & Assert
        assertThatExceptionOfType(SchemaNotFoundException.class).isThrownBy(() -> semanticsHubFacade.getModelJsonSchema(
                "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt"));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(url)));
    }
}
