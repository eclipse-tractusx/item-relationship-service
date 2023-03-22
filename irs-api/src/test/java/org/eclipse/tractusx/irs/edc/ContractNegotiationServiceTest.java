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
package org.eclipse.tractusx.irs.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractNegotiationServiceTest {

    private static final String CONNECTOR_URL = "dummyConnectorUrl";
    private static final Integer DEFAULT_PAGE_SIZE = 3;
    @InjectMocks
    private ContractNegotiationService testee;

    @Mock
    private EdcControlPlaneClient edcControlPlaneClient;

    @Spy
    private EdcConfiguration config = new EdcConfiguration();

    @BeforeEach
    void setUp() {
        final CatalogCacheConfiguration cacheConfig = new CatalogCacheConfiguration();
        cacheConfig.setTtl(Duration.ofMinutes(10));
        cacheConfig.setMaxCachedItems(1000L);
        EDCCatalogFacade catalogFetcher = new EDCCatalogFacade(edcControlPlaneClient, config);
        final CatalogCache catalogCache = new InMemoryCatalogCache(catalogFetcher, cacheConfig);
        testee = new ContractNegotiationService(edcControlPlaneClient, config, catalogCache);
    }

    @Test
    void shouldNegotiateSuccessfully() throws ContractNegotiationException {
        // arrange
        final var assetId = "testTarget";
        final var catalog = mockCatalog(assetId);
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, 0)).thenReturn(catalog);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                NegotiationId.builder().value("negotiationId").build());
        CompletableFuture<NegotiationResponse> response = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                TransferProcessId.builder().value("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));

        // act
        NegotiationResponse result = testee.negotiate(CONNECTOR_URL, assetId);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getContractAgreementId()).isEqualTo("agreementId");
    }

    @Test
    void shouldNegotiateSuccessfullyWithCatalogOnSecondPage() throws ContractNegotiationException {
        // arrange
        final var assetId = "testTarget";
        final var firstPage = mockCatalog("other");
        final var catalog = mockCatalog(assetId);
        setPageSizeInCatalog();
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, 0)).thenReturn(firstPage);
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, DEFAULT_PAGE_SIZE)).thenReturn(catalog);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                NegotiationId.builder().value("negotiationId").build());
        CompletableFuture<NegotiationResponse> response = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                TransferProcessId.builder().value("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));

        // act
        NegotiationResponse result = testee.negotiate(CONNECTOR_URL, assetId);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getContractAgreementId()).isEqualTo("agreementId");
    }

    @Test
    void shouldThrowExceptionWhenAssetIsMissingForBothPage() {
        // arrange
        final var assetId = "testTarget";
        final var firstPage = mockCatalog("other", DEFAULT_PAGE_SIZE);
        final var secondPage = mockCatalog("other", 2);
        setPageSizeInCatalog();
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, 0)).thenReturn(firstPage);
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, 3)).thenReturn(secondPage);

        // act + assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, assetId))
                .isInstanceOf(NoSuchElementException.class);

    }

    private void setPageSizeInCatalog() {
        final EdcConfiguration.ControlplaneConfig controlplaneConfig = new EdcConfiguration.ControlplaneConfig();
        controlplaneConfig.setCatalogPageSize(DEFAULT_PAGE_SIZE);
        config.setControlplane(controlplaneConfig);
    }

    private static Catalog mockCatalog(final String assetId) {
        return mockCatalog(assetId, DEFAULT_PAGE_SIZE);
    }


    private static Catalog mockCatalog(final String assetId, final int numberOfElements) {
        final var catalog = mock(Catalog.class);
        final var contractOffer = mock(ContractOffer.class);
        final var asset = mock(Asset.class);
        when(asset.getId()).thenReturn(assetId);
        when(contractOffer.getAsset()).thenReturn(asset);
        when(contractOffer.getId()).thenReturn(assetId);
        when(catalog.getContractOffers()).thenReturn(IntStream.range(0, numberOfElements)
                                                                      .boxed()
                                                                      .map(i -> contractOffer)
                                                                      .collect(Collectors.toList()));
        return catalog;
    }

    private static Catalog mockCatalog(final String assetId, List<String> offerIdList) {
        final var catalog = mock(Catalog.class);
        final var asset = mock(Asset.class);
        final var policy = mock(Policy.class);
        when(asset.getId()).thenReturn(assetId);
        when(catalog.getContractOffers()).thenReturn(offerIdList.stream()
                                                                .map(id -> ContractOffer.Builder.newInstance()
                                                                                                .id(id)
                                                                                                .asset(asset)
                                                                                                .policy(policy)
                                                                                                .build())
                                                                .collect(Collectors.toList()));
        return catalog;
    }

    @Test
    void shouldThrowErrorWhenRetrievingNegotiationResult() {
        // arrange
        final var assetId = "testTarget";
        final var catalog = mockCatalog(assetId);
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL, 0)).thenReturn(catalog);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                NegotiationId.builder().value("negotiationId").build());
        CompletableFuture<NegotiationResponse> response = CompletableFuture.failedFuture(
                new RuntimeException("Test exception"));
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);

        // act & assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, assetId)).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldThrowExceptionWhenAssetIdIsMissingAndPagesAreTheSame() {
        // arrange
        final var assetId = "testTarget";
        final var firstPage = mockCatalog("other", List.of("offer1", "offer2", "offer3", "offer4"));
        setPageSizeInCatalog();
        when(edcControlPlaneClient.getCatalog(anyString(), anyInt())).thenReturn(firstPage);

        // act + assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, assetId))
                .isInstanceOf(NoSuchElementException.class);
    }
}