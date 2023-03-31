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
package org.eclipse.tractusx.edc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.tractusx.edc.model.NegotiationId;
import org.eclipse.tractusx.edc.model.NegotiationRequest;
import org.eclipse.tractusx.edc.model.NegotiationResponse;
import org.eclipse.tractusx.edc.model.TransferProcessId;
import org.eclipse.tractusx.edc.model.TransferProcessRequest;
import org.eclipse.tractusx.edc.model.TransferProcessResponse;
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

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_ERROR = "ERROR";

    private final RestTemplate edcRestTemplate;
    private final AsyncPollingService pollingService;

    private final EdcConfiguration config;

    /* package */ Catalog getCatalog(final String providerConnectorUrl, final int offset) {
        final var catalogUrl = config.getControlplane().getEndpoint().getData()
                + "/catalog?providerUrl={providerUrl}&limit={limit}&offset={offset}";
        final var providerUrl = providerConnectorUrl + config.getControlplane().getProviderSuffix();
        final var limit = config.getControlplane().getCatalogPageSize();

        return edcRestTemplate.exchange(catalogUrl, HttpMethod.GET, new HttpEntity<>(null, headers()), Catalog.class,
                providerUrl, limit, offset).getBody();
    }

    /* package */ NegotiationId startNegotiations(final NegotiationRequest request) {
        return edcRestTemplate.exchange(config.getControlplane().getEndpoint().getData() + "/contractnegotiations",
                HttpMethod.POST, new HttpEntity<>(request, headers()), NegotiationId.class).getBody();
    }

    /* package */ CompletableFuture<NegotiationResponse> getNegotiationResult(final NegotiationId negotiationId) {
        final HttpEntity<Object> objectHttpEntity = new HttpEntity<>(null, headers());

        return pollingService.<NegotiationResponse>createJob()
                             .action(() -> {
                                 log.info("Check negotiations status");

                                 final NegotiationResponse response = edcRestTemplate.exchange(
                                                                                             config.getControlplane().getEndpoint().getData() + "/contractnegotiations/"
                                                                                                     + negotiationId.getValue(), HttpMethod.GET, objectHttpEntity,
                                                                                             NegotiationResponse.class)
                                                                                     .getBody();

                                 log.info("Response status of negotiation: {}", response);

                                 if (response != null) {
                                     return switch (response.getState()) {
                                         case STATUS_CONFIRMED -> Optional.of(response);
                                         case STATUS_ERROR -> throw new IllegalStateException(
                                                 "NegotiationResponse with id " + response.getResponseId()
                                                         + " is in state ERROR");
                                         default -> Optional.empty();
                                     };
                                 }
                                 return Optional.empty();
                             })
                             .description("wait for negotiation confirmation")
                             .timeToLive(config.getControlplane().getRequestTtl())
                             .build()
                             .schedule();

    }

    /* package */ TransferProcessId startTransferProcess(final TransferProcessRequest request) {
        return edcRestTemplate.exchange(config.getControlplane().getEndpoint().getData() + "/transferprocess",
                HttpMethod.POST, new HttpEntity<>(request, headers()), TransferProcessId.class).getBody();
    }

    /* package */ CompletableFuture<TransferProcessResponse> getTransferProcess(
            final TransferProcessId transferProcessId) {

        final HttpEntity<Object> objectHttpEntity = new HttpEntity<>(null, headers());

        return pollingService.<TransferProcessResponse>createJob()
                             .action(() -> {
                                 log.info("Check Transfer Process status");

                                 final TransferProcessResponse response = edcRestTemplate.exchange(
                                         config.getControlplane().getEndpoint().getData() + "/transferprocess/"
                                                 + transferProcessId.getValue(), HttpMethod.GET, objectHttpEntity,
                                         TransferProcessResponse.class).getBody();

                                 log.info("Response status of Transfer Process: {}", response);

                                 if (response != null) {
                                     return switch (response.getState()) {
                                         case STATUS_COMPLETED -> Optional.of(response);
                                         case STATUS_ERROR -> throw new IllegalStateException(
                                                 "TransferProcessResponse with id " + response.getResponseId()
                                                         + " is in state ERROR");
                                         default -> Optional.empty();
                                     };
                                 }
                                 return Optional.empty();

                             })
                             .description("wait for transfer process completion")
                             .timeToLive(config.getControlplane().getRequestTtl())
                             .build()
                             .schedule();

    }

    private HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final String apiKeyHeader = config.getControlplane().getApiKey().getHeader();
        if (apiKeyHeader != null) {
            headers.add(apiKeyHeader, config.getControlplane().getApiKey().getSecret());
        }
        return headers;
    }

}
