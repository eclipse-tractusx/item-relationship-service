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
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Listens for {@link JobProcessingFinishedEvent} and calling callbackUrl with notification.
 * Execution is done in a separate thread.
 */
@Slf4j
@Service
class JobProcessingEventListener {

    private final UrlValidator urlValidator;
    private final RestTemplate restTemplate;

    /* package */ JobProcessingEventListener(final RestTemplate noErrorRestTemplate) {
        this.urlValidator = new UrlValidator();
        this.restTemplate = noErrorRestTemplate;
    }

    @Async
    @EventListener
    public void handleJobProcessingFinishedEvent(final JobProcessingFinishedEvent jobProcessingFinishedEvent) {
        if (thereIsCallbackUrlRegistered(jobProcessingFinishedEvent.getCallbackUrl())) {
            log.info("Processing of job has finished - attempting to notify job requestor");

            final URI callbackUri = buildCallbackUri(jobProcessingFinishedEvent);
            if (urlValidator.isValid(callbackUri.toString())) {
                log.info("Got callback url {} for jobId {} with state {}", callbackUri,
                        jobProcessingFinishedEvent.getJobId(), jobProcessingFinishedEvent.getJobState());

                final ResponseEntity<Void> callbackResponse = restTemplate.getForEntity(callbackUri, Void.class);
                log.info("Callback url pinged, received http status: {}", callbackResponse.getStatusCode());
            }
        }
    }

    private boolean thereIsCallbackUrlRegistered(final String callbackUrl) {
        return StringUtils.isNotBlank(callbackUrl);
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private URI buildCallbackUri(final JobProcessingFinishedEvent jobProcessingFinishedEvent) {
        final Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("jobId", jobProcessingFinishedEvent.getJobId());
        uriVariables.put("jobState", jobProcessingFinishedEvent.getJobState());

        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(jobProcessingFinishedEvent.getCallbackUrl());
        uriComponentsBuilder.uriVariables(uriVariables);
        return uriComponentsBuilder.build().toUri();
    }
}
