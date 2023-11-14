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
package org.eclipse.tractusx.irs.edc.client;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Communicates with the EDC DataPlane.
 */
@Slf4j
@Service("irsEdcClientEdcDataPlaneClient")
public class EdcDataPlaneClient {

    private static final Pattern RESPONSE_PATTERN = Pattern.compile("\\{\"data\":\"(?<embeddedData>.*)\"\\}");
    private final RestTemplate edcRestTemplate;

    public EdcDataPlaneClient(@Qualifier("edcClientRestTemplate") final RestTemplate edcRestTemplate) {
        this.edcRestTemplate = edcRestTemplate;
    }

    public String getData(final EndpointDataReference dataReference, final String submodelDataplaneUrl) {

        final String response = edcRestTemplate.exchange(submodelDataplaneUrl, HttpMethod.GET,
                new HttpEntity<>(null, headers(dataReference)), String.class).getBody();

        log.info("Extracting raw embeddedData from EDC data plane response");
        return extractData(response);
    }

    private HttpHeaders headers(final EndpointDataReference dataReference) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final var authKey = dataReference.getAuthKey();
        if (authKey != null) {
            headers.add(authKey, dataReference.getAuthCode());
        }
        return headers;
    }

    private String extractData(final String response) {
        String modifiedResponse = response;
        Matcher dataMatcher = RESPONSE_PATTERN.matcher(modifiedResponse);
        while (dataMatcher.matches()) {
            modifiedResponse = dataMatcher.group("embeddedData");
            modifiedResponse = modifiedResponse.replace("\\\"", "\"").replace("\\\\", "\\");
            dataMatcher = RESPONSE_PATTERN.matcher(response);
        }
        return modifiedResponse;
    }

    public EdcNotificationResponse sendData(final EndpointDataReference dataReference,
            final EdcNotification<NotificationContent> notification) {
        final String url = dataReference.getEndpoint();
        final HttpHeaders headers = headers(dataReference);
        headers.setContentType(MediaType.APPLICATION_JSON);

        final ResponseEntity<String> response = edcRestTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(StringMapper.mapToString(notification), headers), String.class);
        log.info("Call to {} returned with status code {}", url, response.getStatusCode());

        return () -> response.getStatusCode().is2xxSuccessful();
    }
}
