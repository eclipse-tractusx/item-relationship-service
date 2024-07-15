/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.contract.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.contract.model.EdcContractDefinition;
import org.eclipse.tractusx.irs.edc.client.contract.model.exception.CreateEdcContractDefinitionException;
import org.eclipse.tractusx.irs.edc.client.contract.model.exception.EdcContractDefinitionAlreadyExists;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.TypeFactory;

@ExtendWith(MockitoExtension.class)
class EdcContractDefinitionServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    EdcConfiguration edcConfiguration;
    @Mock
    EdcConfiguration.ControlplaneConfig controlplaneConfig;
    @Mock
    EdcConfiguration.ControlplaneConfig.EndpointConfig endpointConfig;
    private ObjectMapper objectMapper;
    private EdcContractDefinitionService service;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.service = new EdcContractDefinitionService(edcConfiguration, restTemplate);
    }

    @Test
    void testCreateContractDefinition() throws JsonProcessingException, JSONException {
        // given
        String assetId = "Asset1";
        String policyId = "Policy1";
        String contractId = "ContractId1";

        // when
        EdcContractDefinition request = service.createContractDefinitionRequest(assetId, policyId,
                contractId);

        // then
        JSONAssert.assertEquals("""
                        {
                        	"@context": {
                        		"edc": "https://w3id.org/edc/v0.0.1/ns/"
                        	},
                        	"@type": "ContractDefinition",
                        	"@id": "ContractId1",
                        	"accessPolicyId": "Policy1",
                        	"contractPolicyId": "Policy1",
                        	"assetsSelector": {
                        		"@type": "CriterionDto",
                        		"operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
                        		"operator": "=",
                        		"operandRight": "Asset1"
                        	}
                        }
                """, objectMapper.writeValueAsString(request), false);
    }

    @Test
    void givenCreateContractDefinition_whenOK_thenReturnPolicyId() throws CreateEdcContractDefinitionException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");
        String assetId = "Asset1";
        String policyId = "Policy1";
        when(restTemplate.postForEntity(any(String.class), any(EdcContractDefinition.class),
                any())).thenReturn(ResponseEntity.ok("test"));

        String result = service.createContractDefinition(assetId, policyId);

        assertThat(result).isEqualTo(policyId);
    }

    @Test
    void givenCreateContractDefinition_whenConflict_thenThrowException() throws CreateEdcContractDefinitionException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");
        final String assetId = "Asset1";
        final String policyId = "Policy1";
        when(restTemplate.postForEntity(any(String.class), any(EdcContractDefinition.class),
                any())).thenThrow(
                HttpClientErrorException.create("Surprise", HttpStatus.CONFLICT, "", null, null, null));

        // when/then
        assertThrows(EdcContractDefinitionAlreadyExists.class,
                () -> service.createContractDefinition(assetId, policyId));
    }

    @Test
    void givenCreateContractDefinition_whenBadRequest_thenThrowException() {
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");
        String assetId = "Asset1";
        String policyId = "Policy1";
        when(restTemplate.postForEntity(any(String.class), any(EdcContractDefinition.class),
                any())).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build());

        assertThrows(CreateEdcContractDefinitionException.class,
                () -> service.createContractDefinition(assetId, policyId));
    }

    @Test
    void givenCreateContractDefinition_whenRestClientException_thenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");
        String assetId = "Asset1";
        String policyId = "Policy1";
        when(restTemplate.postForEntity(any(String.class), any(EdcContractDefinition.class),
                any())).thenThrow( HttpClientErrorException.create("Surprise", HttpStatus.INTERNAL_SERVER_ERROR, "", null, null, null));

        assertThrows(CreateEdcContractDefinitionException.class,
                () -> service.createContractDefinition(assetId, policyId));
    }

    @Test
    void givenGetContractDefinitions_thenReturnContractDefinitions() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class),
                any(), any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.of(
                Optional.of(List.of(EdcContractDefinition.builder().accessPolicyId("").contractPolicyId("").build()))));

        //when
        final ResponseEntity<List<EdcContractDefinition>> contractDefinitions = service.getContractDefinitions(
                new QuerySpec());
        //then
        assertThat(contractDefinitions.getBody()).isNotEmpty();

    }

    @Test
    void givenDeleteContractDefinition_thenCallDeletionEndpoint() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getContractDefinition()).thenReturn("/management/v2/contractdefinitions");

        //when
        service.deleteContractDefinition("");

        //then
        Mockito.verify(restTemplate).delete(anyString(), anyString());

    }

    @Test
    void testCreateContractDefinitionResponse() throws IOException, JSONException {
        //GIVEN
        String jsonResponse = """
                [
                	{
                		"@id": "d355a2d5-bb3c-4fb1-b7a5-efa94f48ce73",
                		"@type": "ContractDefinition",
                		"accessPolicyId": "default-policy",
                		"contractPolicyId": "default-policy",
                		"assetsSelector": {
                			"@type": "Criterion",
                			"operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
                			"operator": "=",
                			"operandRight": "02d6977e-b71c-4873-9294-a9a15e05abb7"
                		},
                		"@context": {
                			"edc": "https://w3id.org/edc/v0.0.1/ns/"
                		}
                	}
                ]
                """;
        //WHEN
        List<EdcContractDefinition> responseObj = objectMapper.readValue(jsonResponse, TypeFactory.defaultInstance().constructCollectionType(List.class, EdcContractDefinition.class));

        //THEN
        JSONAssert.assertEquals(jsonResponse, objectMapper.writeValueAsString(responseObj), false);
    }
}
