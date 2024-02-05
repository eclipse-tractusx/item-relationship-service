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
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.DeleteEdcAssetException;
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

    private final EdcTransformer edcTransformer;
    private final EdcConfiguration config;

    public String createNotificationAsset(final String baseUrl, final String assetName,
            final NotificationMethod notificationMethod, final NotificationType notificationType,
            final RestTemplate restTemplate) throws CreateEdcAssetException {
        final JsonObject request = createNotificationAssetRequest(assetName, baseUrl, notificationMethod,
                notificationType);
        return sendRequest(request, restTemplate);
    }

    public String createDtrAsset(final String baseUrl, final String assetName, final RestTemplate restTemplate)
            throws CreateEdcAssetException {
        final JsonObject request = createDtrAssetRequest(assetName, baseUrl);
        return sendRequest(request, restTemplate);
    }

    private String sendRequest(final JsonObject request, final RestTemplate restTemplate)
            throws CreateEdcAssetException {
        final ResponseEntity<String> createEdcDataAssetResponse;
        try {
            createEdcDataAssetResponse = restTemplate.postForEntity(
                    config.getControlplane().getEndpoint().getAsset(),
                    request,
                    String.class);
            final HttpStatusCode responseCode = createEdcDataAssetResponse.getStatusCode();

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

    public void deleteAsset(final String assetId, final RestTemplate restTemplate) throws DeleteEdcAssetException {
        final String deleteUri = UriComponentsBuilder.fromPath(config.getControlplane().getEndpoint().getAsset())
                                                     .pathSegment("{notificationAssetId}")
                                                     .buildAndExpand(assetId)
                                                     .toUriString();

        try {
            restTemplate.delete(deleteUri);
        } catch (RestClientException e) {
            log.error("Failed to delete EDC notification asset {}. Reason: ", assetId, e);
            throw new DeleteEdcAssetException(e);
        }
    }

    private JsonObject createNotificationAssetRequest(final String assetName, final String baseUrl,
            final NotificationMethod notificationMethod, final NotificationType notificationType) {
        final String assetId = UUID.randomUUID().toString();
        final Map<String, Object> properties = Map.of("description", assetName, "contenttype", DEFAULT_CONTENT_TYPE,
                "policy-id", DEFAULT_POLICY_ID, "type", notificationType.getValue(), "notificationtype",
                notificationType.getValue(), "notificationmethod", notificationMethod.getValue());

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type(DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                           .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                                                                   DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                                                                   baseUrl)
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                                                                   DEFAULT_METHOD)
                                                           .build();

        final Asset asset = Asset.Builder.newInstance()
                                         .id(assetId)
                                         .contentType("Asset")
                                         .properties(properties)
                                         .dataAddress(dataAddress)
                                         .build();
        return edcTransformer.transformAssetRequestToJson(
                AssetRequest.builder().asset(asset).dataAddress(dataAddress).build());
    }

    private JsonObject createDtrAssetRequest(final String assetName, final String baseUrl) {
        final String assetId = UUID.randomUUID().toString();
        final Map<String, Object> properties = Map.of("description", assetName, "type",
                "data.core.digitalTwinRegistry");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type("DataAddress")
                                                           .property("https://w3id.org/edc/v0.0.1/ns/dataAddress/type",
                                                                   DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl",
                                                                   baseUrl)
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyPath",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyQueryParams",
                                                                   Boolean.TRUE.toString())
                                                           .property(
                                                                   "https://w3id.org/edc/v0.0.1/ns/dataAddress/method",
                                                                   DEFAULT_METHOD)
                                                           .build();

        final Asset asset = Asset.Builder.newInstance()
                                         .id(assetId)
                                         .contentType("Asset")
                                         .properties(properties)
                                         .dataAddress(dataAddress)
                                         .build();
        return edcTransformer.transformAssetRequestToJson(
                AssetRequest.builder().asset(asset).dataAddress(dataAddress).build());
    }

    private static String getAssetId(final JsonObject jsonObject) {
        return jsonObject.get("asset").asJsonObject().get("@id").toString();
    }
}
