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
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.tractusx.edc.model.CatalogItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCatalogCacheTest {

    private CatalogCacheConfiguration cacheConfig;
    private EDCCatalogFacade catalogFetcher;

    private InMemoryCatalogCache catalogCache;

    @BeforeEach
    void setUp() {
        cacheConfig = new CatalogCacheConfiguration();
        cacheConfig.setMaxCachedItems(50L);
        cacheConfig.setTtl(Duration.parse("P1D"));
        cacheConfig.setEnabled(true);

        catalogFetcher = mock(EDCCatalogFacade.class);
        catalogCache = new InMemoryCatalogCache(catalogFetcher, cacheConfig);
    }

    @Test
    void shouldReturnCachedItemWhenRequestingSameItemTwice() {
        // Arrange
        final String connectorUrl = "test-url";
        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId("testId")
                                                   .connectorId("testConnector")
                                                   .assetPropId("test-asset-id")
                                                   .build();
        when(catalogFetcher.fetchCatalogItemsUntilMatch(any(), any())).thenReturn(List.of(catalogItem))
                                                                      .thenReturn(List.of());

        // Act
        final Optional<CatalogItem> catalogItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final Optional<CatalogItem> catalogItem2 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(catalogItem1).isEqualTo(catalogItem2);
        verify(catalogFetcher, times(1)).fetchCatalogItemsUntilMatch(any(), any());
    }

    @Test
    void shouldReturnDifferentItemsForDifferentIds() {
        // Arrange
        final String connectorUrl = "test-url";
        final CatalogItem catalogItem1 = CatalogItem.builder()
                                                    .itemId("testId")
                                                    .connectorId("testConnector")
                                                    .assetPropId("test-asset-id")
                                                    .build();
        final CatalogItem catalogItem2 = CatalogItem.builder()
                                                    .itemId("differentTestId")
                                                    .connectorId("differentTestConnector")
                                                    .assetPropId("different-test-asset-id")
                                                    .build();
        when(catalogFetcher.fetchCatalogItemsUntilMatch(any(), any())).thenReturn(List.of(catalogItem1))
                                                                      .thenReturn(List.of(catalogItem2));

        // Act
        final Optional<CatalogItem> returnedItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final Optional<CatalogItem> returnedItem2 = catalogCache.getCatalogItem(connectorUrl,
                "different-test-asset-id");

        // Assert
        assertThat(returnedItem1).isPresent();
        assertThat(returnedItem2).isPresent();
        assertThat(returnedItem1.get()).isNotEqualTo(returnedItem2.get());
        verify(catalogFetcher, times(2)).fetchCatalogItemsUntilMatch(any(), any());
    }

    @Test
    void shouldReturnEmptyWhenItemNotFound() {
        // Arrange
        final String connectorUrl = "test-url";

        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId("testId")
                                                   .connectorId("testConnector")
                                                   .assetPropId("not-test-asset-id")
                                                   .build();
        when(catalogFetcher.fetchCatalogItemsUntilMatch(any(), any())).thenReturn(List.of(catalogItem))
                                                                      .thenReturn(List.of());

        // Act
        final Optional<CatalogItem> returnedItem = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(returnedItem).isEmpty();
        verify(catalogFetcher, times(1)).fetchCatalogItemsUntilMatch(any(), any());
    }

    @Test
    void shouldCallCatalogAgainAfterTTL() {
        // Arrange
        final String connectorUrl = "test-url";
        final CatalogItem catalogItem1 = CatalogItem.builder()
                                                    .itemId("testId")
                                                    .connectorId("testConnector")
                                                    .assetPropId("test-asset-id")
                                                    .build();
        final CatalogItem catalogItem2 = CatalogItem.builder()
                                                    .itemId("differentTestId")
                                                    .connectorId("differentTestConnector")
                                                    .assetPropId("different-test-asset-id")
                                                    .build();
        when(catalogFetcher.fetchCatalogItemsUntilMatch(any(), any())).thenReturn(List.of(catalogItem1))
                                                                      .thenReturn(List.of(catalogItem1, catalogItem2));
        final Duration cacheTTL = Duration.ofSeconds(2);

        cacheConfig.setTtl(cacheTTL);

        // Act
        final Optional<CatalogItem> returnedItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        waitSeconds(3);

        final Optional<CatalogItem> returnedItem2 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(returnedItem1).isEqualTo(returnedItem2);
        verify(catalogFetcher, times(2)).fetchCatalogItemsUntilMatch(any(), any());
    }

    private void waitSeconds(final long seconds) {
        final Instant now = Instant.now();
        await().pollInterval(seconds, TimeUnit.SECONDS)
               .until(() -> Instant.now().isAfter(now.plus(Duration.ofSeconds(seconds))));
    }

    @Test
    void shouldRemoveOldestItemsWhenCatalogMaxSizeIsReached() {
        // Arrange
        final String connectorUrl1 = "test1-url";
        final String connectorUrl2 = "test2-url";
        final List<CatalogItem> catalogItemsBatch1 = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            catalogItemsBatch1.add(CatalogItem.builder()
                                              .itemId("id-" + i)
                                              .connectorId("connector-" + i)
                                              .assetPropId("asset-id-" + i)
                                              .build());
        }
        final List<CatalogItem> catalogItemsBatch2 = new ArrayList<>();
        for (int i = 51; i <= 100; i++) {
            catalogItemsBatch2.add(CatalogItem.builder()
                                              .itemId("id-" + i)
                                              .connectorId("connector-" + i)
                                              .assetPropId("asset-id-" + i)
                                              .build());
        }

        when(catalogFetcher.fetchCatalogItemsUntilMatch(eq(connectorUrl1), any())).thenReturn(catalogItemsBatch1);
        when(catalogFetcher.fetchCatalogItemsUntilMatch(eq(connectorUrl2), any())).thenReturn(catalogItemsBatch2);

        // Act
        final Optional<CatalogItem> returnedItem1 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-25");
        final Optional<CatalogItem> returnedItem2 = catalogCache.getCatalogItem(connectorUrl2, "asset-id-51");
        final Optional<CatalogItem> returnedItem3 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-1");

        // Assert
        assertThat(returnedItem1).isPresent();
        assertThat(returnedItem2).isPresent();
        assertThat(returnedItem3).isPresent();
        assertThat(returnedItem1.get().getItemId()).isEqualTo("id-25");
        assertThat(returnedItem2.get().getItemId()).isEqualTo("id-51");
        assertThat(returnedItem3.get().getItemId()).isEqualTo("id-1");

        verify(catalogFetcher, times(3)).fetchCatalogItemsUntilMatch(any(), any());
    }

    @Test
    void shouldOnlyCacheMaxConfiguredAmountOfItems() {
        // Arrange
        final String connectorUrl1 = "test1-url";
        final List<CatalogItem> catalogItemsBatch1 = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            catalogItemsBatch1.add(CatalogItem.builder()
                                              .itemId("id-" + i)
                                              .connectorId("connector-" + i)
                                              .assetPropId("asset-id-" + i)
                                              .build());
        }
        cacheConfig.setMaxCachedItems(10L);
        when(catalogFetcher.fetchCatalogItemsUntilMatch(eq(connectorUrl1), any())).thenReturn(catalogItemsBatch1);

        // Act
        // first retrieval will call connector
        final Optional<CatalogItem> returnedItem1 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-1");
        // second retrieval will call connector since the cache is limited to 10
        final Optional<CatalogItem> returnedItem2 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-22");
        // third retrieval will call cache since the id is within the first 10 items
        final Optional<CatalogItem> returnedItem3 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-2");

        // Assert
        assertThat(returnedItem1).isPresent();
        assertThat(returnedItem2).isPresent();
        assertThat(returnedItem3).isPresent();
        assertThat(returnedItem1.get().getItemId()).isEqualTo("id-1");
        assertThat(returnedItem2.get().getItemId()).isEqualTo("id-22");
        assertThat(returnedItem3.get().getItemId()).isEqualTo("id-2");

        verify(catalogFetcher, times(2)).fetchCatalogItemsUntilMatch(any(), any());
    }

    @Test
    void shouldNotUseCacheWhenDisabled() {
        // Arrange
        final String connectorUrl = "test-url";
        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId("testId")
                                                   .connectorId("testConnector")
                                                   .assetPropId("test-asset-id")
                                                   .build();
        cacheConfig.setEnabled(false);
        when(catalogFetcher.fetchCatalogItemsUntilMatch(any(), any())).thenReturn(List.of(catalogItem))
                                                                      .thenReturn(List.of(catalogItem))
                                                                      .thenReturn(List.of());

        // Act
        final Optional<CatalogItem> catalogItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final Optional<CatalogItem> catalogItem2 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        final Optional<CatalogItem> catalogItem3 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(catalogItem3).isEmpty();
        assertThat(catalogItem1).isEqualTo(catalogItem2);
        verify(catalogFetcher, times(3)).fetchCatalogItemsUntilMatch(any(), any());
    }
}