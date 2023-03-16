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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

class InMemoryCatalogCacheTest {

    @Test
    void shouldReturnCachedItemWhenRequestingSameItemTwice() {
        // Arrange
        final String connectorUrl = "test-url";

        final EDCCatalogFetcher catalogFetcher = mock(EDCCatalogFetcher.class);
        final CatalogItem catalogItem = CatalogItem.builder()
                                             .itemId("testId")
                                             .connectorId("testConnector")
                                             .assetPropId("test-asset-id")
                                             .build();
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem)).thenReturn(List.of());
        final InMemoryCatalogCache catalogCache = new InMemoryCatalogCache(catalogFetcher);

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

        final EDCCatalogFetcher catalogFetcher = mock(EDCCatalogFetcher.class);
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
        when(catalogFetcher.getCatalog(any(), any())).thenReturn(List.of(catalogItem1)).thenReturn(List.of(catalogItem2));
        final InMemoryCatalogCache catalogCache = new InMemoryCatalogCache(catalogFetcher);

        // Act
        final CatalogItem returnedItem1 = catalogCache.getCatalogItem(connectorUrl, "test-asset-id");
        final CatalogItem returnedItem2 = catalogCache.getCatalogItem(connectorUrl, "different-test-asset-id");

        // Assert
        assertThat(returnedItem1).isNotEqualTo(returnedItem2);
        verify(catalogFetcher, times(2)).getCatalog(any(), any());
    }
}