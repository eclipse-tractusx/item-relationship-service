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
package org.eclipse.tractusx.irs.edc;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.model.TransferProcessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Communicates with the EDC ControlPlane
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EdcControlPlaneClient {

    static final String CONSUMER_CONTROL_PLANE = "https://irs-consumer-controlplane.dev.demo.catena-x.net";

    static final String CONTROL_PLANE_SUFIX = "/api/v1/ids/data";
    private static final String EDC_HEADER = "X-Api-Key";

    @Value("${edc.token:}")
    private static final String EDC_TOKEN = "";
    public static final int MAX_NUMBER_OF_CALLS = 20;
    private static final long SLEEP_TIMEOUT_IN_MILLIS = 2000;

    private final RestTemplate simpleRestTemplate;

    Catalog getCatalog(String providerConnectorUrl) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                + "/data/catalog?providerUrl="
                + providerConnectorUrl
                + CONTROL_PLANE_SUFIX
                + "&limit=1000",
                HttpMethod.GET,
                new HttpEntity<>(null, headers()),
                Catalog.class).getBody();
    }

    NegotiationId startNegotiations(NegotiationRequest request) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                        + "/data/contractnegotiations",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                NegotiationId.class).getBody();
    }

    @SneakyThrows
    NegotiationResponse getNegotiationResult(NegotiationId negotiationId) {

        NegotiationResponse response = null;

        int calls = 0;
        boolean confirmed = false;
        while (calls < MAX_NUMBER_OF_CALLS && !confirmed) {
            calls++;
            log.info("Check negotiations status for: {} time", calls);
            Thread.sleep(SLEEP_TIMEOUT_IN_MILLIS);

            response = simpleRestTemplate.exchange(
                    CONSUMER_CONTROL_PLANE + "/data/contractnegotiations/" + negotiationId.getId(), HttpMethod.GET,
                    new HttpEntity<>(null, headers()), NegotiationResponse.class).getBody();

            log.info("Response status of negotiation: {}", response);

            if (response != null) {
                confirmed = "CONFIRMED".equals(response.getState());
            }

        }
        return response;
    }

    TransferProcessId startTransferProcess(TransferProcessRequest request) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                        + "/data/transferprocess",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                TransferProcessId.class).getBody();
    }

    @SneakyThrows
    TransferProcessResponse getTransferProcess(TransferProcessId transferProcessId) {
        TransferProcessResponse response = null;

        int calls = 0;
        boolean completed = false;
        while (calls < MAX_NUMBER_OF_CALLS && !completed) {
            calls++;
            log.info("Check Transfer Process status for: {} time", calls);
            Thread.sleep(SLEEP_TIMEOUT_IN_MILLIS);

            response = simpleRestTemplate.exchange(
                    CONSUMER_CONTROL_PLANE + "/data/transferprocess/" + transferProcessId.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers()),
                    TransferProcessResponse.class).getBody();

            log.info("Response status of Transfer Process: {}", response);

            if (response != null) {
                completed = "COMPLETED".equals(response.getState());
            }

        }
        return response;
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add(EDC_HEADER, EDC_TOKEN);
        return headers;
    }

}
