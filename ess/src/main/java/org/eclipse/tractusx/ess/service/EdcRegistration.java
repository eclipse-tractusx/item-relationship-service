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
package org.eclipse.tractusx.ess.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.nimbusds.jose.shaded.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.edc.StringMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Event listener to create required EDC provider registrations on startup.
 */
@Component
@Slf4j
public class EdcRegistration {

    public static final String ASSET_ID_RESPONSE = "ess-response-asset";
    public static final String ASSET_ID_REQUEST = "notify-request-asset";
    private final RestTemplate restTemplate;
    private final String edcProviderUrl;
    private final String essBaseUrl;
    private final String apiKeyHeader;
    private final String apiKeySecret;

    public EdcRegistration(@Qualifier("noErrorRestTemplate") final RestTemplate restTemplate,
            @Value("${ess.localEdcEndpoint}") final String edcProviderUrl,
            @Value("${ess.irs.url}") final String essBaseUrl,
            @Value("${edc.controlplane.api-key.header}") final String apiKeyHeader,
            @Value("${edc.controlplane.api-key.secret}") final String apiKeySecret) {
        this.restTemplate = restTemplate;
        this.edcProviderUrl = edcProviderUrl;
        this.essBaseUrl = essBaseUrl;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeySecret = apiKeySecret;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerEdcAsset() {
        log.info("Starting EDC registration event listener.");

        if (assetIsNotRegisteredYet(ASSET_ID_RESPONSE)) {
            log.info("Notification response receiver asset is not registered yet, starting registration.");
            registerAsset(ASSET_ID_RESPONSE, "ess-supplier-response", "/ess/notifications/receive");
            registerPolicy("1000", ASSET_ID_RESPONSE);
            registerContractDefinition("1000", ASSET_ID_RESPONSE);
            log.info("Registration finished.");
        }

        if (assetIsNotRegisteredYet(ASSET_ID_REQUEST)) {
            log.info("Mock notification request receiver asset is not registered yet, starting registration.");
            registerAsset(ASSET_ID_REQUEST, "ess-supplier-request", "/ess/mock/notification");
            registerPolicy("1001", ASSET_ID_REQUEST);
            registerContractDefinition("1001", ASSET_ID_REQUEST);
            log.info("Registration finished.");
        }

        log.info("Finishing EDC registration event listener.");
    }

    private void registerAsset(final String assetId, final String notificationType, final String path) {
        final var body = """
                {
                  "asset": {
                    "properties": {
                      "asset:prop:id": "%s",
                      "asset:prop:description": "ESS notification endpoint",
                      "asset:prop:contenttype": "application/json",
                      "asset:prop:policy-id": "use-eu",
                      "asset:prop:notificationtype":"%s",
                      "asset:prop:notificationmethod": "receive"
                    }
                  },
                  "dataAddress": {
                    "properties": {
                      "baseUrl": "%s",
                      "type": "HttpData",
                      "proxyBody": true,
                      "proxyMethod": true
                    }
                  }
                }
                """.formatted(assetId, notificationType, essBaseUrl + path);
        final var entity = restTemplate.exchange(edcProviderUrl + "/data/assets", HttpMethod.POST, toEntity(body),
                String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification asset registered successfully.");
        } else {
            log.error("Could not create notification asset, provider returned status code {}",
                    entity.getStatusCodeValue());
        }

    }

    private void registerPolicy(final String policyId, final String assetId) {
        final var body = """
                {
                   "id": %s,
                   "policy": {
                     "prohibitions": [],
                     "obligations": [],
                     "permissions": [
                       {
                         "edctype": "dataspaceconnector:permission",
                         "action": {
                           "type": "USE"
                         },
                         "target": "%s"
                       }
                     ]
                   }
                 }
                """.formatted(policyId, assetId);
        final var entity = restTemplate.exchange(edcProviderUrl + "/data/policydefinitions", HttpMethod.POST,
                toEntity(body), String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification policy registered successfully.");
        } else {
            log.error("Could not create notification policy, provider returned status code {}",
                    entity.getStatusCodeValue());
        }
    }

    private void registerContractDefinition(final String contractId, final String assetId) {
        final var body = """
                {
                    "id": %s,
                    "criteria": [
                      {
                        "operandLeft": "asset:prop:id",
                        "operator": "=",
                        "operandRight": "%s"
                      }
                    ],
                    "accessPolicyId": %s,
                    "contractPolicyId": %s
                  }
                """.formatted(contractId, assetId, contractId, contractId);

        final var entity = restTemplate.exchange(edcProviderUrl + "/data/contractdefinitions", HttpMethod.POST,
                toEntity(body), String.class);
        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification contract definition registered successfully.");
        } else {
            log.error("Could not create notification contract definition, provider returned status code {}",
                    entity.getStatusCodeValue());
        }
    }

    private HttpEntity<String> toEntity(final String body) {
        return new HttpEntity<>(body, headers());
    }

    private HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        if (StringUtils.isNotBlank(apiKeyHeader)) {
            headers.add(apiKeyHeader, apiKeySecret);
        }
        return headers;
    }

    private boolean assetIsNotRegisteredYet(final String assetId) {
        if (restTemplate != null && edcProviderUrl != null) {
            final var url = edcProviderUrl + "/data/assets?filter=asset:prop:id=" + assetId;
            log.info("Requesting asset from EDC provider with url {}", url);
            final var entity = restTemplate.exchange(url, HttpMethod.GET, toEntity(null), String.class);
            if (entity.getStatusCode().is2xxSuccessful()) {
                final JSONArray array = StringMapper.mapFromString(entity.getBody(), JSONArray.class);
                return array.isEmpty();
            }
            log.error("Cannot ask EDC provider for asset registration, please check it! Status code was {}",
                    entity.getStatusCodeValue());
        }
        return false;
    }

}
