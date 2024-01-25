/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcAsset;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcAssetProperties;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcContext;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcCreateDataAssetRequest;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcDataAddress;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationMethod;
import org.eclipse.tractusx.irs.edc.client.asset.model.NotificationType;
import org.eclipse.tractusx.irs.edc.client.asset.model.exception.CreateEdcAssetException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class EdcAssetService {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_POLICY_ID = "use-eu";
    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_DATA_ADDRESS_PROPERTY_TYPE = "HttpData";
    // TODO: move to config ?
    private static final String ASSETS_PATH = "/management/v2/assets";

    public String createNotificationAsset(String baseUrl, String assetName, NotificationMethod notificationMethod,
            NotificationType notificationType, RestTemplate restTemplate) {
        EdcCreateDataAssetRequest request = createNotificationAssetRequest(assetName, baseUrl, notificationMethod,
                notificationType);
        return sendRequest(request, restTemplate);
    }

    private static String sendRequest( final EdcCreateDataAssetRequest request, final RestTemplate restTemplate) {
        final String assetId = request.getAsset().getAssetId();
        final String assetName = request.getAsset().getEdcAssetProperties().getAssetName();
        final ResponseEntity<String> createEdcDataAssetResponse;
        try {
            createEdcDataAssetResponse = restTemplate.postForEntity(ASSETS_PATH, request, String.class);
            HttpStatusCode responseCode = createEdcDataAssetResponse.getStatusCode();

            if (responseCode.value() == 409) {
                log.info("{} asset already exists in the EDC", assetId);
                return assetId;
            }

            if (responseCode.value() == 200) {
                return assetId;
            }
        } catch (RestClientException e) {
            throw new CreateEdcAssetException(e);
        }
        throw new CreateEdcAssetException("Failed to create asset %s".formatted(assetName));
    }

    public String createDtrAsset() {
        return "";
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

    private EdcCreateDataAssetRequest createNotificationAssetRequest(String assetName, String baseUrl,
            NotificationMethod notificationMethod, NotificationType notificationType) {

        String assetId = UUID.randomUUID().toString();
        EdcDataAddress dataAddress = EdcDataAddress.builder()
                                                   .type(DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                   .baseUrl(baseUrl)
                                                   .method(DEFAULT_METHOD)
                                                   .proxyBody("true")
                                                   .proxyMethod("true")
                                                   .build();

        EdcAssetProperties assetProperties = EdcAssetProperties.builder()
                                                               .assetName(assetName)
                                                               .policyId(DEFAULT_POLICY_ID)
                                                               .contentType(DEFAULT_CONTENT_TYPE)
                                                               .type(notificationType.getValue())
                                                               .notificationType(notificationType.getValue())
                                                               .notificationMethod(notificationMethod.getValue())
                                                               .build();

        EdcAsset asset = EdcAsset.builder().assetId(assetId).type("Asset").edcAssetProperties(assetProperties).build();

        EdcContext edcContext = EdcContext.builder().edc(NAMESPACE_EDC).build();

        return EdcCreateDataAssetRequest.builder().asset(asset).dataAddress(dataAddress).context(edcContext).build();
    }

    // TODO check edc edc for corresponding asset class 
    private EdcCreateDataAssetRequest createDtrAssetRequest(String assetName, String baseUrl,
            NotificationMethod notificationMethod, NotificationType notificationType) {
        String assetId = UUID.randomUUID().toString();
        EdcDataAddress dataAddress = EdcDataAddress.builder()
                                                   .type(DEFAULT_DATA_ADDRESS_PROPERTY_TYPE)
                                                   .baseUrl(baseUrl)
                                                   .method(DEFAULT_METHOD)
                                                   .proxyBody("true")
                                                   .proxyMethod("true")
                                                   .build();

        EdcAssetProperties assetProperties = EdcAssetProperties.builder()
                                                               .assetName(assetName)
                                                               .policyId(DEFAULT_POLICY_ID)
                                                               .contentType(DEFAULT_CONTENT_TYPE)
                                                               .type(notificationType.getValue())
                                                               .notificationType(notificationType.getValue())
                                                               .notificationMethod(notificationMethod.getValue())
                                                               .build();

        EdcAsset asset = EdcAsset.builder().assetId(assetId).type("Asset").edcAssetProperties(assetProperties).build();

        EdcContext edcContext = EdcContext.builder().edc(NAMESPACE_EDC).build();

        return EdcCreateDataAssetRequest.builder().asset(asset).dataAddress(dataAddress).context(edcContext).build();
    }
}
