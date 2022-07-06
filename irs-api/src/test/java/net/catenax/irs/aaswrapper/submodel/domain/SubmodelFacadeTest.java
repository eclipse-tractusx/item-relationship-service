/*
 * Copyright (c) 2022. Copyright Holder (Catena-X Consortium)
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * See the LICENSE file(s) distributed with this work for
 * additional information regarding license terms.
 *
 */
package net.catenax.irs.aaswrapper.submodel.domain;

import static net.catenax.irs.util.TestMother.jobParameter;
import static net.catenax.irs.util.TestMother.jobParameterFilter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.util.JsonUtil;
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
        final String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate(),
                "http://aaswrapper:9191/api/service", jsonUtil);
        final SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelFacade.getSubmodel(url, jobParameter()));
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithChildDataWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssemblyPartRelationshipDTO submodelResponse = submodelFacade.getSubmodel(catenaXId, jobParameter());

        assertThat(submodelResponse.getCatenaXId()).isEqualTo(catenaXId);

        final Set<ChildDataDTO> childParts = submodelResponse.getChildParts();
        assertThat(childParts).hasSize(3);

        final List<String> childIds = childParts.stream()
                                                .map(ChildDataDTO::getChildCatenaXId)
                                                .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4",
                "urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc", "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
    }

    @Test
    void shouldReturnFilteredAssemblyPartRelationshipWithoutChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssemblyPartRelationshipDTO submodelResponse = submodelFacade.getSubmodel(catenaXId,
                jobParameterFilter());

        assertThat(submodelResponse.getCatenaXId()).isEqualTo(catenaXId);
        assertThat(submodelResponse.getChildParts()).isEmpty();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipDTOWhenRequestingOnRealClient() {
        final String endpointUrl = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(restTemplate,
                "http://aaswrapper:9191/api/service", jsonUtil);
        SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);

        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship();
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        assemblyPartRelationship.setCatenaXId(catenaXId);
        assemblyPartRelationship.setChildParts(new HashSet<>());

        final String jsonObject = jsonUtil.asString(assemblyPartRelationship);
        final ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonObject, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(URI.class), any());

        final AssemblyPartRelationshipDTO submodel = submodelFacade.getSubmodel(endpointUrl, jobParameter());

        assertThat(submodel.getCatenaXId()).isEqualTo(catenaXId);
        final Set<ChildDataDTO> childParts = submodel.getChildParts();
        assertThat(childParts).isEmpty();
    }

    @Test
    void shouldReturnStringWhenRequestingSubmodelWithoutAspect() {
        final String catenaXId = "urn:uuid:ea724f73-cb93-4b7b-b92f-d97280ff888b";

        final String submodelResponse = submodelFacade.getSubmodelAsString(catenaXId);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AYRE\",\"key\":\"ManufacturerID\"}");
    }
}