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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.RetryRegistry;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SubmodelClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final OutboundMeterRegistryService meterRegistry = mock(OutboundMeterRegistryService.class);
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    private final static String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
    private final JsonUtil jsonUtil = new JsonUtil();
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
    private final SubmodelClient submodelClient = new SubmodelClientImpl(restTemplate,
            "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry, urlValidator);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldThrowExceptionWhenSubmodelNotFound() {
        final String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
        final SubmodelClientImpl submodelClient = new SubmodelClientImpl(new RestTemplate(),
                "http://aaswrapper:9191/api/service", jsonUtil, meterRegistry, retryRegistry, urlValidator);

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> submodelClient.getSubmodel(url, AssemblyPartRelationship.class));
    }

    @Test
    void shouldReturnAspectModelResponseWhen200_OK() throws IOException {
        final File file = new File("src/test/resources/submodelTest.json");
        final String data = objectMapper.readTree(file).get("data").toString();
        final AssemblyPartRelationship assemblyPartRelationship = objectMapper.readValue(data,
                AssemblyPartRelationship.class);

        final String jsonObject = jsonUtil.asString(assemblyPartRelationship);
        final ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonObject, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(), any());

        final AssemblyPartRelationship submodelResponse = submodelClient.getSubmodel(url,
                AssemblyPartRelationship.class);

        assertThat(submodelResponse.getCatenaXId()).isEqualTo("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6");
    }

    @Test
    void shouldReturnStringAspectModelResponseWhenRequestingForSerialPartTypization() throws IOException {
        final File file = new File("src/test/resources/__files/materialForRecycling.json");
        final String data = objectMapper.readTree(file).toString();

        final ResponseEntity<String> responseEntity = new ResponseEntity<>(data, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(), any());

        final Object submodelResponse = submodelClient.getSubmodel(url, Object.class);

        assertThat(submodelResponse).isNotNull();
        assertThat(objectMapper.writeValueAsString(submodelResponse)).isEqualTo(data);
    }

    @Test
    void shouldCallExternalServiceOnceAndRunIntoTimeout() {
        final SocketTimeoutException timeoutException = new SocketTimeoutException("UnitTestTimeout");
        Throwable ex = new ResourceAccessException("UnitTest", timeoutException);
        doThrow(ex).when(restTemplate).getForEntity(any(), any());

        assertThrows(ResourceAccessException.class, () -> submodelClient.getSubmodel(url, Object.class));

        verify(this.restTemplate, times(3)).getForEntity(any(), any());
        verify(meterRegistry, times(3)).incrementSubmodelTimeoutCounter(any());
    }

    @Test
    void shouldThrowExceptionWhenEndpointAddressIsMalformed() {
        final String malformedAddress = "null/BPNL00000003AYRE/urn%3Auuid%3A0834c0ea-435e-4085-8a9b-ac46af75deae-urn%3Auuid%3A6972cc4c-e31e-438c-8b23-4f32331a73ba/submodel?content=value&extent=WithBLOBValue";
        assertThrows(IllegalArgumentException.class, () -> submodelClient.getSubmodel(malformedAddress));
    }

    @Test
    void shouldReturnSubmodelWhenCallingLocalService() {
        final String localAddress = "http://irs-aaswrapper:9191/BPNL00000003AYRE/urn%3Auuid%3A0834c0ea-435e-4085-8a9b-ac46af75deae-urn%3Auuid%3A6972cc4c-e31e-438c-8b23-4f32331a73ba/submodel?content=value&extent=WithBLOBValue";
        final String data = "testdata";

        final ResponseEntity<String> responseEntity = new ResponseEntity<>(data, HttpStatus.OK);
        doReturn(responseEntity).when(restTemplate).getForEntity(any(), any());

        final String submodelResponse = submodelClient.getSubmodel(localAddress);

        assertThat(submodelResponse).isNotNull();
        assertThat(submodelResponse).isEqualTo(data);
    }
}
