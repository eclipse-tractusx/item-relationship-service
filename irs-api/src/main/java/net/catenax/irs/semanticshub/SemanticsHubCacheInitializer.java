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

import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Initializing Semantics Hub cache with values
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SemanticsHubCacheInitializer {

    private final SemanticsHubFacade semanticsHubFacade;

    /**
     * Initializing Semantics Hub cache with values, initially after application starts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCacheValues() {
        log.debug("Initializing Semantics Hub Cache with values.");

        try {
            Stream.of(SchemaModel.values()).forEach(model -> semanticsHubFacade.getModelJsonSchema(model.getUrn()));
        } catch (final HttpServerErrorException ex) {
            log.error("Initialization of Semantics Hub Cache failed", ex);
        }
    }

    /**
     * Cleaning up Semantics Hub cache after scheduled time, and reinitializing it once again.
     */
    @Scheduled(cron = "${semanticsHub.cleanup.scheduler}")
    /* package */ void reinitializeAllCacheInterval() {
        log.debug("Reinitializing Semantics Hub Cache with new values.");

        semanticsHubFacade.evictAllCacheValues();
        initializeCacheValues();
    }

}
