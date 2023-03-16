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
 return null;
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache Facade which returns a ContractOffer. Either from cache or directly from the EDC if not found in Cache.
 */
public interface CatalogCache {

    /**
     * @param connectorUrl The connectur URL from which the ContractOffer should be fetched
     * @param target       The id of the desired ContractOffer
     * @return The Contract offer. If not found, a {@link java.util.NoSuchElementException} is thrown.
     */
    CatalogItem getCatalogItem(String connectorUrl, String target);

}

@Slf4j
@RequiredArgsConstructor
class InMemoryCatalogCache implements CatalogCache {

    private final Map<String, List<CatalogItem>> catalogCache = new HashMap<>();
    private final EDCCatalogFetcher catalogFetcher;

    @Override
    public CatalogItem getCatalogItem(final String connectorUrl, final String target) {
        final Optional<CatalogItem> catalogItems = getItemFromCache(connectorUrl, target);
        if (catalogItems.isEmpty()) {
            return getOfferFromCatalog(connectorUrl, target).orElseThrow();
        }
        return catalogItems.get();
    }

    private Optional<CatalogItem> getItemFromCache(final String connectorUrl, final String target) {
        if (catalogCache.containsKey(connectorUrl)) {
            return catalogCache.get(connectorUrl)
                               .stream()
                               .filter(catalogItem -> catalogItem.getAssetPropId().equals(target))
                               .findFirst();
        } else {
            return Optional.empty();
        }
    }

    private Optional<CatalogItem> getOfferFromCatalog(final String connectorUrl, final String target) {
        final List<CatalogItem> catalog = catalogFetcher.getCatalog(connectorUrl, target);

        List<CatalogItem> catalogItems = new ArrayList<>();
        if (catalogCache.containsKey(connectorUrl)) {
            catalogItems = catalogCache.get(connectorUrl);
        }
        catalogItems.addAll(catalog);
        // TODO add logic to manage the cache size, rotation and TTL behaviour
        catalogCache.put(connectorUrl, catalogItems);

        return catalog.stream().filter(catalogItem -> catalogItem.getAssetPropId().equals(target)).findFirst();
    }

}
