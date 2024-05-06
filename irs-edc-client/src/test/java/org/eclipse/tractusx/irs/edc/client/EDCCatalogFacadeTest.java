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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createCatalog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
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
        edcConfig.getControlplane().setCatalogPageSize(DEFAULT_PAGE_SIZE);
        edcCatalogFacade = new EDCCatalogFacade(controlPlaneClient, edcConfig);
    }

    @Test
    void shouldReturnEntireCatalogIfAssetIdNotFound() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final String providerBpn = "BPN000123456";
        final var firstPage = createCatalog("other", DEFAULT_PAGE_SIZE);
        final var secondPage = createCatalog("other", 2);

        when(controlPlaneClient.getCatalog(connectorUrl, 0, providerBpn)).thenReturn(firstPage);
        when(controlPlaneClient.getCatalog(connectorUrl, 3, providerBpn)).thenReturn(secondPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId,
                providerBpn);

        // assert
        assertThat(catalog).hasSize(5);
        verify(controlPlaneClient, times(2)).getCatalog(any(), anyInt(), eq(providerBpn));
    }

    @Test
    void shouldReturnEntireCatalogIfAssetIdNotFoundAndPagesAreTheSame() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final String providerBpn = "BPN000123456";
        final var firstPage = createCatalog("other", DEFAULT_PAGE_SIZE);

        when(controlPlaneClient.getCatalog(anyString(), anyInt(), anyString())).thenReturn(firstPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId,
                providerBpn);

        // assert
        assertThat(catalog).hasSize(3);
        verify(controlPlaneClient, times(2)).getCatalog(any(), anyInt(), eq(providerBpn));
    }

    @Test
    void shouldReturnOnePageIfOfferIsFound() {
        // arrange
        final var assetId = "testTarget2";
        final String connectorUrl = "testConnector";
        final String providerBpn = "BPN000123456";
        final var firstPage = createCatalog("testTarget", DEFAULT_PAGE_SIZE);
        final var secondPage = createCatalog("other", DEFAULT_PAGE_SIZE);

        when(controlPlaneClient.getCatalog(connectorUrl, 0, providerBpn)).thenReturn(firstPage);
        when(controlPlaneClient.getCatalog(connectorUrl, DEFAULT_PAGE_SIZE, providerBpn)).thenReturn(secondPage);

        // act
        final List<CatalogItem> catalog = edcCatalogFacade.fetchCatalogItemsUntilMatch(connectorUrl, assetId,
                providerBpn);

        // assert
        assertThat(catalog).hasSize(3);
        verify(controlPlaneClient, times(1)).getCatalog(any(), anyInt(), anyString());
    }

    @Test
    void shouldReturnAndMapCatalog() {
        // arrange
        final var assetId = "testTarget";
        final String connectorUrl = "testConnector";
        final String providerBpn = "BPN000123456";
        final String filterKey = "filterKey";
        final String offerId = "definitionId:%s:randomId".formatted(assetId);
        final Policy policy = Policy.Builder.newInstance()
                                            .type(PolicyType.OFFER)
                                            .permissions(List.of())
                                            .prohibitions(List.of())
                                            .duties(List.of())
                                            .build();
        final Catalog catalog = createCatalog(assetId, policy, providerBpn, offerId);
        when(controlPlaneClient.getCatalogWithFilter(connectorUrl, filterKey, assetId, providerBpn)).thenReturn(
                catalog);

        // act
        final List<CatalogItem> catalogItems = edcCatalogFacade.fetchCatalogByFilter(connectorUrl, filterKey, assetId,
                providerBpn);

        // assert
        assertThat(catalogItems).hasSize(1);
        final CatalogItem catalogItem = catalogItems.get(0);
        assertThat(catalogItem.getAssetPropId()).isEqualTo(assetId);
        assertThat(catalogItem.getItemId()).isEqualTo(assetId);
        assertThat(catalogItem.getConnectorId()).isEqualTo(providerBpn);
        assertThat(catalogItem.getOfferId()).isEqualTo(offerId);
        final Policy catalogPolicy = catalogItem.getPolicy();
        assertThat(catalogPolicy).isNotNull();
        assertThat(catalogPolicy.getAssigner()).isEqualTo(providerBpn);
        assertThat(catalogPolicy.getTarget()).isEqualTo(assetId);

    }
}