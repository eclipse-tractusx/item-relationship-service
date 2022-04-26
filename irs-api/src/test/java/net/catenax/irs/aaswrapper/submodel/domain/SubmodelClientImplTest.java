//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SubmodelClientImplTest {

    @Mock
    RestTemplate restTemplate;

    private SubmodelClient submodelClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        submodelClient = new SubmodelClientImpl(restTemplate);
    }

    @Test
    void shouldReturnTombStoneResponseWhen404_NotFound() {
        final String url = "http://localhost/test";
        final String catenaXId = "testCatenaXId";
        final ResponseEntity<String> notFoundResponse = new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        doReturn(notFoundResponse).when(restTemplate).getForEntity(url, AssemblyPartRelationship.class);

        assertThatExceptionOfType(SubmodelClientException.class).isThrownBy(
                                                                        () -> submodelClient.getSubmodel(url, AssemblyPartRelationship.class))
                                                                .withMessage("404 NOT_FOUND");

    }

//    @Test
//    void shouldReturnAspectModelResponseWhen200_OK() throws IOException, SubmodelClientException {
//        final String url = "http://localhost/submodel";
//        final String catenaXId = "testCatenaXId";
//        final File file = new File("src/test/resources/submodelTest.json");
//        final String data = objectMapper.readTree(file).get("data").toString();
//        final AssemblyPartRelationship assemblyPartRelationship = objectMapper.readValue(data,
//                AssemblyPartRelationship.class);
//        final ResponseEntity<AssemblyPartRelationship> okResponse = new ResponseEntity<>(assemblyPartRelationship,
//                HttpStatus.OK);
//        doReturn(okResponse).when(restTemplate).getForEntity(url, AssemblyPartRelationship.class);
//
//        final Aspect submodelResponse = submodelClient.getSubmodel(url, AssemblyPartRelationship.class);
//
//        assertThat(submodelResponse).isInstanceOf(AssemblyPartRelationship.class);
//
//        final AssemblyPartRelationship submodel = (AssemblyPartRelationship) submodelResponse;
//
//        assertThat(submodel.getCatenaXId()).isEqualTo("8a61c8db-561e-4db0-84ec-a693fc5ffdf6");
//    }
//
//    @Test
//    void shouldReturnTombstoneWhenRequestingOnFacade() {
//        final String url = "http://localhost/test";
//        final String catenaXId = "testCatenaXId";
//        final ResponseEntity<String> notFoundResponse = new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
//        doReturn(notFoundResponse).when(restTemplate).getForEntity(url, AssemblyPartRelationship.class);
//        final SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);
//
//        final AssemblyPartRelationshipDTO submodelResponse = submodelFacade.getSubmodel(url);
//        assertThat(submodelResponse).isInstanceOf(ItemRelationshipAspectTombstone.class);
//        final ItemRelationshipAspectTombstone responseTombStone = (ItemRelationshipAspectTombstone) submodelResponse;
//
//        assertThat(responseTombStone).isNotNull();
//        assertThat(responseTombStone.getProcessingError().getErrorDetail()).isEqualTo("404 NOT_FOUND");
//        assertThat(responseTombStone.getEndpointURL()).isEqualTo(url);
//        assertThat(responseTombStone.getCatenaXId()).isEqualTo(catenaXId);
//    }
}