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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.DeleteEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EdcAssetServiceTest {

    @Mock
    private RestTemplate restTemplate;
    private org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private EdcTransformer edcTransformer;
    private EdcAssetService service;

    @BeforeEach
    void setUp() {
        this.objectMapper = new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper();
        TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");
        jsonLd.registerNamespace("dct", "https://purl.org/dc/terms/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("dcat", "https://www.w3.org/ns/dcat/");
        jsonLd.registerNamespace("dspace", "https://w3id.org/dspace/v0.8/");

        this.edcTransformer = new EdcTransformer(objectMapper(), jsonLd, new TypeTransformerRegistryImpl());
        this.service = new EdcAssetService(edcTransformer);
    }

    @Test
    void testAssetCreateRequestStructure() throws JSONException {

        Map<String, Object> properties = Map.of("description", "endpoint to qualityinvestigation receive",
                "contenttype", "application/json", "policy-id", "use-eu", "type", "receive", "notificationtype",
                "qualityinvestigation", "notificationmethod", "receive");

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                     .type("DEFAULT_DATA_ADDRESS_PROPERTY_TYPE")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                                                             "HttpData")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                                                             "https://traceability.dev.demo.catena-x.net/api/qualitynotifications/receive")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                                                             "POST")
                                                     .build();

        Asset asset = Asset.Builder.newInstance().id("Asset1").contentType("Asset").properties(properties).build();

        JsonObject jsonObject = edcTransformer.transformAssetRequestToJson(
                AssetRequest.builder().asset(asset).dataAddress(dataAddress).build());

        JSONAssert.assertEquals(jsonObject.toString(), """
                {
                	"asset": {
                		"@id": "Asset1",
                		"@type": "edc:Asset",
                		"edc:properties": {
                			"edc:id": "Asset1",
                			"edc:contenttype": "Asset"
                		}
                	},
                	"dataAddress": {
                		"@type": "edc:DataAddress",
                		"type": "HttpData",
                		"proxyBody": "true",
                		"edc:type": "DEFAULT_DATA_ADDRESS_PROPERTY_TYPE",
                		"baseUrl": "https://traceability.dev.demo.catena-x.net/api/qualitynotifications/receive",
                		"method": "POST",
                		"proxyMethod": "true"
                	},
                	"@context": {
                		"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                		"method": "https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                		"dataAddress": "https://w3id.org/edc/v0.0.1/ns/dataAddress",
                		"edc": "https://w3id.org/edc/v0.0.1/ns/",
                		"proxyQueryParams": "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyQueryParams",
                		"proxyBody": "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                		"type": "https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                		"proxyPath": "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyPath",
                		"dspace": "https://w3id.org/dspace/v0.8/",
                		"baseUrl": "https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                		"dct": "https://purl.org/dc/terms/",
                		"proxyMethod": "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                		"odrl": "http://www.w3.org/ns/odrl/2/",
                		"dcat": "https://www.w3.org/ns/dcat/",
                		"asset": "https://w3id.org/edc/v0.0.1/ns/asset"
                	}
                }
                """, false);
    }

    @Test
    void givenCreateNotificationAsset_whenOk_ThenReturnCreatedAssetId() {
        // given
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        NotificationMethod notificationMethod = NotificationMethod.RECEIVE;
        NotificationType notificationType = NotificationType.QUALITY_ALERT;
        when(restTemplate.postForEntity(any(String.class), any(JsonObject.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        String assetId = service.createNotificationAsset(baseUrl, assetName, notificationMethod, notificationType,
                restTemplate);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenCreateDtrAsset_whenOk_ThenReturnCreatedAssetId() {
        // given
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        when(restTemplate.postForEntity(any(String.class), any(JsonObject.class), any())).thenReturn(
                ResponseEntity.ok("test"));

        // when
        String assetId = service.createDtrAsset(baseUrl, assetName, restTemplate);

        // then
        assertThat(assetId).isNotBlank();
    }

    @Test
    void givenDeleteAsset_whenOk_ThenReturnCreatedAssetId() {
        // given
        String assetId = "id";

        // when
        service.deleteAsset(assetId, restTemplate);

        // then
        verify(restTemplate).delete(any(String.class));
    }

    @Test
    void givenCreateDtrAsset_whenOK_ThenThrowException() {
        // given
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        doThrow(new RestClientException("Surprise")).when(restTemplate)
                                                    .postForEntity(any(String.class), any(JsonObject.class), any());

        // when/then
        assertThrows(CreateEdcAssetException.class, () -> service.createDtrAsset(baseUrl, assetName, restTemplate));
    }

    @Test
    void givenCreateDtrAsset_whenTemplateException_ThenThrowException() {
        // given
        String baseUrl = "http://test.test";
        String assetName = "asset1";
        doThrow(new RestClientException("Surprise")).when(restTemplate)
                                                    .postForEntity(any(String.class), any(JsonObject.class), any());

        // when/then
        assertThrows(CreateEdcAssetException.class, () -> service.createDtrAsset(baseUrl, assetName, restTemplate));
    }

    @Test
    void givenDeleteAsset_whenTemplateException_ThenThrowException() {
        // given
        String assetId = "id";
        doThrow(new RestClientException("Surprise")).when(restTemplate).delete(any(String.class));

        // when/then
        assertThrows(DeleteEdcAssetException.class, () -> service.deleteAsset(assetId, restTemplate));
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
