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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

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
import org.eclipse.tractusx.irs.edc.client.asset.model.Notification;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.DeleteEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.EdcAssetAlreadyExistsException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.GetEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.UpdateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.model.EdcTechnicalServiceAuthentication;
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

    public static final String MANAGEMENT_ASSETS_PATH = "/management/v2/assets";
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
        final TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");
        jsonLd.registerNamespace("dct", "http://purl.org/dc/terms/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("dcat", "https://www.w3.org/ns/dcat/");
        jsonLd.registerNamespace("dspace", "https://w3id.org/dspace/v0.8/");
        jsonLd.registerNamespace("cx-policy", "https://w3id.org/catenax/policy/");

        this.edcTransformer = new EdcTransformer(objectMapper(), jsonLd, new TypeTransformerRegistryImpl());
        this.service = new EdcAssetService(edcTransformer, edcConfiguration, restTemplate);
    }

    @Test
    void testAssetCreateRequestStructure() throws JSONException {

        final Map<String, Object> properties = Map.of("https://w3id.org/edc/v0.0.1/ns/description",
                "endpoint to qualityinvestigation receive", "https://w3id.org/edc/v0.0.1/ns/contenttype",
                "application/json", "https://w3id.org/edc/v0.0.1/ns/policy-id", "use-eu",
                "https://w3id.org/edc/v0.0.1/ns/type", "receive", "https://w3id.org/edc/v0.0.1/ns/notificationtype",
                "qualityinvestigation", "https://w3id.org/edc/v0.0.1/ns/notificationmethod", "receive");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type(DATA_ADDRESS_TYPE_HTTP_DATA)
                                                           .property("https://w3id.org/edc/v0.0.1/ns/type", "HttpData")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/baseUrl",
                                                             "https://traceability.dev.demo.catena-x.net/api/qualitynotifications/receive")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/proxyMethod", "true")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/proxyBody", "true")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/method", "POST")
                                                           .build();

        final Asset asset = Asset.Builder.newInstance()
                                         .id("Asset1")
                                         .contentType("Asset")
                                         .properties(properties)
                                         .dataAddress(dataAddress)
                                         .build();
        final JsonObject jsonObject = edcTransformer.transformAssetToJson(asset);

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
                		"dct": "http://purl.org/dc/terms/",
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
    void testRegistryAssetCreateRequestStructure() throws JSONException {

        final Map<String, Object> properties = Map.of("http://purl.org/dc/terms/type",
                Map.of("@id", "https://w3id.org/catenax/taxonomy#DigitalTwinRegistry"),
                "https://w3id.org/catenax/ontology/common#version", "3.0", "https://w3id.org/edc/v0.0.1/ns/type",
                "data.core.digitalTwinRegistry");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type(DATA_ADDRESS_TYPE_HTTP_DATA)
                                                           .property("https://w3id.org/edc/v0.0.1/ns/type", "HttpData")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/baseUrl",
                                                             "https://test.dtr/registry")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/proxyMethod", "true")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/proxyBody", "true")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/method", "POST")
                                                           .build();

        final Asset asset = Asset.Builder.newInstance()
                                         .id("Asset1")
                                         .contentType("Asset")
                                         .properties(properties)
                                         .dataAddress(dataAddress)
                                         .build();
        final JsonObject jsonObject = edcTransformer.transformAssetToJson(asset);

        JSONAssert.assertEquals("""
                {
                	"@id": "Asset1",
                	"@type": "edc:Asset",
                	"edc:properties": {
                        "dct:type": {
                            "@id": "https://w3id.org/catenax/taxonomy#DigitalTwinRegistry"
                        },
                        "https://w3id.org/catenax/ontology/common#version": "3.0",
                		"edc:type": "data.core.digitalTwinRegistry",
                        "edc:id": "Asset1",
                        "edc:contenttype": "Asset"
                	},
                	"edc:dataAddress": {
                		"@type": "edc:DataAddress",
                		"edc:method": "POST",
                		"edc:type": "HttpData",
                		"edc:proxyMethod": "true",
                		"edc:proxyBody": "true",
                		"edc:baseUrl": "https://test.dtr/registry"
                	},
                	"@context": {
                		"dct": "http://purl.org/dc/terms/",
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
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
        final NotificationMethod notificationMethod = NotificationMethod.RECEIVE;
        final NotificationType notificationType = NotificationType.QUALITY_ALERT;
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        final String assetId = service.createNotificationAsset(baseUrl, assetName, notificationMethod, notificationType, null);

        // then
        assertThat(assetId).isEqualTo(Notification.RECEIVE_QUALITY_ALERT_NOTIFICATION.getAssetId());
        final String expectedRequestPayload = expectedCreateNotificationAssetPayload(assetId,
                Notification.RECEIVE_QUALITY_ALERT_NOTIFICATION);
        verify(restTemplate).postForEntity(MANAGEMENT_ASSETS_PATH, expectedRequestPayload, String.class);
    }

    @Test
    void givenCreateTaxoNotificationAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
        final Notification updateQualityAlertNotification = Notification.UPDATE_QUALITY_ALERT_NOTIFICATION;
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        final String assetId = service.createNotificationAsset(baseUrl, assetName, updateQualityAlertNotification);

        // then
        assertThat(assetId).isEqualTo(Notification.UPDATE_QUALITY_ALERT_NOTIFICATION.getAssetId());
        final String expectedRequestPayload = expectedCreateNotificationAssetPayload(assetId,
                Notification.UPDATE_QUALITY_ALERT_NOTIFICATION);
        verify(restTemplate).postForEntity(MANAGEMENT_ASSETS_PATH, expectedRequestPayload, String.class);
    }

    private static String expectedCreateNotificationAssetPayload(final String assetId,
            final Notification notification) {
        return """
                {"@id":"%s","@type":"edc:Asset","edc:properties":{"edc:policy-id":"use-eu","dct:type":{"@id":"https://w3id.org/catenax/taxonomy#%s"},"edc:description":"asset1","https://w3id.org/catenax/ontology/common#version":"1.2","edc:id":"%s","edc:contenttype":"application/json"},"edc:dataAddress":{"@type":"edc:DataAddress","edc:method":"POST","edc:type":"HttpData","edc:proxyMethod":"true","edc:proxyBody":"true","edc:baseUrl":"http://test.test"},"@context":{"odrl":"http://www.w3.org/ns/odrl/2/","dct":"http://purl.org/dc/terms/","tx":"https://w3id.org/tractusx/v0.0.1/ns/","edc":"https://w3id.org/edc/v0.0.1/ns/","dcat":"https://www.w3.org/ns/dcat/","dspace":"https://w3id.org/dspace/v0.8/","cx-policy":"https://w3id.org/catenax/policy/"}}""".formatted(
                assetId, notification.getValue(), assetId);
    }

    @Test
    void givenCreateDtrAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        final String assetId = service.createDtrAsset(baseUrl, assetName);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenCreateSubmodelAsset_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        final String assetId = service.createSubmodelAsset(baseUrl, assetName);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenDeleteAsset_whenOk_ThenReturnCreatedAssetId() throws DeleteEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";

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
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
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
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
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
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";
        doThrow(new RestClientException("Surprise")).when(restTemplate).delete(any(String.class));

        // when/then
        assertThrows(DeleteEdcAssetException.class, () -> service.deleteAsset(assetId));
    }

    @Test
    void givenCreateNotificationAssetIncludingAuthentication_whenOk_ThenReturnCreatedAssetId() throws CreateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String baseUrl = "http://test.test";
        final String assetName = "asset1";
        final NotificationMethod notificationMethod = NotificationMethod.RECEIVE;
        final NotificationType notificationType = NotificationType.QUALITY_ALERT;
        when(restTemplate.postForEntity(any(String.class), any(String.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        final EdcTechnicalServiceAuthentication edcTechnicalServiceAuthentication = EdcTechnicalServiceAuthentication.builder()
                                                                                                                           .technicalServiceApiKey("apiKeyValue").build();

        // when
        final String assetId = service.createNotificationAsset(baseUrl, assetName, notificationMethod, notificationType, edcTechnicalServiceAuthentication);

        // then
        assertThat(assetId).isNotBlank();
        final String expectedRequestPayload = expectedCreateNotificationAssetIncludingAuthenticationPayload(assetId,
                Notification.RECEIVE_QUALITY_ALERT_NOTIFICATION);
        verify(restTemplate, times(1)).postForEntity(MANAGEMENT_ASSETS_PATH, expectedRequestPayload, String.class);
    }

    private static String expectedCreateNotificationAssetIncludingAuthenticationPayload(final String assetId,
            final Notification notification) {
        return """
                {"@id":"%s","@type":"edc:Asset","edc:properties":{"edc:policy-id":"use-eu","dct:type":{"@id":"https://w3id.org/catenax/taxonomy#%s"},"edc:description":"asset1","https://w3id.org/catenax/ontology/common#version":"1.2","edc:id":"%s","edc:contenttype":"application/json"},"edc:dataAddress":{"@type":"edc:DataAddress","edc:method":"POST","edc:type":"HttpData","edc:proxyMethod":"true","edc:proxyBody":"true","header:x-technical-service-key":"apiKeyValue","edc:baseUrl":"http://test.test"},"@context":{"odrl":"http://www.w3.org/ns/odrl/2/","dct":"http://purl.org/dc/terms/","tx":"https://w3id.org/tractusx/v0.0.1/ns/","edc":"https://w3id.org/edc/v0.0.1/ns/","dcat":"https://www.w3.org/ns/dcat/","dspace":"https://w3id.org/dspace/v0.8/","cx-policy":"https://w3id.org/catenax/policy/"}}""".formatted(
                assetId, notification.getValue(), assetId);
    }

    @Test
    void shouldUpdateAsset() throws UpdateEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";
        org.eclipse.tractusx.irs.edc.client.asset.model.Asset request = org.eclipse.tractusx.irs.edc.client.asset.model.Asset.builder()
                                                       .dataAddress(Map.of("baseUrl", "https://google.com"))
                                                       .type("HttpData")
                                                       .assetId(assetId)
                                                       .build();

        // when
        service.updateAsset(request);

        // then
        verify(restTemplate).put(eq("/management/v2/assets"), eq(request));
    }

    @Test
    void shouldNotUpdateAsset_restException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";
        org.eclipse.tractusx.irs.edc.client.asset.model.Asset request = org.eclipse.tractusx.irs.edc.client.asset.model.Asset.builder()
                                                                                                                             .dataAddress(Map.of("baseUrl", "https://google.com"))
                                                                                                                             .type("HttpData")
                                                                                                                             .assetId(assetId)
                                                                                                                             .build();
        doThrow(new RestClientException("error")).when(restTemplate).put(anyString(), any(org.eclipse.tractusx.irs.edc.client.asset.model.Asset.class));

        // then
        assertThrows(UpdateEdcAssetException.class, () -> service.updateAsset(request));
    }

    @Test
    void shouldGetAsset() throws GetEdcAssetException {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";
        org.eclipse.tractusx.irs.edc.client.asset.model.Asset response = org.eclipse.tractusx.irs.edc.client.asset.model.Asset.builder()
                                                                                                                             .dataAddress(Map.of("baseUrl", "https://google.com"))
                                                                                                                             .type("HttpData")
                                                                                                                             .assetId(assetId)
                                                                                                                             .build();
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(response));

        // when
        ResponseEntity<org.eclipse.tractusx.irs.edc.client.asset.model.Asset> asset = service.getAsset(assetId);

        // then
        assertEquals(response, asset.getBody());
    }

    @Test
    void shouldNotGetAsset_restException() {
        // given
        when(edcConfiguration.getControlplane()).thenReturn(controlplaneConfig);
        when(controlplaneConfig.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getAsset()).thenReturn(MANAGEMENT_ASSETS_PATH);
        final String assetId = "id";

        when(restTemplate.getForEntity(anyString(), any())).thenThrow(new RestClientException("error"));

        // then
        assertThrows(GetEdcAssetException.class, () -> service.getAsset(assetId));
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
