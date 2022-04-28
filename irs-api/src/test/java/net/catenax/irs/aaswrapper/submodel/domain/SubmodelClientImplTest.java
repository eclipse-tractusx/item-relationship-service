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
    void shouldThrowExceptionWhenSubmodelNotFound() {
        final String url = "http://localhost/notAvailableUrl/testCatenaXId";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate());

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelClient.getSubmodel(url, AssemblyPartRelationship.class));

    }

    @Test
    void shouldReturnAspectModelResponseWhen200_OK() throws IOException {
        final String url = "http://localhost/submodel";
        final File file = new File("src/test/resources/submodelTest.json");
        final String data = objectMapper.readTree(file).get("data").toString();
        final AssemblyPartRelationship assemblyPartRelationship = objectMapper.readValue(data,
                AssemblyPartRelationship.class);
        final ResponseEntity<AssemblyPartRelationship> okResponse = new ResponseEntity<>(assemblyPartRelationship,
                HttpStatus.OK);
        doReturn(okResponse).when(restTemplate).getForEntity(url, AssemblyPartRelationship.class);

        final AssemblyPartRelationship submodelResponse = submodelClient.getSubmodel(url,
                AssemblyPartRelationship.class);

        assertThat(submodelResponse.getCatenaXId()).isEqualTo("8a61c8db-561e-4db0-84ec-a693fc5ffdf6");
    }
}
