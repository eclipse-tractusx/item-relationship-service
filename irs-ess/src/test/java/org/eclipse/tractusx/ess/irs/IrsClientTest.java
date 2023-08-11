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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.ess.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.net.URI;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

class IrsClientTest {

    private static final String URL = "https://irs-esr.dev.demo.catena-x.net/";
    private static final String JOB_ID = "1545da82-e5b3-4fda-bc35-866dc6a29c4c";
    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    void shouldStartJob() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        IrsRequest irsRequest = IrsRequest.builder().key(
                PartChainIdentificationKey.builder().globalAssetId("global-id").bpn("BPNL0000000000DD").build()).bomLifecycle("asBuilt").build();
        JobHandle expectedResponse = JobHandle.builder().id(UUID.fromString(JOB_ID)).build();

        given(restTemplate.postForObject(URL + "/irs/jobs",
                new HttpEntity<>(irsRequest), JobHandle.class))
                .willReturn(expectedResponse);

        // when
        final JobHandle actualResponse = irsClient.startJob(irsRequest);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void shouldFetchResponseWithEmptyList() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        Jobs expectedResponse = Jobs.builder().job(Job.builder().id(UUID.randomUUID()).state(JobState.COMPLETED).build()).build();

        given(restTemplate.getForObject(getUriWithJobId(URL, JOB_ID), Jobs.class))
                .willReturn(expectedResponse);

        // when
        final Jobs actualResponse = irsClient.getJobDetails(JOB_ID);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private URI getUriWithJobId(String url, String jobId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.path("/irs/jobs/").path(jobId);
        return uriBuilder.build().toUri();
    }

}
