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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.eclipse.tractusx.irs.util.LocalTestDataConfigurationAware;
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
class SubmodelFacadeTest extends LocalTestDataConfigurationAware {

    private final JsonUtil jsonUtil = new JsonUtil();

    private final OutboundMeterRegistryService meterRegistry = mock(OutboundMeterRegistryService.class);
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    @Mock
    RestTemplate restTemplate;

    private SubmodelFacade submodelFacade;

    SubmodelFacadeTest() throws IOException {
        super();
    }

    @BeforeEach
    void setUp() throws IOException {
        final SubmodelClientLocalStub submodelClientStub = new SubmodelClientLocalStub(jsonUtil, localTestDataConfiguration.cxTestDataContainer());
        submodelFacade = new SubmodelFacade(submodelClientStub, null);
    }

    @Test
    void shouldThrowExceptionWhenSubmodelNotFound() {
        final String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate(),
                "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry);
        final SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient, null);

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelFacade.getRelationships(url, RelationshipAspect.AssemblyPartRelationship));
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndAssemblyPartRelationship()
            throws ExecutionException, InterruptedException, EdcClientException {
        final String catenaXId = "urn:uuid:a4a2ba57-1c50-48ad-8981-7a0ef032146b";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId + "_assemblyPartRelationship", RelationshipAspect.AssemblyPartRelationship);

        assertThat(submodelResponse.get(0).getCatenaXId().getGlobalAssetId()).isEqualTo(catenaXId);
        assertThat(submodelResponse).hasSize(32);

        final List<String> childIds = submodelResponse.stream()
                                                .map(Relationship::getLinkedItem)
                                                .map(LinkedItem::getChildCatenaXId)
                                                .map(GlobalAssetIdentification::getGlobalAssetId)
                                                .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("urn:uuid:0d8da814-fcee-4b5e-b251-fc400da32399",
                "urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873", "urn:uuid:0d039178-90a1-4329-843e-04ca0475a56e");
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelBomAsPlanned()
            throws ExecutionException, InterruptedException, EdcClientException {
        final String catenaXId = "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId + "_singleLevelBomAsPlanned", RelationshipAspect.SingleLevelBomAsPlanned);

        assertThat(submodelResponse.get(0).getCatenaXId().getGlobalAssetId()).isEqualTo(catenaXId);
        assertThat(submodelResponse).hasSize(1);

        final List<String> childIds = submodelResponse.stream()
                                                      .map(Relationship::getLinkedItem)
                                                      .map(LinkedItem::getChildCatenaXId)
                                                      .map(GlobalAssetIdentification::getGlobalAssetId)
                                                      .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97");
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelUsageAsBuilt()
            throws ExecutionException, InterruptedException, EdcClientException {
        final String catenaXId = "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId + "_singleLevelUsageAsBuilt", RelationshipAspect.SingleLevelUsageAsBuilt);

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithNotExistingCatenaXIdAndAssemblyPartRelationship()
            throws ExecutionException, InterruptedException, EdcClientException {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(
                catenaXId + "_assemblyPartRelationship", RelationshipAspect.AssemblyPartRelationship);

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingOnRealClient()
            throws ExecutionException, InterruptedException, EdcClientException {
        final String endpointUrl = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(restTemplate,
                "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry);
        SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient, null);

        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", new HashSet<>());

        final String jsonObject = jsonUtil.asString(assemblyPartRelationship);
        final ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonObject, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(URI.class), any());

        final List<Relationship> submodelResponse = submodelFacade.getRelationships(endpointUrl,
                RelationshipAspect.AssemblyPartRelationship);

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnRawSerialPartTypizationWhenExisting() {
        final String catenaXId = "urn:uuid:7eb7daf6-0c54-455b-aab7-bd5ca252f6ee";

        final String submodelResponse = submodelFacade.getSubmodelRawPayload(catenaXId + "_serialPartTypization");

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AYRE\",\"key\":\"manufacturerId\"}");
    }
}