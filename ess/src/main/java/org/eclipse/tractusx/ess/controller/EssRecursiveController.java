package org.eclipse.tractusx.ess.controller;

import java.util.Optional;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.ess.service.EssRecursiveService;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ess/notification")
@RequiredArgsConstructor
@Validated
@Hidden
@Slf4j
public class EssRecursiveController {

    private final EssRecursiveService essRecursiveService;

    @PostMapping("/receive-recursive")
    public void registerRecursiveBPNInvestigation(final @Valid @RequestBody EdcNotification notification) throws
            EdcClientException {
        log.info("receive recursive Notification mock called");
        essRecursiveService.handleNotification(notification);
    }

}
