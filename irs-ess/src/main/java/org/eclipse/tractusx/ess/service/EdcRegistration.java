/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.ess.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.data.StringMapper;
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
    public static final String ASSET_ID_REQUEST_RECURSIVE = "notify-request-asset-recursive";
    public static final String REGISTRATION_FINISHED = "Registration finished.";
    private final RestTemplate restTemplate;
    private final String edcProviderUrl;
    private final String essBaseUrl;
    private final String apiKeyHeader;
    private final String apiKeySecret;
    private final String managementPath;

    public EdcRegistration(@Qualifier("noErrorRestTemplate") final RestTemplate restTemplate,
            @Value("${ess.localEdcEndpoint}") final String edcProviderUrl,
            @Value("${ess.irs.url}") final String essBaseUrl,
            @Value("${irs-edc-client.controlplane.api-key.header}") final String apiKeyHeader,
            @Value("${irs-edc-client.controlplane.api-key.secret}") final String apiKeySecret,
            @Value("${ess.managementPath}") final String managementPath) {
        this.restTemplate = restTemplate;
        this.edcProviderUrl = edcProviderUrl;
        this.essBaseUrl = essBaseUrl;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeySecret = apiKeySecret;
        this.managementPath = managementPath;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerEdcAsset() {
        log.info("Starting EDC registration event listener.");

        if (assetIsNotRegisteredYet(ASSET_ID_RESPONSE)) {
            log.info("Notification response receiver asset is not registered yet, starting registration.");
            registerAsset(ASSET_ID_RESPONSE, "ess-supplier-response", "/ess/notification/receive");
            registerPolicy("1000");
            registerContractDefinition("1000", ASSET_ID_RESPONSE);
            log.info(REGISTRATION_FINISHED);
        }

        if (assetIsNotRegisteredYet(ASSET_ID_REQUEST)) {
            log.info("Mock notification request receiver asset is not registered yet, starting registration.");
            registerAsset(ASSET_ID_REQUEST, "ess-supplier-request", "/ess/mock/notification/receive");
            registerPolicy("1001");
            registerContractDefinition("1001", ASSET_ID_REQUEST);
            log.info(REGISTRATION_FINISHED);
        }

        if (assetIsNotRegisteredYet(ASSET_ID_REQUEST_RECURSIVE)) {
            log.info("Recursive notification request receiver asset is not registered yet, starting registration.");
            registerAsset(ASSET_ID_REQUEST_RECURSIVE, "ess-supplier-request", "/ess/notification/receive-recursive");
            registerPolicy("1002");
            registerContractDefinition("1002", ASSET_ID_REQUEST_RECURSIVE);
            log.info(REGISTRATION_FINISHED);
        }

        log.info("Finishing EDC registration event listener.");
    }

    private void registerAsset(final String assetId, final String notificationType, final String path) {
        final var body = """
                {
                    "@context": {},
                    "asset": {
                        "@id": "%s",
                        "properties": {
                            "description": "ESS notification endpoint",
                            "contenttype": "application/json",
                            "notificationtype":"%s",
                            "notificationmethod": "receive"
                        }
                    },
                    "dataAddress": {
                        "baseUrl": "%s",
                        "type": "HttpData",
                        "proxyBody": "true",
                        "proxyMethod": "true"
                    }
                }
                """.formatted(assetId, notificationType, essBaseUrl + path);
        final var entity = restTemplate.exchange(edcProviderUrl + managementPath + "/assets", HttpMethod.POST,
                toEntity(body), String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification asset registered successfully.");
        } else {
            log.error("Could not create notification asset, provider returned status code {}",
                    entity.getStatusCode().value());
        }

    }

    private void registerPolicy(final String policyId) {
        final var body = """
                  {
                      "@context": {
                        "odrl": "http://www.w3.org/ns/odrl/2/"
                      },
                      "@id": "%s",
                      "policy": {
                        "odrl:permission": [
                          {
                            "odrl:action": "USE",
                            "odrl:constraint": {
                              "@type": "AtomicConstraint",
                              "odrl:or": [
                                {
                                  "@type": "Constraint",
                                  "odrl:leftOperand": "idsc:PURPOSE",
                                  "odrl:operator": "EQ",
                                  "odrl:rightOperand": "ID 3.0 Trace"
                                }
                              ]
                            }
                          }
                        ]
                      }
                  }
                """.formatted(policyId);
        final var entity = restTemplate.exchange(edcProviderUrl + managementPath + "/policydefinitions",
                HttpMethod.POST, toEntity(body), String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification policy registered successfully.");
        } else {
            log.error("Could not create notification policy, provider returned status code {}",
                    entity.getStatusCode().value());
        }
    }

    private void registerContractDefinition(final String contractId, final String assetId) {
        final var body = """
                {
                    "@context": {},
                    "accessPolicyId": "%s",
                    "contractPolicyId": "%s",
                    "assetsSelector": {
                        "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
                        "operator": "=",
                        "operandRight": "%s"
                    }
                }
                """.formatted(contractId, contractId, assetId);

        final var entity = restTemplate.exchange(edcProviderUrl + managementPath + "/contractdefinitions",
                HttpMethod.POST, toEntity(body), String.class);
        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification contract definition registered successfully.");
        } else {
            log.error("Could not create notification contract definition, provider returned status code {}",
                    entity.getStatusCode().value());
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
        if (restTemplate != null && StringUtils.isNotBlank(edcProviderUrl)) {
            final var url = edcProviderUrl + managementPath + "/assets/request";
            log.info("Requesting asset from EDC provider with url {}", url);
            final String filter = """
                    {
                        "@context": {},
                        "filterExpression": [
                                {
                                    "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
                                    "operandRight": "%s",
                                    "operator": "="
                                }
                            ]
                    }
                    """.formatted(assetId);
            final var entity = restTemplate.exchange(url, HttpMethod.POST, toEntity(filter), String.class);
            if (entity.getStatusCode().is2xxSuccessful()) {
                final List<?> array = StringMapper.mapFromString(entity.getBody(), List.class);
                return array.isEmpty();
            }
            log.error("Cannot ask EDC provider for asset registration, please check it! Status code was {}",
                    entity.getStatusCode().value());
        }
        return false;
    }

}
