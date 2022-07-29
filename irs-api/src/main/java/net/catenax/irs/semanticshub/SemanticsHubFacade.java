//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.semanticshub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for semantics hub domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticsHubFacade {

    private static final String SEMANTICS_HUB_CACHE_NAME = "schema_cache";

    private final SemanticsHubClient semanticsHubClient;

    /**
     * Returning requested Schema model and putting result into cache
     * @param urn of the model
     * @return Json Schema
     */
    @Cacheable(value = SEMANTICS_HUB_CACHE_NAME, key = "#urn")
    public String getModelJsonSchema(final String urn) {
        return this.semanticsHubClient.getModelJsonSchema(urn);
    }

    /**
     * Clearing cache from all values
     */
    @CacheEvict(value = SEMANTICS_HUB_CACHE_NAME, allEntries = true)
    public void evictAllCacheValues() {
        log.debug("Clearing Semantics Hub Cache.");
    }

}
