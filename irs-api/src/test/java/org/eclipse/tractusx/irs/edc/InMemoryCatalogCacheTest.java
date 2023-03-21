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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCatalogCacheTest {

    private CatalogCacheConfiguration cacheConfig;
    private EDCCatalogFetcher catalogFetcher;

    private InMemoryCatalogCache catalogCache;

    @BeforeEach
    void setUp() {
        cacheConfig = new CatalogCacheConfiguration();
        cacheConfig.setMaxCachedItems(50L);
        cacheConfig.setTtl(Duration.parse("P1D"));
        cacheConfig.setEnabled(true);

        catalogFetcher = mock(EDCCatalogFetcher.class);
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
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem)).thenReturn(List.of());

        // Act
        final CatalogItem catalogItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final CatalogItem catalogItem2 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(catalogItem1).isEqualTo(catalogItem2);
        verify(catalogFetcher, times(1)).getCatalog(any(), any());
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
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem1))
                                                     .thenReturn(List.of(catalogItem2));

        // Act
        final CatalogItem returnedItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final CatalogItem returnedItem2 = catalogCache.getCatalogItem(connectorUrl, "different-test-asset-id");

        // Assert
        assertThat(returnedItem1).isNotEqualTo(returnedItem2);
        verify(catalogFetcher, times(2)).getCatalog(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        // Arrange
        final String connectorUrl = "test-url";

        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId("testId")
                                                   .connectorId("testConnector")
                                                   .assetPropId("not-test-asset-id")
                                                   .build();
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem)).thenReturn(List.of());

        // Assert
        assertThatThrownBy(() -> catalogCache.getCatalogItem(connectorUrl, "test-asset-id")).isInstanceOf(
                NoSuchElementException.class);
        verify(catalogFetcher, times(1)).getCatalog(any(), any());
    }

    @Test
    void shouldCallCatalogAgainAfterTTL() throws InterruptedException {
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
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem1))
                                                     .thenReturn(List.of(catalogItem1, catalogItem2));
        final Duration cacheTTL = Duration.ofSeconds(2);

        cacheConfig.setTtl(cacheTTL);

        // Act
        final CatalogItem returnedItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        Thread.sleep(3000);
        final CatalogItem returnedItem2 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");

        // Assert
        assertThat(returnedItem1).isEqualTo(returnedItem2);
        verify(catalogFetcher, times(2)).getCatalog(any(), any());
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

        when(catalogFetcher.getCatalog(eq(connectorUrl1), any())).thenReturn(catalogItemsBatch1);
        when(catalogFetcher.getCatalog(eq(connectorUrl2), any())).thenReturn(catalogItemsBatch2);

        // Act
        final CatalogItem returnedItem1 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-25");
        final CatalogItem returnedItem2 = catalogCache.getCatalogItem(connectorUrl2, "asset-id-51");
        final CatalogItem returnedItem3 = catalogCache.getCatalogItem(connectorUrl1, "asset-id-1");

        // Assert
        assertThat(returnedItem1.getItemId()).isEqualTo("id-25");
        assertThat(returnedItem2.getItemId()).isEqualTo("id-51");
        assertThat(returnedItem3.getItemId()).isEqualTo("id-1");

        verify(catalogFetcher, times(3)).getCatalog(any(), any());
    }

}