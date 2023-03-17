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
package org.eclipse.tractusx.ess.controller;


import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.ess.service.EssRecursiveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application ESS Recursive REST controller.
 */
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
