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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterFilter;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SubmodelFacadeTest {

    private final JsonUtil jsonUtil = new JsonUtil();

    private final OutboundMeterRegistryService meterRegistry = mock(OutboundMeterRegistryService.class);
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    @Mock
    RestTemplate restTemplate;

    private SubmodelFacade submodelFacade;

    @BeforeEach
    void setUp() {
        final SubmodelClientLocalStub submodelClientStub = new SubmodelClientLocalStub();
        submodelFacade = new SubmodelFacade(submodelClientStub);
    }

    @Test
    void shouldThrowExceptionWhenSubmodelNotFound() {
        final JobParameter jobParameter = jobParameter();
        final String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate(),
                "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry);
        final SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelFacade.getRelationships(url, jobParameter));
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithChildDataWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId, jobParameter());

        assertThat(submodelResponse.get(0).getCatenaXId().getGlobalAssetId()).isEqualTo(catenaXId);
        assertThat(submodelResponse).hasSize(3);

        final List<String> childIds = submodelResponse.stream()
                                                .map(Relationship::getLinkedItem)
                                                .map(LinkedItem::getChildCatenaXId)
                                                .map(GlobalAssetIdentification::getGlobalAssetId)
                                                .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4",
                "urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc", "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
    }

    @Test
    void shouldReturnFilteredAssemblyPartRelationshipWithoutChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId, jobParameterFilter());

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipDTOWhenRequestingOnRealClient() {
        final String endpointUrl = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(restTemplate,
                "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry);
        SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);

        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship();
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        assemblyPartRelationship.setCatenaXId(catenaXId);
        assemblyPartRelationship.setChildParts(new HashSet<>());

        final String jsonObject = jsonUtil.asString(assemblyPartRelationship);
        final ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonObject, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(URI.class), any());

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(endpointUrl,
                jobParameter());

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnStringWhenRequestingSubmodelWithoutAspect() {
        final String catenaXId = "urn:uuid:ea724f73-cb93-4b7b-b92f-d97280ff888b";

        final String submodelResponse = submodelFacade.getSubmodelRawPayload(catenaXId);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AYRE\",\"key\":\"ManufacturerID\"}");
    }
}