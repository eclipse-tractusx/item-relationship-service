/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.tractusx.irs.configuration.SemanticsHubConfiguration;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class SemanticHubWiremockTest {
    private WireMockServer wireMockServer;

    private SemanticsHubFacade semanticsHubFacade;
    private SemanticsHubConfiguration config;

    @BeforeEach
    void configureSystemUnderTest() {
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();
        configureFor(this.wireMockServer.port());

        config = new SemanticsHubConfiguration();
        config.setPageSize(10);
        config.setUrl(String.format("http://localhost:%d/models", this.wireMockServer.port()));
        config.setModelJsonSchemaEndpoint("sem.hub/models/{urn}/json-schema");

        final RestTemplate restTemplate = new RestTemplate();
        final SemanticsHubClient semanticsHubClient = new SemanticsHubClientImpl(restTemplate, config);
        semanticsHubFacade = new SemanticsHubFacade(semanticsHubClient);
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void shouldReturn1Page() throws SchemaNotFoundException {
        givenThat(get(urlPathEqualTo("/models")).willReturn(aResponse().withStatus(200)
                                                                       .withHeader("Content-Type",
                                                                               "application/json;charset=UTF-8")
                                                                       .withBodyFile("all-models-page.json")));

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).isNotEmpty();
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
    }

    @Test
    void shouldReturn2Pages() throws SchemaNotFoundException {
        givenThat(get(urlPathEqualTo("/models")).withQueryParam("page", equalTo("0"))
                                                .withQueryParam("pageSize", equalTo("10"))
                                                .willReturn(aResponse().withStatus(200)
                                                                       .withHeader("Content-Type",
                                                                               "application/json;charset=UTF-8")
                                                                       .withBodyFile("all-models-page1.json")));
        givenThat(get(urlPathEqualTo("/models")).withQueryParam("page", equalTo("1"))
                                                .withQueryParam("pageSize", equalTo("10"))
                                                .willReturn(aResponse().withStatus(200)
                                                                       .withHeader("Content-Type",
                                                                               "application/json;charset=UTF-8")
                                                                       .withBodyFile("all-models-page2.json")));

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).hasSize(20);
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
    }
}
