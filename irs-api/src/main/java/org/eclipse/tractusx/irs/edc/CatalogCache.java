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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.configuration.CatalogCacheConfiguration;
import org.eclipse.tractusx.irs.edc.model.CatalogItem;
import org.springframework.stereotype.Service;

/**
 * Cache Facade which returns a ContractOffer. Either from cache or directly from the
 * EDC if not found in Cache.
 */
public interface CatalogCache {

    /**
     * @param connectorUrl The connectur URL from which the ContractOffer should be fetched
     * @param target       The id of the desired ContractOffer
     * @return The Contract offer. If not found, a {@link java.util.NoSuchElementException} is thrown.
     */
    Optional<CatalogItem> getCatalogItem(String connectorUrl, String target);

}

/**
 * In-memory implementation of the catalog cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class InMemoryCatalogCache implements CatalogCache {

    private final Map<String, List<CatalogItem>> catalogCache = new ConcurrentHashMap<>();
    private final EDCCatalogFacade catalogFetcher;
    private final CatalogCacheConfiguration cacheConfig;

    @Override
    public Optional<CatalogItem> getCatalogItem(final String connectorUrl, final String target) {
        cleanupExpiredCacheValues();
        Optional<CatalogItem> catalogItem = getItemFromCache(connectorUrl, target);
        if (catalogItem.isPresent()) {
            log.info("Retrieved Item from cache: '{}'", catalogItem);
        } else {
            log.info("Retrieving Catalog from connector '{}'", connectorUrl);
            catalogItem = getOfferFromCatalog(connectorUrl, target);
            catalogItem.ifPresent(item -> log.info("Retrieved Item from connector: '{}'", item));
        }
        return catalogItem;
    }

    private Optional<CatalogItem> getItemFromCache(final String connectorUrl, final String target) {
        if (catalogCache.containsKey(connectorUrl)) {
            return catalogCache.get(connectorUrl)
                               .stream()
                               .filter(catalogItem -> catalogItem.getAssetPropId().equals(target)
                                       && catalogItem.getValidUntil().isAfter(Instant.now()))
                               .findFirst();
        } else {
            return Optional.empty();
        }
    }

    private Optional<CatalogItem> getOfferFromCatalog(final String connectorUrl, final String target) {
        final List<CatalogItem> catalog = catalogFetcher.getCatalog(connectorUrl, target);

        if (cacheConfig.isEnabled()) {
            List<CatalogItem> catalogItems = new ArrayList<>();
            if (catalogCache.containsKey(connectorUrl)) {
                catalogItems = catalogCache.get(connectorUrl);
            }
            final int listSize = catalog.size();
            if (!cacheHasSpaceLeft(listSize)) {
                removeOldestCacheValues(listSize);
            }
            catalogItems.addAll(catalog.stream().map(this::updateTTL).toList());

            catalogCache.put(connectorUrl, catalogItems);
        }

        return catalog.stream().filter(catalogItem -> catalogItem.getAssetPropId().equals(target)).findFirst();
    }

    private void cleanupExpiredCacheValues() {
        catalogCache.keySet().forEach(this::removeIfExpired);
    }

    private void removeIfExpired(final String key) {
        final List<CatalogItem> expiredItems = catalogCache.get(key)
                                                           .stream()
                                                           .filter(catalogItem -> catalogItem.getValidUntil()
                                                                                             .isBefore(Instant.now()))
                                                           .toList();
        final int size = expiredItems.size();
        if (size > 0) {
            log.info("Found '{}' expired items. Removing '{}'", size, expiredItems);
            catalogCache.get(key).removeIf(catalogItem -> catalogItem.getValidUntil().isBefore(Instant.now()));
        }
    }

    private void removeOldestCacheValues(final int listSize) {
        final List<CatalogItem> oldestCatalogItems = catalogCache.values()
                                                                 .stream()
                                                                 .flatMap(List::stream)
                                                                 .sorted((o1, o2) -> (int) (
                                                                         o1.getValidUntil().toEpochMilli() - o2.getValidUntil().toEpochMilli()))
                                                                 .limit(listSize).toList();
        log.info("Removing '{}' oldest Items: '{}'", oldestCatalogItems.size(), oldestCatalogItems);

        catalogCache.values().forEach(catalogItems -> catalogItems.removeAll(oldestCatalogItems));
    }

    private boolean cacheHasSpaceLeft(final int listSize) {
        final int cacheSize = catalogCache.keySet()
                                          .stream()
                                          .map(s -> catalogCache.get(s).size())
                                          .mapToInt(Integer::intValue)
                                          .sum();

        return (cacheSize + listSize) <= cacheConfig.getMaxCachedItems();
    }

    private CatalogItem updateTTL(final CatalogItem catalogItem) {
        final Instant nowPlusTTL = Instant.now().plus(cacheConfig.getTtl());
        catalogItem.setValidUntil(nowPlusTTL);
        return catalogItem;
    }

}
