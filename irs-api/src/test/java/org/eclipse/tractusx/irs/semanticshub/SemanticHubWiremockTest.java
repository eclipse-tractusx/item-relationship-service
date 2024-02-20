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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport.SEMANTIC_HUB_SCHEMA_URL;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport.semanticHubWillReturnBatchSchema;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;

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

    @BeforeEach
    void configureSystemUnderTest(WireMockRuntimeInfo wireMockRuntimeInfo) {
        final RestTemplate restTemplate = restTemplateProxy(PROXY_SERVER_HOST, wireMockRuntimeInfo.getHttpPort());

        final SemanticsHubConfiguration config = new SemanticsHubConfiguration();
        config.setPageSize(10);
        config.setUrl("http://semantic.hub/models");
        config.setModelJsonSchemaEndpoint(SEMANTIC_HUB_SCHEMA_URL);

        final SemanticsHubClient semanticsHubClient = new SemanticsHubClientImpl(restTemplate, config);
        semanticsHubFacade = new SemanticsHubFacade(semanticsHubClient);
    }

    @Test
    void shouldReturn1Page() throws SchemaNotFoundException {
        SemanticHubWireMockSupport.semanticHubWillReturnAllModels("all-models-page.json");

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).isNotEmpty();
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
        SemanticHubWireMockSupport.verifySemanticHubWasCalledForAllModels(1);
    }

    @Test
    void shouldReturn2Pages() throws SchemaNotFoundException {
        SemanticHubWireMockSupport.semanticHubWillReturnPagedModels(0, 10, "all-models-page1.json");
        SemanticHubWireMockSupport.semanticHubWillReturnPagedModels(1, 10, "all-models-page2.json");

        final AspectModels allAspectModels = semanticsHubFacade.getAllAspectModels();

        assertThat(allAspectModels.models()).hasSize(20);
        assertThat(allAspectModels.models().get(0).name()).isEqualTo("SerialPartTypization");
        SemanticHubWireMockSupport.verifySemanticHubWasCalledForAllModels(2);
    }

    @Test
    void shouldReturnJsonSchema() throws SchemaNotFoundException {
        // Arrange
        semanticHubWillReturnBatchSchema();

        // Act
        final String modelJsonSchema = semanticsHubFacade.getModelJsonSchema("urn:samm:io.catenax.batch:2.0.0#Batch");

        // Assert
        assertThat(modelJsonSchema).contains("urn_samm_io.catenax.batch_2.0.0_CatenaXIdTrait")
                                   .contains("A batch is a quantity of (semi-) finished products or (raw) material");
        SemanticHubWireMockSupport.verifySemanticHubWasCalledForModel("urn:samm:io.catenax.batch:2.0.0%23Batch", 1);
    }

    @Test
    void shouldThrowSchemaExceptionWhenSchemaNotFound() {
        // Arrange
        final String semanticModel = "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0%23SingleLevelBomAsBuilt";
        SemanticHubWireMockSupport.semanticHubWillThrowErrorForSemanticModel(semanticModel);

        // Act & Assert
        assertThatExceptionOfType(SchemaNotFoundException.class).isThrownBy(() -> semanticsHubFacade.getModelJsonSchema(
                "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt"));
        SemanticHubWireMockSupport.verifySemanticHubWasCalledForModel(semanticModel, 1);
    }

}
