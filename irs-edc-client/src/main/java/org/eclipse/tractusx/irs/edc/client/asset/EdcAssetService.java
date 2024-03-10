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

import static org.eclipse.edc.spi.types.domain.DataAddress.EDC_DATA_ADDRESS_TYPE_PROPERTY;
import static org.eclipse.edc.spi.types.domain.HttpDataAddress.HTTP_DATA;

import java.util.Map;
import java.util.UUID;

import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.DeleteEdcAssetException;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.EdcAssetAlreadyExistsException;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * EdcAssetService used for creating edc assets for notifications dtr and to delete assets from edc
 */

@Slf4j
@RequiredArgsConstructor
public class EdcAssetService {
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_POLICY_ID = "use-eu";
    private static final String DEFAULT_METHOD = "POST";
    private static final String ASSET_CREATION_DATA_ADDRESS_BASE_URL = "https://w3id.org/edc/v0.0.1/ns/baseUrl";
    private static final String ASSET_CREATION_DATA_ADDRESS_PROXY_METHOD = "https://w3id.org/edc/v0.0.1/ns/proxyMethod";
    private static final String ASSET_CREATION_DATA_ADDRESS_PROXY_BODY = "https://w3id.org/edc/v0.0.1/ns/proxyBody";
    private static final String ASSET_CREATION_DATA_ADDRESS_PROXY_PATH = "https://w3id.org/edc/v0.0.1/ns/proxyPath";
    private static final String ASSET_CREATION_DATA_ADDRESS_PROXY_QUERY_PARAMS = "https://w3id.org/edc/v0.0.1/ns/proxyQueryParams";
    private static final String ASSET_CREATION_DATA_ADDRESS_METHOD = "https://w3id.org/edc/v0.0.1/ns/method";
    private static final String ASSET_CREATION_PROPERTY_DESCRIPTION = "https://w3id.org/edc/v0.0.1/ns/description";
    private static final String ASSET_CREATION_PROPERTY_CONTENT_TYPE = "https://w3id.org/edc/v0.0.1/ns/contenttype";
    private static final String ASSET_CREATION_PROPERTY_POLICY_ID = "https://w3id.org/edc/v0.0.1/ns/policy-id";
    private static final String ASSET_CREATION_PROPERTY_TYPE = "https://w3id.org/edc/v0.0.1/ns/type";
    private static final String ASSET_CREATION_PROPERTY_NOTIFICATION_TYPE = "https://w3id.org/edc/v0.0.1/ns/notificationtype";
    private static final String ASSET_CREATION_PROPERTY_NOTIFICATION_METHOD = "https://w3id.org/edc/v0.0.1/ns/notificationmethod";

    private final EdcTransformer edcTransformer;
    private final EdcConfiguration config;
    private final RestTemplate restTemplate;

    public String createNotificationAsset(final String baseUrl, final String assetName,
            final NotificationMethod notificationMethod, final NotificationType notificationType)
            throws CreateEdcAssetException {
        final Asset request = createNotificationAssetRequest(assetName, baseUrl, notificationMethod, notificationType);
        return sendRequest(request);
    }

    public String createDtrAsset(final String baseUrl, final String assetId) throws CreateEdcAssetException {
        final Asset request = createDtrAssetRequest(assetId, baseUrl);
        return sendRequest(request);
    }

    public String createSubmodelAsset(final String baseUrl, final String assetId) throws CreateEdcAssetException {
        final Asset request = createSubmodelAssetRequest(assetId, baseUrl);
        return sendRequest(request);
    }

    private String sendRequest(final Asset request) throws CreateEdcAssetException {
        final JsonObject transformedPayload = edcTransformer.transformAssetToJson(request);
        final ResponseEntity<String> createEdcDataAssetResponse;
        try {
            createEdcDataAssetResponse = restTemplate.postForEntity(config.getControlplane().getEndpoint().getAsset(),
                    transformedPayload.toString(), String.class);
            final HttpStatusCode responseCode = createEdcDataAssetResponse.getStatusCode();

            if (responseCode.value() == HttpStatus.OK.value()) {
                return request.getId();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new EdcAssetAlreadyExistsException("asset already exists in the EDC");
            }
            throw new CreateEdcAssetException(e);
        }
        throw new CreateEdcAssetException("Failed to create asset %s".formatted(request.getId()));
    }

    public void deleteAsset(final String assetId) throws DeleteEdcAssetException {
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

    private Asset createNotificationAssetRequest(final String assetName, final String baseUrl,
            final NotificationMethod notificationMethod, final NotificationType notificationType) {
        final String assetId = UUID.randomUUID().toString();
        final Map<String, Object> properties = Map.of(ASSET_CREATION_PROPERTY_DESCRIPTION, assetName,
                ASSET_CREATION_PROPERTY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE, ASSET_CREATION_PROPERTY_POLICY_ID,
                DEFAULT_POLICY_ID, ASSET_CREATION_PROPERTY_TYPE, notificationType.getValue(),
                ASSET_CREATION_PROPERTY_NOTIFICATION_TYPE, notificationType.getValue(),
                ASSET_CREATION_PROPERTY_NOTIFICATION_METHOD, notificationMethod.getValue());

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type(HTTP_DATA)
                                                           .property(EDC_DATA_ADDRESS_TYPE_PROPERTY, HTTP_DATA)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_BASE_URL, baseUrl)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_METHOD,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_BODY,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_METHOD, DEFAULT_METHOD)
                                                           .build();
        return Asset.Builder.newInstance()
                            .id(assetId)
                            .contentType("Asset")
                            .properties(properties)
                            .dataAddress(dataAddress)
                            .build();
    }

    private Asset createDtrAssetRequest(final String assetId, final String baseUrl) {
        final Map<String, Object> properties = Map.of(ASSET_CREATION_PROPERTY_DESCRIPTION,
                "Digital Twin Registry Asset", ASSET_CREATION_PROPERTY_TYPE, "data.core.digitalTwinRegistry");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type("DataAddress")
                                                           .property(EDC_DATA_ADDRESS_TYPE_PROPERTY, HTTP_DATA)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_BASE_URL, baseUrl)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_METHOD,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_BODY,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_PATH,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_QUERY_PARAMS,
                                                                   Boolean.TRUE.toString())
                                                           .build();

        return Asset.Builder.newInstance()
                            .id(assetId)
                            .contentType("Asset")
                            .properties(properties)
                            .dataAddress(dataAddress)
                            .build();
    }

    private Asset createSubmodelAssetRequest(final String assetId, final String baseUrl) {
        final Map<String, Object> properties = Map.of(ASSET_CREATION_PROPERTY_DESCRIPTION, "Submodel Server Asset");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type("DataAddress")
                                                           .property(EDC_DATA_ADDRESS_TYPE_PROPERTY, HTTP_DATA)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_BASE_URL, baseUrl)
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_METHOD,
                                                                   Boolean.FALSE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_BODY,
                                                                   Boolean.FALSE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_PATH,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_CREATION_DATA_ADDRESS_PROXY_QUERY_PARAMS,
                                                                   Boolean.FALSE.toString())
                                                           .build();

        return Asset.Builder.newInstance()
                            .id(assetId)
                            .contentType("Asset")
                            .properties(properties)
                            .dataAddress(dataAddress)
                            .build();
    }
}
