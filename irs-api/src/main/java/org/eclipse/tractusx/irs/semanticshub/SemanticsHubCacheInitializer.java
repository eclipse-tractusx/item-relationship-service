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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Initializing Semantics Hub cache with values
 */
@Service
@Slf4j
class SemanticsHubCacheInitializer {

    private final SemanticsHubFacade semanticsHubFacade;
    private final List<String> defaultUrns;

    /* package */ SemanticsHubCacheInitializer(final SemanticsHubFacade semanticsHubFacade,
            @Value("${semanticshub.defaultUrns:}") final List<String> defaultUrns) {
        this.semanticsHubFacade = semanticsHubFacade;
        this.defaultUrns = defaultUrns;
    }

    /**
     * Initializing Semantics Hub cache with values, initially after application starts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCacheValues() {
        log.debug("Initializing Semantics Hub Cache with values.");

        defaultUrns.forEach(urn -> {
            try {
                semanticsHubFacade.getModelJsonSchema(urn);
            } catch (final HttpServerErrorException | SchemaNotFoundException ex) {
                log.error("Initialization of semantic hub cache failed for URN '{}'", urn, ex);
            }
        });
        try {
            semanticsHubFacade.getAllAspectModels();
        } catch (SchemaNotFoundException e) {
            log.error("Initialization of semantic model cache failed.", e);
        }
    }

    /**
     * Cleaning up Semantics Hub cache after scheduled time, and reinitializing it once again.
     */
    @Scheduled(cron = "${semanticshub.cleanup.scheduler}")
    /* package */ void reinitializeAllCacheInterval() {
        log.debug("Reinitializing Semantics Hub Cache with new values.");

        defaultUrns.stream().findFirst().ifPresentOrElse(urn -> {
            try {
                semanticsHubFacade.getModelJsonSchema(urn);
                log.info("Could retrieve schema. Reinitializing cache values");
                semanticsHubFacade.evictAllCacheValues();
                initializeCacheValues();
            } catch (SchemaNotFoundException e) {
                log.error("Error while retrieving semantic models for cache. Reusing existing cached values", e);
            }
        }, () -> log.warn("Semantic models could not be retrieved for cache. Reusing existing cached value"));
    }

}
