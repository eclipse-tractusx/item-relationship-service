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

import com.nimbusds.jose.shaded.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.StringMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Event listener to create required EDC provider registrations on startup.
 */
@Component
@Slf4j
public class EdcRegistration {

    private final RestTemplate restTemplate;
    private final String edcProviderUrl;
    private final String essBaseUrl;

    public EdcRegistration(@Qualifier("noErrorRestTemplate") final RestTemplate restTemplate,
            @Value("${ess.localEdcEndpoint}") final String edcProviderUrl,
            @Value("${ess.irs.url}") final String essBaseUrl) {
        this.restTemplate = restTemplate;
        this.edcProviderUrl = edcProviderUrl;
        this.essBaseUrl = essBaseUrl;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerEdcAsset() {
        log.info("Starting EDC registration event listener.");

        if (assetIsNotRegisteredYet()) {
            log.info("Notification asset is not registered yet, starting registration.");
            registerAsset();
            registerPolicy();
            registerContractDefinition();
            log.info("Registration finished.");
        }

        log.info("Finishing EDC registration event listener.");
    }

    private void registerAsset() {
        final var body = """
                {
                  "asset": {
                    "properties": {
                      "asset:prop:id": "ess-response-asset",
                      "asset:prop:description": "ESS notification endpoint",
                      "asset:prop:contenttype": "application/json",
                      "asset:prop:policy-id": "use-eu"
                      "asset:prop:notificationtype":"ess-supplier-response",
                      "asset:prop:notificationmethod": "receive"
                    }
                  },
                  "dataAddress": {
                    "properties": {
                      "baseUrl": "%s/ess/notifications/receive",
                      "type": "HttpData",
                      "proxyBody": true,
                      "proxyMethod": true
                    }
                  }
                }
                """.formatted(essBaseUrl);
        final var entity = restTemplate.postForEntity(edcProviderUrl + "/data/assets", body, String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification asset registered successfully.");
        } else {
            log.error("Could not create notification asset, provider returned status code {}",
                    entity.getStatusCodeValue());
        }

    }

    private void registerPolicy() {
        final var body = """
                {
                   "id": 1000,
                   "policy": {
                     "prohibitions": [],
                     "obligations": [],
                     "permissions": [
                       {
                         "edctype": "dataspaceconnector:permission",
                         "action": {
                           "type": "USE"
                         },
                         "target": "ess-response-asset"
                       }
                     ]
                   }
                 }
                """;
        final var entity = restTemplate.postForEntity(edcProviderUrl + "/data/policydefinitions", body, String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification policy registered successfully.");
        } else {
            log.error("Could not create notification policy, provider returned status code {}",
                    entity.getStatusCodeValue());
        }
    }

    private void registerContractDefinition() {
        final var body = """
                {
                    "id": 1000,
                    "criteria": [
                      {
                        "operandLeft": "asset:prop:id",
                        "operator": "=",
                        "operandRight": "ess-response-asset"
                      }
                    ],
                    "accessPolicyId": 1000,
                    "contractPolicyId": 1000
                  }
                """;
        final var entity = restTemplate.postForEntity(edcProviderUrl + "/data/contractdefinitions", body, String.class);

        if (entity.getStatusCode().is2xxSuccessful()) {
            log.info("Notification contract definition registered successfully.");
        } else {
            log.error("Could not create notification contract definition, provider returned status code {}",
                    entity.getStatusCodeValue());
        }
    }

    private boolean assetIsNotRegisteredYet() {
        if (restTemplate != null && edcProviderUrl != null) {
            final var entity = restTemplate.getForEntity(
                    edcProviderUrl + "/data/assets?filter=asset:prop:id=ess-response-asset", String.class);
            if (entity.getStatusCode().is2xxSuccessful()) {
                final JSONArray array = StringMapper.mapFromString(entity.getBody(), JSONArray.class);
                return array.isEmpty();
            }
        }
        log.error("Cannot ask EDC provider for asset registration, please check it!");
        return false;
    }

}
