/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.edc.client;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationState;
import org.eclipse.tractusx.irs.edc.client.model.Response;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Communicates with the EDC ControlPlane
 */
@Slf4j
@Service("irsEdcClientEdcControlPlaneClient")
@SuppressWarnings({ "PMD.TooManyMethods" })
public class EdcControlPlaneClient {

    public static final String STATUS_FINALIZED = "FINALIZED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_STARTED = "STARTED";
    public static final String STATUS_ERROR = "ERROR";
    public static final String DATASPACE_PROTOCOL_HTTP = "dataspace-protocol-http";
    public static final String STATUS_TERMINATED = "TERMINATED";

    private final RestTemplate edcRestTemplate;
    private final AsyncPollingService pollingService;
    private final EdcConfiguration config;
    private final EdcTransformer edcTransformer;

    public EdcControlPlaneClient(@Qualifier("edcClientRestTemplate") final RestTemplate edcRestTemplate,
            final AsyncPollingService pollingService, final EdcConfiguration config,
            final EdcTransformer edcTransformer) {
        this.edcRestTemplate = edcRestTemplate;
        this.pollingService = pollingService;
        this.config = config;
        this.edcTransformer = edcTransformer;
    }

    private static String getResponseBody(final ResponseEntity<String> response) {
        String responseBody = "";
        if (response.hasBody() && response.getBody() != null) {
            responseBody = response.getBody();
        }
        return responseBody;
    }

    /* package */ Catalog getCatalog(final String providerConnectorUrl, final int offset, final String bpn) {
        final var limit = config.getControlplane().getCatalogPageSize();

        final CatalogRequest request = buildCatalogRequest(offset, providerConnectorUrl, limit, bpn);
        return getCatalog(request);
    }

    /* package */ Catalog getCatalog(final CatalogRequest requestBody) {
        final var endpoint = config.getControlplane().getEndpoint();
        final var url = endpoint.getData() + endpoint.getCatalog();

        final String requestJson = edcTransformer.transformCatalogRequestToJson(requestBody).toString();
        log.info("Requesting catalog with payload: {}", requestJson);
        final ResponseEntity<String> response = edcRestTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(requestJson, headers()), String.class);
        final String catalog = getResponseBody(response);
        return edcTransformer.transformCatalog(catalog, StandardCharsets.UTF_8);
    }

    private CatalogRequest buildCatalogRequest(final int offset, final String providerUrl, final int limit,
            final String bpn) {
        final QuerySpec.Builder querySpec = QuerySpec.Builder.newInstance().offset(offset);
        if (config.getControlplane().getCatalogPageSize() > 0) {
            querySpec.limit(limit);
        }
        return CatalogRequest.Builder.newInstance()
                                     .counterPartyAddress(providerUrl)
                                     .counterPartyId(bpn)
                                     .protocol(DATASPACE_PROTOCOL_HTTP)
                                     .querySpec(querySpec.build())
                                     .build();
    }

    /* package */ Catalog getCatalogWithFilter(final String providerConnectorUrl, final String key, final String value,
            final String bpn) {
        final QuerySpec querySpec = QuerySpec.Builder.newInstance().filter(new Criterion(key, "=", value)).build();
        final var catalogRequest = CatalogRequest.Builder.newInstance()
                                                         .counterPartyAddress(providerConnectorUrl)
                                                         .counterPartyId(bpn)
                                                         .protocol(DATASPACE_PROTOCOL_HTTP)
                                                         .querySpec(querySpec)
                                                         .build();
        return getCatalog(catalogRequest);
    }

    /* package */ Response startNegotiations(final NegotiationRequest request) {
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getContractNegotiation();

        final String jsonObject = edcTransformer.transformNegotiationRequestToJson(request).toString();

        return edcRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonObject, headers()), Response.class)
                              .getBody();
    }

    /* package */ CompletableFuture<NegotiationResponse> getNegotiationResult(final Response negotiationId) {
        final HttpEntity<Object> objectHttpEntity = new HttpEntity<>(null, headers());

        return pollingService.<NegotiationResponse>createJob()
                             .action(() -> {
                                 log.info("Check negotiations status");
                                 final NegotiationState negotiationState = getContractNegotiationState(negotiationId,
                                         objectHttpEntity);
                                 log.info("Response status of negotiation: {}", negotiationState);

                                 if (negotiationState != null) {
                                     return switch (negotiationState.getState()) {
                                         case STATUS_FINALIZED -> Optional.of(
                                                 getContractNegotiationResponse(negotiationId, objectHttpEntity));
                                         case STATUS_ERROR -> throw new IllegalStateException(
                                                 "NegotiationResponse with id " + getContractNegotiationResponse(
                                                         negotiationId, objectHttpEntity).getResponseId()
                                                         + " is in state ERROR");
                                         case STATUS_TERMINATED -> throw new IllegalStateException(
                                                 "NegotiationResponse with id " + getContractNegotiationResponse(
                                                         negotiationId, objectHttpEntity).getResponseId()
                                                         + " is in state TERMINATED");
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

    private NegotiationState getContractNegotiationState(final Response negotiationId,
            final HttpEntity<Object> objectHttpEntity) {
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getContractNegotiation() + "/" + negotiationId.getResponseId()
                + endpoint.getStateSuffix();

        return edcRestTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, NegotiationState.class).getBody();
    }

    private NegotiationResponse getContractNegotiationResponse(final Response negotiationId,
            final HttpEntity<Object> objectHttpEntity) {
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getContractNegotiation() + "/" + negotiationId.getResponseId();

        return edcRestTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, NegotiationResponse.class).getBody();
    }

    /* package */ Response startTransferProcess(final TransferProcessRequest request) {
        final String jsonObject = edcTransformer.transformTransferProcessRequestToJson(request).toString();
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getTransferProcess();
        return edcRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonObject, headers()), Response.class)
                              .getBody();
    }

    /* package */ CompletableFuture<TransferProcessResponse> getTransferProcess(final Response transferProcessId) {

        final HttpEntity<Object> objectHttpEntity = new HttpEntity<>(null, headers());

        return pollingService.<TransferProcessResponse>createJob()
                             .action(() -> {
                                 log.info("Check Transfer Process status");
                                 final NegotiationState transferProcessState = getTransferProcessState(
                                         transferProcessId, objectHttpEntity);
                                 log.info("Response status of Transfer Process: {}", transferProcessState);

                                 if (transferProcessState != null) {
                                     return switch (transferProcessState.getState()) {
                                         case STATUS_COMPLETED, STATUS_STARTED -> Optional.of(
                                                 getTransferProcessResponse(transferProcessId, objectHttpEntity));
                                         case STATUS_ERROR -> throw new IllegalStateException(
                                                 "TransferProcessResponse with id " + getTransferProcessResponse(
                                                         transferProcessId, objectHttpEntity).getResponseId()
                                                         + " is in state ERROR");
                                         case STATUS_TERMINATED -> throw new IllegalStateException(
                                                 "TransferProcessResponse with id " + getTransferProcessResponse(
                                                         transferProcessId, objectHttpEntity).getResponseId()
                                                         + " is in state TERMINATED");
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

    private NegotiationState getTransferProcessState(final Response transferProcessId,
            final HttpEntity<Object> objectHttpEntity) {
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getTransferProcess() + "/" + transferProcessId.getResponseId()
                + endpoint.getStateSuffix();

        return edcRestTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, NegotiationState.class).getBody();
    }

    private TransferProcessResponse getTransferProcessResponse(final Response transferProcessId,
            final HttpEntity<Object> objectHttpEntity) {
        final var endpoint = config.getControlplane().getEndpoint();
        final String url = endpoint.getData() + endpoint.getTransferProcess() + "/" + transferProcessId.getResponseId();
        return edcRestTemplate.exchange(url, HttpMethod.GET, objectHttpEntity, TransferProcessResponse.class).getBody();
    }

    @SuppressWarnings({ "PMD.LooseCoupling" }) // intended use of implementation class HttpHeaders
    private HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        final String apiKeyHeader = config.getControlplane().getApiKey().getHeader();
        if (apiKeyHeader != null) {
            headers.add(apiKeyHeader, config.getControlplane().getApiKey().getSecret());
        }
        return headers;
    }

}
