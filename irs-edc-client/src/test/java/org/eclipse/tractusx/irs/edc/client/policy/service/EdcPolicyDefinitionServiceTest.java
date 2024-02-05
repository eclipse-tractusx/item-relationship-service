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
package org.eclipse.tractusx.irs.edc.client.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.policy.model.EdcCreatePolicyDefinitionRequest;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.CreateEdcPolicyDefinitionException;
import org.eclipse.tractusx.irs.edc.client.policy.model.exception.DeleteEdcPolicyDefinitionException;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcPolicyDefinitionServiceTest {

    @Mock
    EdcConfiguration edcConfiguration;
    @Mock
    EdcConfiguration.ControlplaneConfig controlplaneConfig;
    @Mock
    EdcConfiguration.ControlplaneConfig.EndpointConfig endpointConfig;
    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private EdcPolicyDefinitionService service;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.service = new EdcPolicyDefinitionService(edcConfiguration);
    }

    @Test
    void testCreatePolicyDefinitionRequest() throws JsonProcessingException, JSONException {
        // given
        String policyName = "ID 3.0 Trace";
        String policyId = "4cc0bb57-2d64-4cfb-a13b-aceef3477b7e";

        // when
        EdcCreatePolicyDefinitionRequest request = service.createPolicyDefinition(policyName, policyId);

        // then
        JSONAssert.assertEquals(objectMapper.writeValueAsString(request), """
                        {
                        	"@context": {
                        		"odrl": "http://www.w3.org/ns/odrl/2/"
                        	},
                        	"@id": "4cc0bb57-2d64-4cfb-a13b-aceef3477b7e",
                        	"@type": "PolicyDefinitionRequestDto",
                        	"policy": {
                        		"@type": "Policy",
                        		"odrl:permission": [
                        			{
                        				"odrl:action": "USE",
                        				"odrl:constraint": {
                        					"@type": "AtomicConstraint",
                        					"odrl:or": [
                        						{
                        							"@type": "Constraint",
                        							"odrl:leftOperand": "PURPOSE",
                        							"odrl:rightOperand": "ID 3.0 Trace",
                        							"odrl:operator": {
                        								"@id": "odrl:eq"
                        							}
                        						}
                        					]
                        				}
                        			}
                        		]
                        	}
                        }
                """, false);
    }

    @Test
    void givenPolicy_WhenCreateAccessPolicy_ThenCreateIt() throws CreateEdcPolicyDefinitionException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";
        when(restTemplate.postForEntity(any(String.class), any(EdcCreatePolicyDefinitionRequest.class),
                any())).thenReturn(ResponseEntity.ok("test"));

        // when
        String result = service.createAccessPolicy(policyName, restTemplate);

        // then
        assertThat(result).isNotBlank();
    }

    @Test
    void givenCreatePolicy_whenConflict_thenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";
        when(restTemplate.postForEntity(any(String.class), any(EdcCreatePolicyDefinitionRequest.class),
                any())).thenReturn(ResponseEntity.status(HttpStatus.CONFLICT.value()).build());

        assertThrows(CreateEdcPolicyDefinitionException.class,
                () -> service.createAccessPolicy(policyName, restTemplate));
    }

    @Test
    void givenCreatePolicy_whenBadRequest_thenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";
        when(restTemplate.postForEntity(any(String.class), any(EdcCreatePolicyDefinitionRequest.class),
                any())).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build());

        assertThrows(CreateEdcPolicyDefinitionException.class,
                () -> service.createAccessPolicy(policyName, restTemplate));
    }

    @Test
    void givenCreatePolicy_whenRestClientException_thenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";
        when(restTemplate.postForEntity(any(String.class), any(EdcCreatePolicyDefinitionRequest.class),
                any())).thenThrow(new RestClientException("Surprise"));

        assertThrows(CreateEdcPolicyDefinitionException.class,
                () -> service.createAccessPolicy(policyName, restTemplate));
    }

    @Test
    void givenDeletePolicy_whenRestClientException_thenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";
        doThrow(new RestClientException("Surprise")).when(restTemplate).delete(any(String.class));

        // when/then
        assertThrows(DeleteEdcPolicyDefinitionException.class,
                () -> service.deleteAccessPolicy(policyName, restTemplate));
    }

    @Test
    void givenDeletePolicy_whenOk_thenCallRestTemplate() throws DeleteEdcPolicyDefinitionException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getPolicyDefinition()).thenReturn("/management/v2/policydefinitions");
        String policyName = "policyName";

        // when
        service.deleteAccessPolicy(policyName, restTemplate);

        // then
        verify(restTemplate, times(1)).delete(any(String.class));
    }

}
