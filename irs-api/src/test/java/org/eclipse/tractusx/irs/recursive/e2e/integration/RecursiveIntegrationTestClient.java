/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStartResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client used by the recursive IRS integration tests.
 */
public final class RecursiveIntegrationTestClient {

    public static final String ADMIN_API_KEY =
            "eyJhbGciOiJub25lIn0.eyJleHAiOjk5OTk5OTk5OTksImRhZCI6InByb2R1Y3Rpb24tbGlrZSIsImNpZCI6ImFwaS1rZXkifQ.test";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final RestTemplate restTemplate = restTemplate();

    public void registerGrant(final RecursiveIrsInstance instance,
            final RecursiveChainOpeningGrant grant) {
        final String target = instance.baseUrl() + "/irs/recursive/chain-openings/grants";
        restTemplate.postForEntity(target, new HttpEntity<>(grant, apiHeaders()), RecursiveChainOpeningGrant.class);
    }

    public String startRootJob(final RecursiveIrsInstance instance, final RecursiveJobRequest request) {
        final String target = instance.baseUrl() + "/irs/recursive/jobs";
        final ResponseEntity<RecursiveJobStartResponse> response = restTemplate.postForEntity(target,
                new HttpEntity<>(request, apiHeaders()), RecursiveJobStartResponse.class);
        return response.getBody().getJobId();
    }

    public RecursiveJobStatusResponse waitForTerminalJob(final RecursiveIrsInstance instance,
            final String jobId) {
        await().pollDelay(Duration.ZERO)
                .pollInterval(Duration.ofMillis(250))
                .timeout(Duration.ofMinutes(3))
                .until(() -> {
                    final JobState state = jobStatus(instance, jobId).getJob().getState();
                    return state == JobState.COMPLETED || state == JobState.ERROR;
                });
        return jobStatus(instance, jobId);
    }

    public RecursiveJobStatusResponse jobStatus(final RecursiveIrsInstance instance, final String jobId) {
        final String target = instance.baseUrl() + "/irs/recursive/jobs/" + jobId;
        final ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET,
                new HttpEntity<>(apiHeaders()), String.class);
        try {
            return OBJECT_MAPPER.readValue(response.getBody(), RecursiveJobStatusResponse.class);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not deserialize recursive integration job status", e);
        }
    }

    /**
     * Delivers a notification directly to the instance's notification endpoint, as the data
     * plane would: the {@code edc-bpn} header carries the transport identity of the sender.
     */
    public ResponseEntity<RecursiveNotificationResponse> sendNotification(final RecursiveIrsInstance instance,
            final String senderBpnl,
            final RecursiveNotificationMessage message) {
        final String target = instance.baseUrl() + "/irs/recursive/notifications";
        final HttpHeaders headers = apiHeaders();
        headers.set("edc-bpn", senderBpnl);
        return restTemplate.postForEntity(target, new HttpEntity<>(message, headers),
                RecursiveNotificationResponse.class);
    }

    public List<RecursiveJobStatusResponse> jobs(final RecursiveIrsInstance instance) {
        final String target = instance.baseUrl() + "/irs/recursive/jobs";
        final ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET,
                new HttpEntity<>(apiHeaders()), String.class);
        try {
            return OBJECT_MAPPER.readValue(response.getBody(), new TypeReference<>() {
            });
        } catch (final Exception e) {
            throw new IllegalStateException("Could not deserialize recursive integration jobs", e);
        }
    }

    private static HttpHeaders apiHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", ADMIN_API_KEY);
        return headers;
    }

    private static RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .forEach(converter -> converter.setObjectMapper(OBJECT_MAPPER));
        return restTemplate;
    }
}
