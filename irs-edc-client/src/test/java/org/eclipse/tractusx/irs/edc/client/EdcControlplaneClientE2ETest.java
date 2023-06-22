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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createEdcTransformer;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.api.model.IdResponseDto;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.connector.api.management.contractnegotiation.model.ContractOfferDescription;
import org.eclipse.edc.connector.api.management.contractnegotiation.model.NegotiationInitiateRequestDto;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Disabled
class EdcControlplaneClientE2ETest {

    public static final String DATASPACE_PROTOCOL_HTTP = "dataspace-protocol-http";
    private final EdcConfiguration config = new EdcConfiguration();
    private String providerUrl;
    private EdcControlPlaneClient controlPlaneClient;

    @BeforeEach
    void setUp() {
        providerUrl = "https://irs-test-controlplane-provider.dev.demo.catena-x.net/api/v1/dsp";
        String consumerUrl = "https://irs-test-controlplane.dev.demo.catena-x.net/management/v2";
        config.getControlplane().getEndpoint().setData(consumerUrl);
        config.getControlplane().setRequestTtl(Duration.ofSeconds(5));
        config.getControlplane().setProviderSuffix("/api/v1/dsp");
        config.getControlplane().getApiKey().setHeader("X-Api-Key");
        config.getControlplane().getApiKey().setSecret("123456");
        config.getSubmodel().setPath("/submodel");
        config.getSubmodel().setUrnPrefix("/urn");

        final RestTemplate restTemplate = new RestTemplateBuilder().build();
        final List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                final ObjectMapper mappingJackson2HttpMessageConverterObjectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(
                        mappingJackson2HttpMessageConverterObjectMapper::registerSubtypes);
            }
        }

        final AsyncPollingService pollingService = new AsyncPollingService(Clock.systemUTC(),
                Executors.newScheduledThreadPool(1));

        controlPlaneClient = new EdcControlPlaneClient(restTemplate, pollingService, config, createEdcTransformer());

    }

    @Test
    void shouldBeAbleToRetrieveCatalogWithCatalogRequest() {
        // Arrange
        final CatalogRequest catalogRequest = CatalogRequest.Builder.newInstance()
                                                                    .providerUrl(providerUrl)
                                                                    .protocol(DATASPACE_PROTOCOL_HTTP)
                                                                    .build();

        // Act
        final Catalog catalog = controlPlaneClient.getCatalog(catalogRequest);

        // Assert
        assertThat(catalog.getDatasets()).hasSize(50);
        assertThat(catalog.getContractOffers()).isNull();
        assertThat(catalog.getDataServices()).isNotEmpty();
        assertThat(catalog.getProperties()).isNotEmpty();
    }

    @Test
    void shouldBeAbleToRetrieveCatalogWithCatalogRequestAndQuerySpec() {
        // Arrange
        final QuerySpec querySpec = QuerySpec.Builder.newInstance()
                                                     .filter(new Criterion("https://w3id.org/edc/v0.0.1/ns/id", "=",
                                                             "10"))
                                                     .build();
        final CatalogRequest catalogRequest = CatalogRequest.Builder.newInstance()
                                                                    .providerUrl(providerUrl)
                                                                    .protocol(DATASPACE_PROTOCOL_HTTP)
                                                                    .querySpec(querySpec)
                                                                    .build();

        // Act
        final Catalog submodel = controlPlaneClient.getCatalog(catalogRequest);

        // Assert
        assertThat(submodel.getDatasets()).hasSize(1);
        assertThat(submodel.getDatasets().get(0).getOffers()).hasSize(2);
        assertThat(submodel.getContractOffers()).isNull();
        assertThat(submodel.getDataServices()).isNotEmpty();
        assertThat(submodel.getProperties()).isNotEmpty();
    }

    @Test
    void shouldBeAbleToCallCatalog() {
        // Act
        final Catalog submodel = controlPlaneClient.getCatalog(providerUrl, 0);

        // Assert
        assertThat(submodel.getDatasets()).hasSize(50);
        assertThat(submodel.getDatasets().get(0).getOffers()).hasSize(1);
        assertThat(submodel.getContractOffers()).isNull();
        assertThat(submodel.getDataServices()).isNotEmpty();
        assertThat(submodel.getProperties()).isNotEmpty();

        Set<String> collect = submodel.getDatasets().stream().map(Dataset::getId).collect(toSet());
        System.out.println(collect);
        collect = submodel.getDatasets()
                          .stream()
                          .map(dataset -> dataset.getProperty("https://w3id.org/edc/v0.0.1/ns/id").toString())
                          .collect(toSet());
        System.out.println(collect);
    }

    @Test
    void shouldStartContractNegotiation() throws ExecutionException, InterruptedException {
        final QuerySpec querySpec = QuerySpec.Builder.newInstance()
                                                     .filter(new Criterion("https://w3id.org/edc/v0.0.1/ns/id", "=",
                                                             "10"))
                                                     .build();
        final CatalogRequest catalogRequest = CatalogRequest.Builder.newInstance()
                                                                    .providerUrl(providerUrl)
                                                                    .protocol(DATASPACE_PROTOCOL_HTTP)
                                                                    .querySpec(querySpec)
                                                                    .build();

        // Act
        final Catalog catalog = controlPlaneClient.getCatalog(catalogRequest);
        final Dataset dataset = catalog.getDatasets().stream().findFirst().orElseThrow();
        final Map<String, Policy> offers = dataset.getOffers();
        final Map.Entry<String, Policy> offer = offers.entrySet().stream().findFirst().orElseThrow();

        // Arrange
        final ContractOfferDescription contractOfferDescription = ContractOfferDescription.Builder.newInstance()
                                                                                                  .offerId(
                                                                                                          offer.getKey())
                                                                                                  .assetId(
                                                                                                          offer.getValue()
                                                                                                               .getTarget())
                                                                                                  .policy(offer.getValue())
                                                                                                  .build();

        final String connectorId = "BPNL00000003CRHK";
        final NegotiationInitiateRequestDto negotiationRequest = NegotiationInitiateRequestDto.Builder.newInstance()
                                                                                                      .connectorId(
                                                                                                              connectorId)
                                                                                                      .connectorAddress(
                                                                                                              providerUrl)
                                                                                                      .protocol(
                                                                                                              DATASPACE_PROTOCOL_HTTP)
                                                                                                      .offer(contractOfferDescription)
                                                                                                      .build();
        // Act
        final IdResponseDto negotiationId = controlPlaneClient.startNegotiations(negotiationRequest);

        // Assert
        assertThat(negotiationId).isNotNull();
        assertThat(negotiationId.getId()).isNotBlank();

        final CompletableFuture<NegotiationResponse> negotiationResult = controlPlaneClient.getNegotiationResult(
                negotiationId);
        final NegotiationResponse negotiationResponse = negotiationResult.get();
        assertThat(negotiationResponse.getResponseId()).isNotBlank();

        final Distribution distribution = dataset.getDistributions().get(0);
        final DataAddress dataAddress = DataAddress.Builder.newInstance().type(distribution.getFormat()).build();
        final TransferProcessRequest transferProcessRequest = TransferProcessRequest.builder()
                                                                                    .assetId(
                                                                                            negotiationRequest.getOffer()
                                                                                                              .getAssetId())
                                                                                    .connectorAddress(
                                                                                            negotiationResponse.getCounterPartyAddress())
                                                                                    .contractId(
                                                                                            negotiationResponse.getContractAgreementId())
                                                                                    .dataDestination(dataAddress)
                                                                                    .protocol(DATASPACE_PROTOCOL_HTTP)
                                                                                    .privateProperties(
                                                                                            Map.of("receiverHttpEndpoint",
                                                                                                    "https://backend.app/endpoint-data-reference"))
                                                                                    .build();
        final IdResponseDto transferProcessResponse = controlPlaneClient.startTransferProcess(transferProcessRequest);
        assertThat(transferProcessResponse.getId()).isNotBlank();
    }
}
