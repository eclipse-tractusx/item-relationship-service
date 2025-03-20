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
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;

import java.util.Map;

import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.model.EdcTechnicalServiceAuthentication;
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
@SuppressWarnings("PMD.TooManyMethods")
public class EdcAssetService {
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_POLICY_ID = "use-eu";
    private static final String DEFAULT_METHOD = "POST";

    private static final String ASSET_DATA_ADDRESS_BASE_URL = NAMESPACE_EDC + "baseUrl";
    private static final String ASSET_DATA_ADDRESS_PROXY_METHOD = NAMESPACE_EDC + "proxyMethod";
    private static final String ASSET_DATA_ADDRESS_PROXY_BODY = NAMESPACE_EDC + "proxyBody";
    private static final String ASSET_DATA_ADDRESS_PROXY_PATH = NAMESPACE_EDC + "proxyPath";
    private static final String ASSET_DATA_ADDRESS_PROXY_QUERY_PARAMS = NAMESPACE_EDC + "proxyQueryParams";
    private static final String ASSET_DATA_ADDRESS_TECHNICAL_SERVICE_API_KEY = "header:x-technical-service-key";
    private static final String ASSET_DATA_ADDRESS_METHOD = NAMESPACE_EDC + "method";
    private static final String ASSET_PROPERTY_DESCRIPTION = NAMESPACE_EDC + "description";
    private static final String ASSET_PROPERTY_CONTENT_TYPE = NAMESPACE_EDC + "contenttype";
    private static final String ASSET_PROPERTY_POLICY_ID = NAMESPACE_EDC + "policy-id";
    private static final String ASSET_PROPERTY_EDC_TYPE = NAMESPACE_EDC + "type";
    private static final String ASSET_PROPERTY_DCAT_TYPE = NAMESPACE_DCT + "type";
    private static final String ASSET_PROPERTY_DATA_CORE_REGISTRY = "data.core.digitalTwinRegistry";
    private static final String ASSET_PROPERTY_DCAT_REGISTRY =
            JsonLdConfiguration.NAMESPACE_CX_TAXONOMY + "DigitalTwinRegistry";
    private static final String ASSET_PROPERTY_REGISTRY_VERSION = "3.0";
    private static final String ASSET_PROPERTY_COMMON_VERSION_KEY =
            JsonLdConfiguration.NAMESPACE_CX_ONTOLOGY + "version";
    private static final String ASSET_PROPERTY_NOTIFICATION_VERSION = "1.2";

    public static final String DATA_ADDRESS_TYPE_HTTP_DATA = "HttpData";

    private final EdcTransformer edcTransformer;
    private final EdcConfiguration config;
    private final RestTemplate restTemplate;

    public String createNotificationAsset(final String baseUrl, final String assetName,
            final NotificationMethod notificationMethod, final NotificationType notificationType, final EdcTechnicalServiceAuthentication edcTechnicalServiceAuthentication)
            throws CreateEdcAssetException {
        final Notification notification = Notification.toNotification(notificationMethod, notificationType);
        final Asset request = createNotificationAssetRequest(assetName, baseUrl, notification,
                edcTechnicalServiceAuthentication);
        return sendRequest(request);
    }

    public String createNotificationAsset(final String baseUrl, final String assetName, final Notification notification)
            throws CreateEdcAssetException {
        final Asset request = createNotificationAssetRequest(assetName, baseUrl, notification, null);
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
                throw new EdcAssetAlreadyExistsException("asset already exists in the EDC", e);
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

    public ResponseEntity<org.eclipse.tractusx.irs.edc.client.asset.model.Asset> getAsset(final String assetId)
            throws GetEdcAssetException {
        final String updateUri = UriComponentsBuilder.fromPath(config.getControlplane().getEndpoint().getAsset())
                                                     .pathSegment("{assetId}")
                                                     .buildAndExpand(assetId)
                                                     .toUriString();
        try {
            return restTemplate.getForEntity(updateUri, org.eclipse.tractusx.irs.edc.client.asset.model.Asset.class);
        } catch (RestClientException e) {
            log.error("Failed to get EDC asset {}. Reason: ", assetId, e);
            throw new GetEdcAssetException(e);
        }
    }

    public void updateAsset(final org.eclipse.tractusx.irs.edc.client.asset.model.Asset assetUpdateRequest) throws UpdateEdcAssetException {
        final String updateUri = UriComponentsBuilder.fromPath(config.getControlplane().getEndpoint().getAsset())
                                                     .toUriString();

        try {
            restTemplate.put(updateUri, assetUpdateRequest);
        } catch (RestClientException e) {
            log.error("Failed to update EDC notification asset's data address {}. Reason: ", assetUpdateRequest.getAssetId(), e);
            throw new UpdateEdcAssetException(e);
        }
    }

    private Asset createNotificationAssetRequest(final String assetName, final String baseUrl,
            final Notification notification, final EdcTechnicalServiceAuthentication edcTechnicalServiceAuthentication) {
        final Map<String, Object> properties = Map.of(ASSET_PROPERTY_DESCRIPTION, assetName,
                ASSET_PROPERTY_CONTENT_TYPE, DEFAULT_CONTENT_TYPE, ASSET_PROPERTY_POLICY_ID, DEFAULT_POLICY_ID,
                ASSET_PROPERTY_COMMON_VERSION_KEY, ASSET_PROPERTY_NOTIFICATION_VERSION, ASSET_PROPERTY_DCAT_TYPE,
                Map.of("@id", JsonLdConfiguration.NAMESPACE_CX_TAXONOMY + notification.getValue()));

        final DataAddress.Builder dataAddressBuilder = DataAddress.Builder.newInstance()
                                                                          .type(DATA_ADDRESS_TYPE_HTTP_DATA) // Address type HTTP
                                                                          .property(EDC_DATA_ADDRESS_TYPE_PROPERTY,
                                                                                  DATA_ADDRESS_TYPE_HTTP_DATA) // Address type property
                                                                          .property(ASSET_DATA_ADDRESS_BASE_URL,
                                                                                  baseUrl) // Base URL
                                                                          .property(ASSET_DATA_ADDRESS_PROXY_METHOD,
                                                                                  Boolean.TRUE.toString()) // Enable proxy method
                                                                          .property(ASSET_DATA_ADDRESS_PROXY_BODY,
                                                                                  Boolean.TRUE.toString()) // Enable proxy body
                                                                          .property(ASSET_DATA_ADDRESS_METHOD,
                                                                                  DEFAULT_METHOD); // Default method (e.g., GET, POST)

        enrichOptionalEdcApiAuthenticationToDataAddress(edcTechnicalServiceAuthentication, dataAddressBuilder);

        final DataAddress dataAddress = dataAddressBuilder.build();

        return Asset.Builder.newInstance()
                            .id(notification.getAssetId())
                            .contentType("Asset")
                            .properties(properties)
                            .dataAddress(dataAddress)
                            .build();
    }

    private Asset createDtrAssetRequest(final String assetId, final String baseUrl) {
        final Map<String, Object> properties = Map.of(ASSET_PROPERTY_DESCRIPTION, "Digital Twin Registry Asset",
                ASSET_PROPERTY_EDC_TYPE, ASSET_PROPERTY_DATA_CORE_REGISTRY, ASSET_PROPERTY_COMMON_VERSION_KEY,
                ASSET_PROPERTY_REGISTRY_VERSION, ASSET_PROPERTY_DCAT_TYPE, Map.of("@id", ASSET_PROPERTY_DCAT_REGISTRY));

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type("DataAddress")
                                                           .property(EDC_DATA_ADDRESS_TYPE_PROPERTY,
                                                                   DATA_ADDRESS_TYPE_HTTP_DATA)
                                                           .property(ASSET_DATA_ADDRESS_BASE_URL, baseUrl)
                                                           .property(ASSET_DATA_ADDRESS_PROXY_METHOD,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_BODY,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_PATH,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_QUERY_PARAMS,
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
        final Map<String, Object> properties = Map.of(ASSET_PROPERTY_DESCRIPTION, "Submodel Server Asset");

        final DataAddress dataAddress = DataAddress.Builder.newInstance()
                                                           .type("DataAddress")
                                                           .property(EDC_DATA_ADDRESS_TYPE_PROPERTY,
                                                                   DATA_ADDRESS_TYPE_HTTP_DATA)
                                                           .property(ASSET_DATA_ADDRESS_BASE_URL, baseUrl)
                                                           .property(ASSET_DATA_ADDRESS_PROXY_METHOD,
                                                                   Boolean.FALSE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_BODY,
                                                                   Boolean.FALSE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_PATH,
                                                                   Boolean.TRUE.toString())
                                                           .property(ASSET_DATA_ADDRESS_PROXY_QUERY_PARAMS,
                                                                   Boolean.FALSE.toString())
                                                           .build();

        return Asset.Builder.newInstance()
                            .id(assetId)
                            .contentType("Asset")
                            .properties(properties)
                            .dataAddress(dataAddress)
                            .build();
    }

    private static void enrichOptionalEdcApiAuthenticationToDataAddress(
            final EdcTechnicalServiceAuthentication edcTechnicalServiceAuthentication, final DataAddress.Builder dataAddressBuilder) {
        if (edcTechnicalServiceAuthentication != null && edcTechnicalServiceAuthentication.getTechnicalServiceApiKey() != null
                && !edcTechnicalServiceAuthentication.getTechnicalServiceApiKey().isEmpty()) {
            dataAddressBuilder.property(ASSET_DATA_ADDRESS_TECHNICAL_SERVICE_API_KEY, edcTechnicalServiceAuthentication.getTechnicalServiceApiKey());
        }
    }
}
