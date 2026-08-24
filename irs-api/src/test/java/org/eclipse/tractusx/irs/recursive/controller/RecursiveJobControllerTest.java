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
package org.eclipse.tractusx.irs.recursive.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.recursive.RecursiveTestConstants.ATLAS_BPNL;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStartResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantInactiveException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationSender;
import org.eclipse.tractusx.irs.recursive.service.RecursiveSubmodelCollector;
import org.eclipse.tractusx.irs.recursive.service.RecursiveTraversalService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveUnsupportedAspectException;
import org.eclipse.tractusx.irs.recursive.store.InMemoryRecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveStoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecursiveJobControllerTest {

    private RecursiveJobController controller;
    private RecursiveChainOpeningGrantStore grantStore;
    private Map<UUID, RecursiveJobState> jobs;
    private Map<String, UUID> messageIds;
    private MockMvc mockMvc;
    private RecursiveExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        jobs = new ConcurrentHashMap<>();
        messageIds = new ConcurrentHashMap<>();
        grantStore = new InMemoryRecursiveChainOpeningGrantStore();
        final RecursiveChainOpeningGrantService grantService = new RecursiveChainOpeningGrantService(grantStore);
        final RecursiveTraversalService traversalService = mock(RecursiveTraversalService.class);
        final RecursiveProperties properties = new RecursiveProperties();
        properties.setLocalBpnl(ATLAS_BPNL);
        final RecursiveJobService jobService = new RecursiveJobService(grantService, traversalService,
                newJobStateStore(), mock(RecursiveNotificationSender.class), mock(RecursiveSubmodelCollector.class),
                properties, command -> { }, Clock.systemUTC());
        controller = new RecursiveJobController(jobService);
        exceptionHandler = new RecursiveExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                 .setControllerAdvice(exceptionHandler)
                                 .build();
    }

    @Test
    void shouldReturnBadRequestWithoutCreatingJobWhenUseCaseIsMissingOrBlank() throws Exception {
        final List<String> invalidRequests = List.of(
                """
                {
                  "openingId": "opening-42",
                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"
                }
                """,
                """
                {
                  "openingId": "opening-42",
                  "useCase": " ",
                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"
                }
                """);

        for (final String request : invalidRequests) {
            mockMvc.perform(post("/irs/recursive/jobs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWithoutCreatingJobWhenUseCaseIsUnknown() throws Exception {
        mockMvc.perform(post("/irs/recursive/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openingId": "opening-42",
                                  "useCase": "UNKNOWN_RECURSIVE_USE_CASE",
                                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Malformed recursive request."))
                .andExpect(jsonPath("$.errorRef").isNotEmpty());

        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldRejectOpeningIdContainingLineBreak() throws Exception {
        mockMvc.perform(post("/irs/recursive/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openingId": "opening-42\\nforged-entry",
                                  "useCase": "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE",
                                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid recursive request."));

        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldReturnUnprocessableEntityNamingUnknownAndSupportedAspects() {
        grantStore.store(validGrant());
        final String typo = "urn:samm:io.catenax.item_stock_anonymizd:1.0.0#ItemStockAnonymizd";
        final RecursiveJobRequest request = validRequest().toBuilder().aspects(List.of(typo)).build();

        RecursiveUnsupportedAspectException thrown = null;
        try {
            controller.startJob(request);
        } catch (final RecursiveUnsupportedAspectException e) {
            thrown = e;
        }
        assertThat(thrown).isNotNull();

        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleUnsupportedAspect(thrown);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.UNSUPPORTED_ASPECT);
        assertThat(response.getBody().getUnknownAspects()).containsExactly(typo);
        assertThat(response.getBody().getSupportedAspects()).isNotEmpty();
    }

    @Test
    void shouldCreateJobAndReturnId() {
        grantStore.store(validGrant());

        final ResponseEntity<RecursiveJobStartResponse> response = controller.startJob(validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getJobId()).isNotNull();
    }

    @Test
    void shouldReturnJobStatus() {
        grantStore.store(validGrant());
        final UUID jobId = controller.startJob(validRequest()).getBody().getJobId();

        final ResponseEntity<RecursiveJobStatusResponse> response = controller.getJobStatus(jobId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getJob().getId()).isEqualTo(jobId);
    }

    @Test
    void shouldRejectInvalidJobIdFormat() throws Exception {
        mockMvc.perform(get("/irs/recursive/jobs/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid recursive request."))
                .andExpect(jsonPath("$.errorRef").isNotEmpty());
    }

    @Test
    void shouldReturnForbiddenWhenRootGrantIsMissing() throws Exception {
        mockMvc.perform(post("/irs/recursive/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openingId": "opening-42",
                                  "useCase": "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE",
                                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b",
                                  "requesterBpn": "%s"
                                }
                                """.formatted(ATLAS_BPNL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("CHAIN_OPENING_GRANT_REJECTED"))
                .andExpect(jsonPath("$.message").value("Chain opening grant validation failed."))
                .andExpect(jsonPath("$.errorRef").isNotEmpty());

        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldReturnForbiddenResponseWhenRootGrantIsInactive() throws Exception {
        grantStore.store(validGrant().toBuilder()
                .validFrom(java.time.ZonedDateTime.now().plusHours(1))
                .validTo(java.time.ZonedDateTime.now().plusHours(2))
                .build());

        mockMvc.perform(post("/irs/recursive/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openingId": "opening-42",
                                  "useCase": "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE",
                                  "globalAssetId": "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b",
                                  "requesterBpn": "%s"
                                }
                                """.formatted(ATLAS_BPNL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("CHAIN_OPENING_GRANT_REJECTED"))
                .andExpect(jsonPath("$.message").value("Chain opening grant validation failed."))
                .andExpect(jsonPath("$.errorRef").isNotEmpty());

        assertThat(jobs).isEmpty();
    }

    @Test
    void shouldReturn403WhenGrantMissing() {
        final ResponseEntity<RecursiveErrorResponse> response =
                exceptionHandler.handleGrantValidation(
                        new RecursiveChainOpeningGrantInactiveException("test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.CHAIN_OPENING_GRANT_REJECTED);
    }

    @Test
    void shouldReturnServiceUnavailableWhenRecursivePersistenceFails() {
        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleStoreFailure(
                new RecursiveStoreException("store details", new IllegalStateException("persistence details")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.PERSISTENCE_UNAVAILABLE);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Recursive IRS persistence is temporarily unavailable.");
        assertThat(UUID.fromString(response.getBody().getErrorRef())).isNotNull();
    }

    @Test
    void shouldReturnNeutralResponseForUnexpectedFailure() {
        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleUnexpectedFailure(
                new IllegalStateException("internal implementation details"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.INTERNAL_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected recursive IRS error occurred.");
        assertThat(response.getBody().getMessage()).doesNotContain("implementation details");
        assertThat(UUID.fromString(response.getBody().getErrorRef())).isNotNull();
    }

    private RecursiveJobRequest validRequest() {
        return RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn(ATLAS_BPNL)
                .ttl("PT15M")
                .build();
    }

    private RecursiveChainOpeningGrant validGrant() {
        return RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .allowedBpnlSet(Set.of())
                .requesterBpn(ATLAS_BPNL)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .validFrom(java.time.ZonedDateTime.now().minusHours(1))
                .validTo(java.time.ZonedDateTime.now().plusHours(1))
                .build();
    }

    private RecursiveJobStateStore newJobStateStore() {
        return new RecursiveJobStateStore() {
            @Override
            public void save(final RecursiveJobState state) {
                jobs.put(state.getJobId(), state);
            }

            @Override
            public Optional<RecursiveJobState> findById(final UUID jobId) {
                return Optional.ofNullable(jobs.get(jobId));
            }

            @Override
            public Optional<UUID> findJobIdByIncomingRequestMessageId(final String messageId) {
                return Optional.ofNullable(messageIds.get("in:" + messageId));
            }

            @Override
            public void registerIncomingRequestMessageId(final String messageId, final UUID jobId) {
                if (messageId != null) {
                    messageIds.put("in:" + messageId, jobId);
                }
            }

            @Override
            public Optional<UUID> findJobIdByChildRequestMessageId(final String messageId) {
                return Optional.ofNullable(messageIds.get("out:" + messageId));
            }

            @Override
            public void registerChildRequestMessageId(final String messageId, final UUID jobId) {
                if (messageId != null) {
                    messageIds.put("out:" + messageId, jobId);
                }
            }

            @Override
            public List<RecursiveJobState> findAll() {
                return List.copyOf(jobs.values());
            }
        };
    }

}
