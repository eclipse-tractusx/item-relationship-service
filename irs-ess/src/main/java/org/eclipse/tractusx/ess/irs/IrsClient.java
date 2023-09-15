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
package org.eclipse.tractusx.ess.irs;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *  IRS Client to fetch IRS Response
 */
@Service
@Slf4j
class IrsClient {

    private final RestTemplate restTemplate;
    private final String irsUrl;

    /* package */ IrsClient(final RestTemplate oAuthRestTemplate, @Value("${ess.irs.url:}") final String irsUrl) {
        this.restTemplate = oAuthRestTemplate;
        this.irsUrl = irsUrl;
    }

    public JobHandle startJob(final IrsRequest irsRequest) {
        return restTemplate.postForObject(irsUrl + "/irs/jobs", new HttpEntity<>(irsRequest), JobHandle.class);
    }

    public Jobs getJobDetails(final String jobId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(irsUrl);
        uriBuilder.path("/irs/jobs/").path(jobId);

        return restTemplate.getForObject(
                uriBuilder.build().toUri(),
                Jobs.class);
    }

}
