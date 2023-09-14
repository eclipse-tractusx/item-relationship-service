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
package org.eclipse.tractusx.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import java.util.Objects;

import org.eclipse.tractusx.irs.configuration.SemanticsHubConfiguration;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class SemanticsHubClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    @NotNull
    private static ResponseEntity<PaginatedResponse<AspectModel>> getResponseEntity(
            final PaginatedResponse<AspectModel> aspectModelResponse2) {
        return new ResponseEntity<>(aspectModelResponse2, HttpStatus.OK);
    }

    private SemanticsHubConfiguration config(final String schemaEndpoint, final String path) {
        final SemanticsHubConfiguration config = new SemanticsHubConfiguration();
        config.setModelJsonSchemaEndpoint(schemaEndpoint);
        config.setLocalModelDirectory(path);
        return config;
    }

    private SemanticsHubConfiguration config(final String semHubURL, final int pageSize, final String schemaEndpoint,
            final String path) {
        final SemanticsHubConfiguration config = config(schemaEndpoint, path);
        config.setUrl(semHubURL);
        config.setPageSize(pageSize);
        return config;
    }

    @Test
    void shouldCallExternalServiceOnceAndGetJsonSchema() throws SchemaNotFoundException {
        final var testee = new SemanticsHubClientImpl(restTemplate, config("url/{urn}", ""));
        final String jsonSchemaMock = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"integer\"}";
        doReturn(jsonSchemaMock).when(restTemplate).getForObject(any(), eq(String.class));

        final String resultJsonSchema = testee.getModelJsonSchema("urn");

        assertThat(resultJsonSchema).isNotBlank().contains("http://json-schema.org/draft-07/schema#");
        verify(this.restTemplate, times(1)).getForObject(any(), eq(String.class));
    }

    @Test
    void shouldReadJsonSchemaFromFilesystemOnly() throws SchemaNotFoundException {
        final String path = Objects.requireNonNull(
                getClass().getResource("/json-schema/assemblyPartRelationship-v1.1.0.json")).getPath();

        final var testee = new SemanticsHubClientImpl(restTemplate, config("", new File(path).getParent()));

        final String resultJsonSchema = testee.getModelJsonSchema("assemblyPartRelationship-v1.1.0.json");

        assertThat(resultJsonSchema).isNotBlank().contains("http://json-schema.org/draft-04/schema");
    }

    @Test
    void shouldReadJsonSchemaFromSemanticHubThenFilesystem() throws SchemaNotFoundException {
        final String path = Objects.requireNonNull(
                getClass().getResource("/json-schema/assemblyPartRelationship-v1.1.0.json")).getPath();

        doThrow(HttpClientErrorException.class).when(restTemplate).getForObject(any(), eq(String.class));

        final var testee = new SemanticsHubClientImpl(restTemplate, config("url/{urn}", new File(path).getParent()));

        final String resultJsonSchema = testee.getModelJsonSchema("assemblyPartRelationship-v1.1.0.json");

        assertThat(resultJsonSchema).isNotBlank().contains("http://json-schema.org/draft-04/schema");
    }

    @Test
    void shouldThrowExceptionIfNothingConfigured() {
        final var testee = new SemanticsHubClientImpl(restTemplate, config("", ""));

        assertThatThrownBy(() -> testee.getModelJsonSchema("assemblyPartRelationship-v1.1.0.json")).isInstanceOf(
                SchemaNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionIfNothingFound() {
        final String path = Objects.requireNonNull(
                getClass().getResource("/json-schema/assemblyPartRelationship-v1.1.0.json")).getPath();

        final var testee = new SemanticsHubClientImpl(restTemplate, config("url/{urn}", new File(path).getParent()));

        doThrow(HttpClientErrorException.class).when(restTemplate).getForObject(any(), eq(String.class));

        assertThatThrownBy(() -> testee.getModelJsonSchema("doesnotexist-v1.1.0.json")).isInstanceOf(
                SchemaNotFoundException.class);
    }

    @Test
    void shouldReturnAspectModels() throws SchemaNotFoundException {
        // Arrange
        final var testee = new SemanticsHubClientImpl(restTemplate, config("url", 1, "url/{urn}", ""));
        final List<AspectModel> aspectModels1 = List.of(
                new AspectModel("urn1", "version1", "name1", "type1", "status1"));
        final List<AspectModel> aspectModels2 = List.of(
                new AspectModel("urn2", "version2", "name2", "type2", "status2"));
        final PaginatedResponse<AspectModel> aspectModelResponse1 = new PaginatedResponse<>(aspectModels1, 2, 0);
        final PaginatedResponse<AspectModel> aspectModelResponse2 = new PaginatedResponse<>(aspectModels2, 2, 1);

        doReturn(getResponseEntity(aspectModelResponse1), getResponseEntity(aspectModelResponse2)).when(restTemplate)
                                                                                                  .exchange(any(),
                                                                                                          (ParameterizedTypeReference<Object>) any());

        // Act
        final List<AspectModel> allAspectModels = testee.getAllAspectModels();

        // Assert
        assertThat(allAspectModels).isNotEmpty();
        assertThat(allAspectModels.get(0).name()).isEqualTo("name1");
        assertThat(allAspectModels.get(1).name()).isEqualTo("name2");
        verify(this.restTemplate, times(2)).exchange(any(), (ParameterizedTypeReference<Object>) any());
    }

    @Test
    void shouldGetAllModelsFromFilesystemOnly() throws SchemaNotFoundException {
        // Arrange
        final String path = Objects.requireNonNull(getClass().getResource("/aspect-models/")).getPath();
        final var testee = new SemanticsHubClientImpl(restTemplate, config("", new File(path).getPath()));
        final AspectModel serialPartTypization = new AspectModel(
                "urn:bamm:io.catenax.serial_part_typization:1.0.0#SerialPartTypization", "1.0.0",
                "SerialPartTypization", SemanticsHubClientImpl.LOCAL_MODEL_TYPE,
                SemanticsHubClientImpl.LOCAL_MODEL_STATUS);

        // Act
        final List<AspectModel> allAspectModels = testee.getAllAspectModels();

        // Assert
        assertThat(allAspectModels).hasSize(1).contains(serialPartTypization);
    }

    @Test
    void shouldReturnEmptyIfLocalModelFilesNotAccessible() throws SchemaNotFoundException {
        // Arrange
        final var testee = new SemanticsHubClientImpl(restTemplate, config("", ""));

        // Act
        final List<AspectModel> allAspectModels = testee.getAllAspectModels();

        // Assert
        assertThat(allAspectModels).isEmpty();
    }
}
