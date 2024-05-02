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
package org.eclipse.tractusx.irs.edc.client.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.asset.EdcAssetService.DATA_ADDRESS_TYPE_HTTP_DATA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.json.JsonObject;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.DeleteEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.EdcAssetAlreadyExistsException;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcAssetServiceTest {

    @Mock
    EdcConfiguration edcConfiguration;
    @Mock
    EdcConfiguration.ControlplaneConfig controlplaneConfig;
    @Mock
    EdcConfiguration.ControlplaneConfig.EndpointConfig endpointConfig;
    @Mock
    private RestTemplate restTemplate;

    private EdcTransformer edcTransformer;
    private EdcAssetService service;

    @BeforeEach
    void setUp() {
        TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");
        jsonLd.registerNamespace("dct", "https://purl.org/dc/terms/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("dcat", "https://www.w3.org/ns/dcat/");
        jsonLd.registerNamespace("dspace", "https://w3id.org/dspace/v0.8/");

        this.edcTransformer = new EdcTransformer(objectMapper(), jsonLd, new TypeTransformerRegistryImpl());
        this.service = new EdcAssetService(edcTransformer, edcConfiguration, restTemplate);
    }

    @Test
    void testAssetCreateRequestStructure() throws JSONException, JsonProcessingException {

        Map<String, Object> properties = Map.of("https://w3id.org/edc/v0.0.1/ns/description",
                "endpoint to qualityinvestigation receive", "https://w3id.org/edc/v0.0.1/ns/contenttype",
                "application/json", "https://w3id.org/edc/v0.0.1/ns/policy-id", "use-eu",
                "https://w3id.org/edc/v0.0.1/ns/type", "receive", "https://w3id.org/edc/v0.0.1/ns/notificationtype",
                "qualityinvestigation", "https://w3id.org/edc/v0.0.1/ns/notificationmethod", "receive");

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                     .type(DATA_ADDRESS_TYPE_HTTP_DATA)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/type", "HttpData")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/baseUrl",
                                                             "https://traceability.dev.demo.catena-x.net/api/qualitynotifications/receive")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/proxyMethod", "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/proxyBody", "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/method", "POST")
                                                     .build();

        Asset asset = Asset.Builder.newInstance()
                                   .id("Asset1")
                                   .contentType("Asset")
                                   .properties(properties)
                                   .dataAddress(dataAddress)
                                   .build();
        System.out.println(objectMapper().writeValueAsString(asset));
        System.out.println(objectMapper().writeValueAsString(dataAddress));
        System.out.println(objectMapper().writeValueAsString(properties));
        JsonObject jsonObject = edcTransformer.transformAssetToJson(asset);
        System.out.println(objectMapper().writeValueAsString(jsonObject));

        JSONAssert.assertEquals("""
                {
                	"@id": "Asset1",
                	"@type": "edc:Asset",
                	"edc:properties": {
                		"edc:type": "receive",
                		"edc:notificationtype": "qualityinvestigation",
                		"edc:policy-id": "use-eu",
                		"edc:description": "endpoint to qualityinvestigation receive",
                		"edc:id": "Asset1",
                		"edc:contenttype": "application/json",
                		"edc:notificationmethod": "receive"
                	},
                	"edc:dataAddress": {
                		"@type": "edc:DataAddress",
                		"edc:method": "POST",
                		"edc:type": "HttpData",
                		"edc:proxyMethod": "true",
                		"edc:proxyBody": "true",
                		"edc:baseUrl": "https://traceability.dev.demo.catena-x.net/api/qualitynotifications/receive"
                	},
                	"@context": {
                		"dct": "https://purl.org/dc/terms/",
                		"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                		"edc": "https://w3id.org/edc/v0.0.1/ns/",
                		"odrl": "http://www.w3.org/ns/odrl/2/",
                		"dcat": "https://www.w3.org/ns/dcat/",
                		"dspace": "https://w3id.org/dspace/v0.8/"
                	}
                }
                """, jsonObject.toString(), false);
    }

    @Test
    void givenCreateNotificationAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        NotificationMethod notificationMethod = NotificationMethod.RECEIVE;
        NotificationType notificationType = NotificationType.QUALITY_ALERT;
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        String assetId = service.createNotificationAsset(baseUrl, assetName, notificationMethod, notificationType);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenCreateDtrAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        String assetId = service.createDtrAsset(baseUrl, assetName);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenCreateSubmodelAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        String assetId = service.createSubmodelAsset(baseUrl, assetName);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenDeleteAsset_whenOk_ThenReturnCreatedAssetId() throws DeleteEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String assetId = "id";

        // when
        service.deleteAsset(assetId);

        // then
        verify(restTemplate).delete(any(String.class));
    }

    @Test
    void givenCreateDtrAsset_whenBadRequest_ThenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        doThrow(HttpClientErrorException.create("Surprise", HttpStatus.BAD_REQUEST, "", null, null, null)).when(
                restTemplate).postForEntity(any(String.class), any(String.class), any());

        // when/then
        assertThrows(CreateEdcAssetException.class, () -> service.createDtrAsset(baseUrl, assetName));
    }

    @Test
    void givenCreateDtrAsset_whenConflict_ThenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        doThrow(HttpClientErrorException.create("Surprise", HttpStatus.CONFLICT, "", null, null, null)).when(
                restTemplate).postForEntity(any(String.class), any(String.class), any());

        // when/then
        assertThrows(EdcAssetAlreadyExistsException.class, () -> service.createDtrAsset(baseUrl, assetName));
    }

    @Test
    void givenDeleteAsset_whenTemplateException_ThenThrowException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn("/management/v2/assets");
        String assetId = "id";
        doThrow(new RestClientException("Surprise")).when(restTemplate).delete(any(String.class));

        // when/then
        assertThrows(DeleteEdcAssetException.class, () -> service.deleteAsset(assetId));
    }

    ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JSONPModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerSubtypes(AtomicConstraint.class, LiteralExpression.class);
        return objectMapper;
    }

}
