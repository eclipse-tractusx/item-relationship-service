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
package org.eclipse.tractusx.irs.semanticshub;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
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
    private static final String SEMANTICS_HUB_MODEL_CACHE_NAME = "model_cache";

    private final SemanticsHubClient semanticsHubClient;

    /**
     * Returning requested Schema model and putting result into cache
     * @param urn of the model
     * @return Json Schema
     */
    @Cacheable(value = SEMANTICS_HUB_CACHE_NAME, key = "#urn")
    public String getModelJsonSchema(final String urn) throws SchemaNotFoundException {
        log.info("Retrieving json schema for urn {}", urn);
        return this.semanticsHubClient.getModelJsonSchema(urn);
    }

    /**
     * Clearing cache from all values
     */
    @CacheEvict(value = SEMANTICS_HUB_CACHE_NAME, allEntries = true)
    public void evictAllCacheValues() {
        log.debug("Clearing Semantics Hub Cache.");
    }

    /**
     * Search in Semantic hub or local models for all available aspect models.
     * @return All available aspect models.
     */
    @Cacheable(SEMANTICS_HUB_MODEL_CACHE_NAME)
    public AspectModels getAllAspectModels() throws SchemaNotFoundException {
        final List<AspectModel> aspectModels = this.semanticsHubClient.getAllAspectModels();

        final String lastUpdated = ZonedDateTime.now(Clock.systemUTC()).toString();
        return AspectModels.builder().models(aspectModels).lastUpdated(lastUpdated).build();
    }

}
