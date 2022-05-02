//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

import io.swagger.v3.oas.annotations.media.Schema;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.services.AsyncJobHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Describe the state of the fetched items
 */

@Schema(description = "Controller for asynchronous JobOrchestration")
@RestController
@ExcludeFromCodeCoverageGeneratedReport
public class IrsConsumerController {

    private final AsyncJobHandlerService jobhandler;

    @Autowired
    public IrsConsumerController(AsyncJobHandlerService jobhandler) {
        this.jobhandler = jobhandler;
    }

}
