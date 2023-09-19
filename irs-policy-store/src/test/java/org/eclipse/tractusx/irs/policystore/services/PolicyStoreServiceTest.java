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
package org.eclipse.tractusx.irs.policystore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PolicyStoreServiceTest {

    private static final String BPN = "testBpn";
    private PolicyStoreService testee;

    @Mock
    private PolicyPersistence persistence;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        testee = new PolicyStoreService(BPN, List.of(), persistence, clock);
    }

    @Test
    void registerPolicy() {
        // arrange
        final var req = new CreatePolicyRequest("testId", OffsetDateTime.now(clock).plusMinutes(1));

        // act
        testee.registerPolicy(req);

        // assert
        verify(persistence).save(eq(BPN), any());
    }

    @Test
    void registerPolicyShouldThrowResponseStatusException() {
        // act
        final String policyId = "testId";
        doThrow(new PolicyStoreException("")).when(persistence).save(eq(BPN), any());
        final CreatePolicyRequest request = new CreatePolicyRequest(policyId, OffsetDateTime.now());

        // assert
        assertThrows(ResponseStatusException.class, () -> testee.registerPolicy(request));
    }

    @Test
    void getStoredPolicies() {
        // arrange
        final List<Policy> policies = List.of(createPolicy("test1"), createPolicy("test2"), createPolicy("test3"));
        when(persistence.readAll(BPN)).thenReturn(policies);

        // act
        final var storedPolicies = testee.getStoredPolicies();

        // assert
        assertThat(storedPolicies).hasSize(3);
    }

    private Policy createPolicy(final String policyId) {
        return new Policy(policyId, OffsetDateTime.now(clock), OffsetDateTime.now(clock).plusDays(1));
    }

    @Test
    void deletePolicy() {
        // act
        testee.deletePolicy("testId");

        // assert
        verify(persistence).delete(BPN, "testId");
    }

    @Test
    void deletePolicyShouldThrowResponseStatusException() {
        // act
        final String policyId = "testId";
        doThrow(new PolicyStoreException("")).when(persistence).delete(BPN, policyId);

        // assert
        assertThrows(ResponseStatusException.class, () -> testee.deletePolicy(policyId));
    }

    @Test
    void updatePolicy() {
        // act
        final String policyId = "testId";
        final OffsetDateTime validUntil = OffsetDateTime.now();
        testee.updatePolicy(policyId, new UpdatePolicyRequest(validUntil));

        // assert
        verify(persistence).update(BPN, policyId, validUntil);
    }

    @Test
    void updatePolicyShouldThrowResponseStatusException() {
        // act
        final String policyId = "testId";
        final OffsetDateTime validUntil = OffsetDateTime.now();
        doThrow(new PolicyStoreException("")).when(persistence).update(BPN, policyId, validUntil);
        final UpdatePolicyRequest request = new UpdatePolicyRequest(validUntil);

        // assert
        assertThrows(ResponseStatusException.class, () -> testee.updatePolicy(policyId, request));
    }
}