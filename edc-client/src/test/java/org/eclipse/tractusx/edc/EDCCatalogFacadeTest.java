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
package org.eclipse.tractusx.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.edc.model.CatalogItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

class EDCCatalogFacadeTest {
    private static final Integer DEFAULT_PAGE_SIZE = 3;
    private EDCCatalogFacade edcCatalogFacade;

    private EdcControlPlaneClient controlPlaneClient;
    @Spy
    private EdcConfiguration edcConfig = new EdcConfiguration();

    @BeforeEach
    void setUp() {
        controlPlaneClient = mock(EdcControlPlaneClient.class);
        final EdcConfiguration.ControlplaneConfig controlplaneConfig = new EdcConfiguration.ControlplaneConfig();
        controlplaneConfig.setCatalogPageSize(DEFAULT_PAGE_SIZE);
        edcConfig.setControlplane(controlplaneConfig);
        edcCatalogFacade = new EDCCatalogFacade(controlPlaneClient, edcConfig);
    }

    @Test
    void shouldReturnEntireCatalogIfAssetIdNotFound() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final var firstPage = createCatalog("other", DEFAULT_PAGE_SIZE);
        final var secondPage = createCatalog("other", 2);

        when(controlPlaneClient.getCatalog(connectorUrl, 0)).thenReturn(firstPage);
        when(controlPlaneClient.getCatalog(connectorUrl, 3)).thenReturn(secondPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId);

        // assert
        assertThat(catalog).hasSize(5);
        verify(controlPlaneClient, times(2)).getCatalog(any(), anyInt());
    }

    @Test
    void shouldReturnEntireCatalogIfAssetIdNotFoundAndPagesAreTheSame() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final var firstPage = createCatalog("other", DEFAULT_PAGE_SIZE);

        when(controlPlaneClient.getCatalog(anyString(), anyInt())).thenReturn(firstPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId);

        // assert
        assertThat(catalog).hasSize(3);
        verify(controlPlaneClient, times(2)).getCatalog(any(), anyInt());
    }

    @Test
    void shouldReturnOnePageIfOfferIsFound() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final var firstPage = createCatalog("testTarget", DEFAULT_PAGE_SIZE);
        final var secondPage = createCatalog("other", DEFAULT_PAGE_SIZE);

        when(controlPlaneClient.getCatalog(connectorUrl, 0)).thenReturn(firstPage);
        when(controlPlaneClient.getCatalog(connectorUrl, DEFAULT_PAGE_SIZE)).thenReturn(secondPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId);

        // assert
        assertThat(catalog).hasSize(3);
        verify(controlPlaneClient, times(1)).getCatalog(any(), anyInt());
    }

    private Catalog createCatalog(final String assetId, final int numberOfOffers) {
        final Policy policy = mock(Policy.class);

        final List<ContractOffer> contractOffers = IntStream.range(0, numberOfOffers)
                                                            .boxed()
                                                            .map(i -> ContractOffer.Builder.newInstance()
                                                                                           .id("offer"+i)
                                                                                           .asset(createAsset(assetId))
                                                                                           .policy(policy)
                                                                                           .build())
                                                            .toList();
        return Catalog.Builder.newInstance().id("default").contractOffers(contractOffers).build();
    }

    private static Asset createAsset(final String assetId) {
        return Asset.Builder.newInstance().id(assetId).property("asset:prop:id", assetId).build();
    }
}