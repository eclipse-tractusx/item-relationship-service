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

import java.util.Map;
import java.util.UUID;

import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * EdcAssetService used for creating edc assets for notifications dtr and to delete assets from edc
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EdcAssetService {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_POLICY_ID = "use-eu";
    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_DATA_ADDRESS_PROPERTY_TYPE = "HttpData";
    private static final String ASSETS_PATH = "/management/v2/assets";

    EdcTransformer edcTransformer;

    public String createNotificationAsset(String baseUrl, String assetName, NotificationMethod notificationMethod,
            NotificationType notificationType, RestTemplate restTemplate) {
        JsonObject request = createNotificationAssetRequest(assetName, baseUrl, notificationMethod, notificationType);
        return sendRequest(request, restTemplate);
    }

    public String createDtrAsset(String baseUrl, String assetName, RestTemplate restTemplate) {
        JsonObject request = createDtrAssetRequest(assetName, baseUrl);
        return sendRequest(request, restTemplate);
    }

    private String sendRequest(final JsonObject request, final RestTemplate restTemplate) {
        final ResponseEntity<String> createEdcDataAssetResponse;
        try {
            createEdcDataAssetResponse = restTemplate.postForEntity(ASSETS_PATH, request, String.class);
            HttpStatusCode responseCode = createEdcDataAssetResponse.getStatusCode();

            if (responseCode.value() == HttpStatus.CONFLICT.value()) {
                log.info("{} asset already exists in the EDC", getAssetId(request));
                return getAssetId(request);
            }

            if (responseCode.value() == HttpStatus.OK.value()) {
                return getAssetId(request);
            }
        } catch (RestClientException e) {
            throw new CreateEdcAssetException(e);
        }
        throw new CreateEdcAssetException("Failed to create asset %s".formatted(getAssetId(request)));
    }

    public void deleteAsset(String notificationAssetId, RestTemplate restTemplate) {
        String deleteUri = UriComponentsBuilder.fromPath(ASSETS_PATH)
                                               .pathSegment("{notificationAssetId}")
                                               .buildAndExpand(notificationAssetId)
                                               .toUriString();

        try {
            restTemplate.delete(deleteUri);
        } catch (RestClientException e) {
            log.error("Failed to delete EDC notification asset {}. Reason: ", notificationAssetId, e);
        }
    }

    private JsonObject createNotificationAssetRequest(String assetName, String baseUrl,
            NotificationMethod notificationMethod, NotificationType notificationType) {
        String assetId = UUID.randomUUID().toString();
        Map<String, Object> properties = Map.of("description", assetName, "contenttype", DEFAULT_CONTENT_TYPE,
                "policy-id", DEFAULT_POLICY_ID, "type", notificationType.getValue(), "notificationtype",
                notificationType.getValue(), "notificationmethod", notificationMethod.getValue());

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                     .type(DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                                                             DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                                                             baseUrl)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                                                             DEFAULT_METHOD)
                                                     .build();

        Asset asset = Asset.Builder.newInstance()
                                   .id(assetId)
                                   .contentType("Asset")
                                   .properties(properties)
                                   .dataAddress(dataAddress)
                                   .build();
        return edcTransformer.transformAssetToJson(asset);
    }

    private JsonObject createDtrAssetRequest(String assetName, String baseUrl) {
        String assetId = UUID.randomUUID().toString();
        Map<String, Object> properties = Map.of("description", assetName, "type", "data.core.digitalTwinRegistry");

        DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                     .type("DataAddress")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                                                             DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                                                             baseUrl)
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyPath",
                                                             "true")
                                                     .property(
                                                             "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyQueryParams",
                                                             "true")
                                                     .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                                                             DEFAULT_METHOD)
                                                     .build();

        Asset asset = Asset.Builder.newInstance()
                                   .id(assetId)
                                   .contentType("Asset")
                                   .properties(properties)
                                   .dataAddress(dataAddress)
                                   .build();
        return edcTransformer.transformAssetToJson(asset);
    }

    private static String getAssetId(JsonObject jsonObject) {
        return jsonObject.get("asset").asJsonObject().get("@id").toString();
    }
}
