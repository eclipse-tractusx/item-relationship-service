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
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrantKey;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantAlreadyExistsException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantNotFoundException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantInactiveException;
import org.eclipse.tractusx.irs.recursive.store.InMemoryRecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RecursiveChainOpeningGrantControllerTest {

    private static final String OPENING_ID = "opening-42";
    private static final RecursiveUseCase USE_CASE = RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String REQUESTER_BPN = "BPNL0000ATLS0001";
    private static final String GLOBAL_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";

    private RecursiveChainOpeningGrantController controller;
    private RecursiveChainOpeningGrantStore grantStore;
    private RecursiveExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        grantStore = new InMemoryRecursiveChainOpeningGrantStore();
        final RecursiveChainOpeningGrantService grantService = new RecursiveChainOpeningGrantService(grantStore);
        controller = new RecursiveChainOpeningGrantController(grantService);
        exceptionHandler = new RecursiveExceptionHandler();
    }

    @Test
    void shouldRegisterGrant() {
        final ResponseEntity<RecursiveChainOpeningGrant> response = controller.registerGrant(validGrant());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getOpeningId()).isEqualTo(OPENING_ID);
        assertThat(response.getBody().getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
        assertThat(response.getBody().getRequesterBpn()).isEqualTo(REQUESTER_BPN);
        assertThat(response.getBody().getAllowedBpnlSet()).containsExactlyInAnyOrder(
                "BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001");
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReturnConflictWhenRegisteringExistingGrant() {
        final ResponseEntity<RecursiveErrorResponse> response =
                exceptionHandler.handleGrantAlreadyExists(
                        new RecursiveChainOpeningGrantAlreadyExistsException("test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.CHAIN_OPENING_GRANT_ALREADY_EXISTS);
    }

    @Test
    void shouldReturnBadRequestForUnknownUseCase() {
        final ResponseEntity<RecursiveErrorResponse> response =
                exceptionHandler.handleInvalidRequest(new IllegalArgumentException("internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.INVALID_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid recursive request.");
    }

    @Test
    void shouldListOnlyValidGrantsByDefault() {
        grantStore.store(validGrant());
        grantStore.store(validGrant().toBuilder()
                .useCase(USE_CASE)
                .requesterBpn("BPNL0000EXPR0001")
                .validFrom(ZonedDateTime.now().minusHours(4))
                .validTo(ZonedDateTime.now().minusHours(1))
                .build());

        final ResponseEntity<List<RecursiveChainOpeningGrant>> response =
                controller.listGrants(OPENING_ID, null, null, null, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUseCase()).isEqualTo(USE_CASE);
    }

    @Test
    void shouldListGrantsForOneChainOpening() {
        grantStore.store(validGrant());
        grantStore.store(validGrant().toBuilder()
                .openingId("other-opening")
                .build());

        final ResponseEntity<List<RecursiveChainOpeningGrant>> response =
                controller.listGrants(OPENING_ID, null, null, null, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getOpeningId()).isEqualTo(OPENING_ID);
    }

    @Test
    void shouldReturnSingleGrant() {
        grantStore.store(validGrant());

        final ResponseEntity<List<RecursiveChainOpeningGrant>> response =
                controller.listGrants(OPENING_ID, GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getOpeningId()).isEqualTo(OPENING_ID);
    }

    @Test
    void shouldReplaceGrant() {
        final RecursiveChainOpeningGrant replacement = validGrant().toBuilder()
                .allowedBpnlSet(Set.of("BPNL0000BELF0001"))
                .build();

        final ResponseEntity<RecursiveChainOpeningGrant> response =
                controller.replaceGrant(replacement);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAllowedBpnlSet()).isEqualTo(Set.of("BPNL0000BELF0001"));
        assertThat(response.getBody().getCreatedAt()).isNotNull();
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
        assertThat(grantStore.find(RecursiveChainOpeningGrantKey.of(replacement)))
                .get()
                .extracting(RecursiveChainOpeningGrant::getAllowedBpnlSet)
                .isEqualTo(Set.of("BPNL0000BELF0001"));
    }

    @Test
    void shouldDeleteGrant() {
        grantStore.store(validGrant());

        final ResponseEntity<Void> response =
                controller.deleteGrant(OPENING_ID, GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(grantStore.find(RecursiveChainOpeningGrantKey.of(validGrant()))).isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingMissingGrant() {
        final RecursiveChainOpeningGrantNotFoundException exception = catchThrowableOfType(
                () -> controller.deleteGrant("missing", GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE),
                RecursiveChainOpeningGrantNotFoundException.class);
        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleGrantNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.CHAIN_OPENING_GRANT_NOT_FOUND);
    }

    @Test
    void shouldReturn403WhenGrantValidationFails() {
        final ResponseEntity<RecursiveErrorResponse> response =
                exceptionHandler.handleGrantValidation(new RecursiveChainOpeningGrantInactiveException("test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo(RecursiveErrorCode.CHAIN_OPENING_GRANT_REJECTED);
    }

    private RecursiveChainOpeningGrant validGrant() {
        return RecursiveChainOpeningGrant.builder()
                .openingId(OPENING_ID)
                .useCase(USE_CASE)
                .requesterBpn(REQUESTER_BPN)
                .globalAssetId(GLOBAL_ASSET_ID)
                .allowedBpnlSet(Set.of("BPNL0000BELF0001", "BPNL0000CERS0001", "BPNL0000DLTA0001"))
                .validFrom(ZonedDateTime.now().minusHours(1))
                .validTo(ZonedDateTime.now().plusHours(12))
                .build();
    }
}
