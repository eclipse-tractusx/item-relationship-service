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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SubmodelClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final static String url = "http://localhost/submodel";
    private final SubmodelClient submodelClient = new SubmodelClientImpl(restTemplate, url);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldThrowExceptionWhenSubmodelNotFound() {
        final String url = "https://irs-aas-proxy.int.demo.catena-x.net";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate(), url);

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelClient.getSubmodel(url, AssemblyPartRelationship.class));
    }

    @Test
    void shouldReturnAspectModelResponseWhen200_OK() throws IOException {
        final File file = new File("src/test/resources/submodelTest.json");
        final String data = objectMapper.readTree(file).get("data").toString();
        final AssemblyPartRelationship assemblyPartRelationship = objectMapper.readValue(data,
                AssemblyPartRelationship.class);
        doReturn(assemblyPartRelationship).when(restTemplate).getForObject(any(), any());

        final AssemblyPartRelationship submodelResponse = submodelClient.getSubmodel(url,
                AssemblyPartRelationship.class);

        assertThat(submodelResponse.getCatenaXId()).isEqualTo("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6");
    }
}
