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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
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
            @Value("${semanticsHub.defaultUrns:}") final List<String> defaultUrns) {
        this.semanticsHubFacade = semanticsHubFacade;
        this.defaultUrns = defaultUrns;
    }

    /**
     * Initializing Semantics Hub cache with values, initially after application starts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCacheValues() {
        log.debug("Initializing Semantics Hub Cache with values.");

        try {
            defaultUrns.forEach(semanticsHubFacade::getModelJsonSchema);
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
