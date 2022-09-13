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
package org.eclipse.tractusx.esr.irs;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *  IRS Client to fetch IRS Response
 */
@Service
@Slf4j
public class IrsClient {

    private final RestTemplate restTemplate;
    private final String irsUrl;

    IrsClient(final RestTemplate defaultRestTemplate, @Value("${esr.irs.url:}") final String irsUrl) {
        this.restTemplate = defaultRestTemplate;
        this.irsUrl = irsUrl;
    }

    StartJobResponse startJob(IrsRequest irsRequest, String authorizationToken) {
        return restTemplate.postForObject(irsUrl + "/irs/jobs",
                new HttpEntity<>(irsRequest, tokenInHeaders(authorizationToken)), StartJobResponse.class);
    }

    @Retry(name = "waiting-for-completed")
    IrsResponse getJobDetails(String jobId, String authorizationToken) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(irsUrl);
        uriBuilder.path("/irs/jobs/").path(jobId);
        return restTemplate.exchange(
                uriBuilder.build().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(null, tokenInHeaders(authorizationToken)),
                IrsResponse.class).getBody();
    }

    private static HttpHeaders tokenInHeaders(final String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", "Bearer " + authorizationToken);
        return headers;
    }

}
