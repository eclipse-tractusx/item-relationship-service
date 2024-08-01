/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.json.JsonObject;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.ConstraintConstants;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.eclipse.tractusx.irs.policystore.config.DefaultAcceptedPoliciesConfig;
import org.eclipse.tractusx.irs.policystore.controllers.PolicyStoreControllerTest;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.UpdatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.eclipse.tractusx.irs.policystore.testutil.PolicyStoreTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PolicyStoreServiceTest {

    private static final String BPN = "testBpn";
    private static final String REGISTER_POLICY_EXAMPLE_PAYLOAD = PolicyStoreControllerTest.REGISTER_POLICY_EXAMPLE_PAYLOAD;
    private static final String CONFIGURED_DEFAULT_POLICY_ID = "default-policy";

    private final Clock clock = Clock.systemUTC();

    private final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(new ConsoleMonitor());
    private final EdcTransformer edcTransformer = new EdcTransformer(new com.fasterxml.jackson.databind.ObjectMapper(),
            titaniumJsonLd, new TypeTransformerRegistryImpl());

    @Captor
    private ArgumentCaptor<Policy> policyCaptor;

    @Captor
    private ArgumentCaptor<String> bpnCaptor;

    private PolicyStoreService testee;

    @Mock
    private PolicyPersistence persistenceMock;

    @BeforeEach
    void setUp() {
        final DefaultAcceptedPoliciesConfig defaultAcceptedPoliciesConfig = new DefaultAcceptedPoliciesConfig();
        final String defaultPoliciesStr = """
                [{
                    "policyId": "%s",
                    "createdOn": "2024-07-17T16:15:14.12345678Z",
                    "validUntil": "9999-01-01T00:00:00.00000000Z",
                    "permissions": [
                        {
                            "action": "use",
                            "constraint": {
                                "and": [
                                    {
                                        "leftOperand": "https://w3id.org/catenax/policy/FrameworkAgreement",
                                        "operator": {
                                            "@id": "eq"
                                        },
                                        "rightOperand": "traceability:1.0"
                                    },
                                    {
                                        "leftOperand": "https://w3id.org/catenax/policy/UsagePurpose",
                                        "operator": {
                                            "@id": "eq"
                                        },
                                        "rightOperand": "cx.core.industrycore:1"
                                    }
                                ],
                                "or": [
                                    {
                                        "leftOperand": "https://w3id.org/catenax/policy/SomethingElse",
                                        "operator": {
                                            "@id": "eq"
                                        },
                                        "rightOperand": "somethingElse:1.0"
                                    }
                                ]
                            }
                        }
                    ]
                }]
                """.formatted(CONFIGURED_DEFAULT_POLICY_ID);
        defaultAcceptedPoliciesConfig.setAcceptedPolicies(StringMapper.toBase64(defaultPoliciesStr));
        testee = new PolicyStoreService(defaultAcceptedPoliciesConfig, persistenceMock, edcTransformer, clock);
    }

    @Nested
    @DisplayName("registerPolicy")
    class RegisterPolicyTests {

        @Test
        void registerPolicy_whenFallbackPolicyId_shouldReturnHttpStatus400() {
            final JsonObject policy = PolicyStoreTestUtil.toJsonObject(
                    REGISTER_POLICY_EXAMPLE_PAYLOAD.formatted(CONFIGURED_DEFAULT_POLICY_ID));
            assertThatThrownBy(() -> testee.registerPolicy(CreatePolicyRequest.builder()
                                                                              .businessPartnerNumber("BPNL1234567890AB")
                                                                              .validUntil(OffsetDateTime.now(clock)
                                                                                                        .plusYears(2))
                                                                              .payload(policy)
                                                                              .build())).isInstanceOf(
                                                                                                ResponseStatusException.class)
                                                                                        .hasMessageContaining(
                                                                                                "400 BAD_REQUEST")
                                                                                        .hasMessageContaining(
                                                                                                CONFIGURED_DEFAULT_POLICY_ID);
        }

        @Test
        void registerPolicy_withBpnNull_shouldStoreAsDefault() {

            // ARRANGE
            final OffsetDateTime now = OffsetDateTime.now();
            final String policyId = "e917f5f-8dac-49ac-8d10-5b4d254d2b48";
            final JsonObject jsonObject = PolicyStoreTestUtil.toJsonObject(
                    REGISTER_POLICY_EXAMPLE_PAYLOAD.formatted(policyId));

            // ACT
            testee.registerPolicy(new CreatePolicyRequest(now, null, jsonObject));

            // ASSERT
            verify(persistenceMock).save(eq(PolicyStoreService.BPN_DEFAULT), policyCaptor.capture());
            assertThat(policyCaptor.getValue().getPolicyId()).isEqualTo(policyId);
            assertThat(policyCaptor.getValue().getValidUntil()).isEqualTo(now);
            assertThat(policyCaptor.getValue().getPermissions()).hasSize(1);
        }

        @Test
        void registerPolicy_success() {

            // ARRANGE
            final OffsetDateTime now = OffsetDateTime.now();
            final String policyId = "e917f5f-8dac-49ac-8d10-5b4d254d2b48";
            final JsonObject jsonObject = PolicyStoreTestUtil.toJsonObject(
                    REGISTER_POLICY_EXAMPLE_PAYLOAD.formatted(policyId));

            // ACT
            final OffsetDateTime validUntil = now.plusMonths(1);
            final CreatePolicyRequest request = new CreatePolicyRequest(validUntil, "BPNL00000123ABCD", jsonObject);
            testee.registerPolicy(request);
            // it does not make sense to check the return value from the method,
            // because it comes from persistence, which is mocked in this test

            // ASSERT
            verify(persistenceMock).save(eq("BPNL00000123ABCD"), policyCaptor.capture());
            assertThat(policyCaptor.getValue().getPolicyId()).isEqualTo(policyId);
            assertThat(policyCaptor.getValue().getValidUntil()).isEqualTo(validUntil);
            assertThat(policyCaptor.getValue().getPermissions()).isNotEmpty();

        }
    }

    @Nested
    @DisplayName("doRegisterPolicy")
    class DoRegisterPolicyTests {

        @Test
        void doRegisterPolicy_permissionEmpty() {

            // ARRANGE
            final OffsetDateTime now = OffsetDateTime.now(clock);
            final String policyId = randomPolicyId();
            final Policy policy = Policy.builder()
                                        .policyId(policyId)
                                        .createdOn(now)
                                        .validUntil(now.plusMinutes(1))
                                        .permissions(emptyList())
                                        .build();

            // ACT
            testee.doRegisterPolicy(policy, BPN);

            // ASSERT
            verify(persistenceMock).save(eq(BPN), any());
        }

        @Test
        void doRegisterPolicy_permissionsNull() {

            // ARRANGE
            final OffsetDateTime now = OffsetDateTime.now(clock);
            final String policyId = randomPolicyId();
            final Policy policy = Policy.builder()
                                        .policyId(policyId)
                                        .createdOn(now)
                                        .validUntil(now.plusMinutes(1))
                                        .permissions(null)
                                        .build();

            // ACT
            final ThrowableAssert.ThrowingCallable call = () -> testee.doRegisterPolicy(policy, "A");

            // ASSERT
            final AbstractThrowableAssert<?, ? extends Throwable> throwableAssert = assertThatThrownBy(call);
            throwableAssert.isInstanceOf(ResponseStatusException.class)
                           .hasMessageContaining("Missing: odrl:permission")
                           .extracting(e -> (ResponseStatusException) e)
                           .satisfies(e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        void doRegisterPolicy_withFilledPermissionList() {

            // ARRANGE
            final OffsetDateTime now = OffsetDateTime.now(clock);
            final String policyId = randomPolicyId();
            final Policy policy = Policy.builder()
                                        .policyId(policyId)
                                        .createdOn(now)
                                        .validUntil(now.plusMinutes(1))
                                        .permissions(createPermissions())
                                        .build();

            // ACT
            testee.doRegisterPolicy(policy, BPN);

            // ASSERT
            verify(persistenceMock).save(eq(BPN), policyCaptor.capture());

            assertThat(policyCaptor.getValue()).isNotNull();
            List<Permission> permissionList = policyCaptor.getValue().getPermissions();
            assertThat(permissionList).hasSize(2);
            assertThat(permissionList.get(0)).usingRecursiveComparison().isEqualTo(createPermissions().get(0));
            assertThat(permissionList.get(1)).usingRecursiveComparison().isEqualTo(createPermissions().get(1));
        }

        @Test
        void doRegisterPolicy_whenPolicyStoreExceptionOccurs() {

            // ACT
            final String policyId = randomPolicyId();
            final OffsetDateTime now = OffsetDateTime.now(clock);
            final Policy policy = Policy.builder()
                                        .policyId(policyId)
                                        .createdOn(now)
                                        .validUntil(now.plusMinutes(1))
                                        .permissions(createPermissions())
                                        .build();

            doThrow(new PolicyStoreException("")).when(persistenceMock).save(any(), any());

            // ASSERT
            assertThatThrownBy(() -> testee.doRegisterPolicy(policy, BPN)).isInstanceOf(ResponseStatusException.class);
        }

        @Test
        void doRegisterPolicy_withMissingConstraintShouldThrowException() {

            // ARRANGE
            final Policy policy = Policy.builder()
                                        .policyId(randomPolicyId())
                                        .permissions(List.of(Permission.builder()
                                                                       .constraint(new Constraints(emptyList(),
                                                                               emptyList()))
                                                                       .build(), Permission.builder().build()))
                                        .build();

            // ACT & ASSERT
            assertThatThrownBy(() -> testee.doRegisterPolicy(policy, null)).isInstanceOf(ResponseStatusException.class)
                                                                           .hasMessageContaining("400 BAD_REQUEST")
                                                                           .hasMessageContaining(
                                                                                   "Missing: odrl:constraint");
        }

    }

    @Nested
    @DisplayName("getStoredPolicies")
    class GetStoredPoliciesTests {

        @Test
        void getStoredPolicies_shouldReturnAllPoliciesStoredForTheBpn() {

            // ARRANGE
            final List<Policy> policies = List.of(createPolicy("test1"), createPolicy("test2"), createPolicy("test3"));
            when(persistenceMock.readAll(BPN)).thenReturn(policies);

            // ACT
            final var storedPolicies = testee.getStoredPolicies(List.of(BPN));

            // ASSERT
            assertThat(storedPolicies).hasSize(3);
        }

        @Test
        void getStoredPolicies_whenNoPoliciesForBpn_shouldReturnTheConfiguredDefaultPolicies() {
            // ACT
            final var defaultPolicies = testee.getStoredPolicies(List.of(BPN));

            // ASSERT
            assertThat(defaultPolicies).hasSize(1);

            final List<Permission> permissionList = defaultPolicies.get(0).getPermissions();
            assertThat(permissionList).hasSize(1);

            final Constraints constraints = permissionList.get(0).getConstraint();
            assertThat(constraints.getOr()).hasSize(1);
            assertThat(constraints.getOr().get(0).getLeftOperand()).isEqualTo(
                    "https://w3id.org/catenax/policy/SomethingElse");

            assertThat(constraints.getAnd()).hasSize(2);
            assertThat(constraints.getAnd().stream().map(Constraint::getLeftOperand)).containsExactlyInAnyOrder(
                    "https://w3id.org/catenax/policy/FrameworkAgreement",
                    "https://w3id.org/catenax/policy/UsagePurpose");
        }
    }

    @Nested
    @DisplayName("getAcceptedPolicies")
    class GetAcceptedPoliciesTests {

        @Nested
        @DisplayName("when BPN is null")
        class WhenBpnIsNull {

            @Test
            @DisplayName("and there are **no** custom default policies in the database")
            void getAcceptedPolicies_whenBpnIsNull_andThereAreNoCustomDefaultPoliciesInDb() {

                // ACT
                final var acceptedPolicies = testee.getAcceptedPolicies(null);

                // ASSERT
                assertThat(acceptedPolicies).as("""
                                                    Should return the configured default policy \
                                                    (because no custom default policies have been added \
                                                    via the Policy Store API)""")
                                            .extracting(policy -> policy.policy().getPolicyId())
                                            .containsExactly(CONFIGURED_DEFAULT_POLICY_ID);
            }

            @Test
            @DisplayName("and there are custom default policies in the database")
            void getAcceptedPolicies_whenBpnIsNull_andThereAreCustomDefaultPoliciesInDb() {

                // ARRANGE
                final String customDefaultPolicy1 = "custom-default-policy-1";
                final String customDefaultPolicy2 = "custom-default-policy-2";
                when(persistenceMock.readAll()).thenReturn(Map.of(PolicyStoreService.BPN_DEFAULT,
                        List.of(createPolicy(customDefaultPolicy1), createPolicy(customDefaultPolicy2))));

                // ACT
                final var acceptedPolicies = testee.getAcceptedPolicies(null);

                // ASSERT
                assertThat(acceptedPolicies).as("""
                                                    Should return the custom default policies \
                                                    that have been added via the Policy Store API \
                                                    instead of the configured default policy \
                                                    (which is only used as fallback).""")
                                            .extracting(policy -> policy.policy().getPolicyId())
                                            .containsExactlyInAnyOrder(customDefaultPolicy1, customDefaultPolicy2);
            }
        }

        @Nested
        @DisplayName("when BPN has no policies")
        class WhenBpnHasNoPolicies {

            @Test
            @DisplayName("and there are **no** custom default policies in the database")
            void getAcceptedPolicies_whenBpnHasNoPolicies_andThereAreNoCustomDefaultPoliciesInDb() {

                // ARRANGE
                when(persistenceMock.readAll(BPN)).thenReturn(emptyList());

                // ACT
                final var acceptedPolicies = testee.getAcceptedPolicies(BPN);

                // ASSERT
                final List<String> policyIds = acceptedPolicies.stream()
                                                               .map(AcceptedPolicy::policy)
                                                               .map(Policy::getPolicyId)
                                                               .toList();
                assertThat(policyIds).containsExactly(CONFIGURED_DEFAULT_POLICY_ID);
            }

            @Test
            @DisplayName("and there are custom default policies in the database")
            void getAcceptedPolicies_whenBpnHasNoPolicies_andThereAreCustomDefaultPoliciesInDb() {

                // ARRANGE
                when(persistenceMock.readAll(BPN)).thenReturn(emptyList());

                // policy registered without BPN should be used as default policy (see #199)
                // this overrides the configured default policy
                final String defaultPolicyId1 = "registered-default-policy-1";
                final String defaultPolicyId2 = "registered-default-policy-2";
                when(persistenceMock.readAll(PolicyStoreService.BPN_DEFAULT)).thenReturn(List.of(
                        // default policy 1
                        createPolicy(defaultPolicyId1),
                        // default policy 2
                        createPolicy(defaultPolicyId2)));

                // ACT
                final var acceptedPolicies = testee.getAcceptedPolicies(BPN);

                // ASSERT
                final List<String> policyIds = acceptedPolicies.stream()
                                                               .map(AcceptedPolicy::policy)
                                                               .map(Policy::getPolicyId)
                                                               .toList();
                assertThat(policyIds).containsExactlyInAnyOrder(defaultPolicyId1, defaultPolicyId2);
                assertThat(acceptedPolicies).as("""
                                                    Should return the configured default policies \
                                                    (because no custom default policies have been added \
                                                    via the Policy Store API)""")
                                            .hasSize(2)
                                            .extracting(policy -> policy.policy().getPolicyId())
                                            .containsExactly(defaultPolicyId1, defaultPolicyId2);
            }
        }

    }

    private Policy createPolicy(final String policyId) {
        return Policy.builder()
                     .policyId(policyId)
                     .createdOn(OffsetDateTime.now(clock))
                     .validUntil(OffsetDateTime.now(clock).plusDays(1))
                     .permissions(emptyList())
                     .build();
    }

    private List<Permission> createPermissions() {
        return List.of(new Permission(PolicyType.USE, createConstraints()),
                new Permission(PolicyType.ACCESS, createConstraints()));
    }

    private Constraints createConstraints() {
        return new Constraints(Collections.emptyList(), List.of(ConstraintConstants.ACTIVE_MEMBERSHIP,
                ConstraintConstants.FRAMEWORK_AGREEMENT_TRACEABILITY_ACTIVE, ConstraintConstants.PURPOSE_ID_3_1_TRACE));
    }

    @Nested
    class DeletePolicyTests {

        @Test
        void deletePolicyForEachBpn_success() {
            // ACT
            final String policyId = UUID.randomUUID().toString();
            testee.deletePolicyForEachBpn(policyId, List.of("BPN1", "BPN2"));

            // ASSERT
            verify(persistenceMock).delete("BPN1", policyId);
            verify(persistenceMock).delete("BPN2", policyId);
        }

        @Test
        void deletePolicy_deleteSuccessful() {
            // ARRANGE
            final String policyId = randomPolicyId();
            when(persistenceMock.readAll()).thenReturn(new HashMap<>(Map.of(BPN, List.of(Policy.builder()
                                                                                               .policyId(policyId)
                                                                                               .createdOn(null)
                                                                                               .validUntil(null)
                                                                                               .permissions(null)
                                                                                               .build()))));

            // ACT
            testee.deletePolicy(policyId);

            // ASSERT
            verify(persistenceMock).delete(BPN, policyId);
        }

        @Test
        void deletePolicy_exceptionFromPolicyPersistence_shouldReturnHttpStatus500() {

            // ACT
            final String policyId = randomPolicyId();
            when(persistenceMock.readAll()).thenReturn(new HashMap<>(Map.of(BPN, List.of(Policy.builder()
                                                                                               .policyId(policyId)
                                                                                               .createdOn(null)
                                                                                               .validUntil(null)
                                                                                               .permissions(null)
                                                                                               .build()))));
            doThrow(new PolicyStoreException("")).when(persistenceMock).delete(BPN, policyId);

            // ASSERT
            assertThatThrownBy(() -> testee.deletePolicy(policyId)).isInstanceOf(ResponseStatusException.class)
                                                                   .hasMessageContaining("500 INTERNAL_SERVER_ERROR");
        }

        @Test
        void deletePolicy_whenPolicyNotFound_shouldReturnHttpStatus404() {

            // ACT
            final String notExistingPolicyId = randomPolicyId();
            final String policyId = randomPolicyId();
            when(persistenceMock.readAll()).thenReturn(
                    new HashMap<>(Map.of(BPN, List.of(Policy.builder().policyId(policyId).build()))));

            // ASSERT
            assertThatThrownBy(() -> testee.deletePolicy(notExistingPolicyId)).isInstanceOf(
                                                                                      ResponseStatusException.class)
                                                                              .hasMessageContaining("404 NOT_FOUND")
                                                                              .hasMessageContaining(
                                                                                      notExistingPolicyId);
        }

        @Test
        void deletePolicy_whenFallbackPolicy_shouldReturnHttpStatus400() {
            assertThatThrownBy(() -> testee.deletePolicy(CONFIGURED_DEFAULT_POLICY_ID)).isInstanceOf(
                                                                                               ResponseStatusException.class)
                                                                                       .hasMessageContaining(
                                                                                               "400 BAD_REQUEST")
                                                                                       .hasMessageContaining(
                                                                                               CONFIGURED_DEFAULT_POLICY_ID);
        }

        @Test
        void deletePolicyForEachBpn_whenFallbackPolicy_shouldReturnHttpStatus400() {
            assertThatThrownBy(() -> testee.deletePolicyForEachBpn(CONFIGURED_DEFAULT_POLICY_ID,
                    List.of("BPNL1234567890AB", "BPNL1234567890CD"))).isInstanceOf(ResponseStatusException.class)
                                                                     .hasMessageContaining("400 BAD_REQUEST")
                                                                     .hasMessageContaining(
                                                                             CONFIGURED_DEFAULT_POLICY_ID);
        }

    }

    @Nested
    class UpdatePoliciesTests {

        @Test
        void updatePolicies_whenFallbackPolicy_shouldReturnHttpStatus400() {
            assertThatThrownBy(() -> testee.updatePolicies(
                    new UpdatePolicyRequest(OffsetDateTime.now(clock), List.of("BPNL1234567890AB", "BPNL1234567890CD"),
                            List.of(CONFIGURED_DEFAULT_POLICY_ID)))).isInstanceOf(ResponseStatusException.class)
                                                                    .hasMessageContaining("400 BAD_REQUEST")
                                                                    .hasMessageContaining(CONFIGURED_DEFAULT_POLICY_ID);
        }

        @Test
        void updatePolicy_whenFallbackPolicy_shouldReturnHttpStatus400() {
            assertThatThrownBy(() -> testee.updatePolicy(CONFIGURED_DEFAULT_POLICY_ID, OffsetDateTime.now(clock),
                    List.of("BPNL1234567890AB", "BPNL1234567890CD"))).isInstanceOf(ResponseStatusException.class)
                                                                     .hasMessageContaining("400 BAD_REQUEST")
                                                                     .hasMessageContaining(
                                                                             CONFIGURED_DEFAULT_POLICY_ID);
        }

        @Test
        void updatePolicies_shouldUpdateBpnAndValidUntil() {
            // ARRANGE
            final String policyId = "testId";

            final String originalBpn = "bpn2";
            final String expectedBpn = "bpn1";

            final OffsetDateTime createdOn = OffsetDateTime.now(clock).minusDays(10);
            final OffsetDateTime originalValidUntil = createdOn.plusMinutes(1);
            final OffsetDateTime expectedValidUntil = createdOn.plusDays(3);

            final List<Permission> permissions = emptyList();

            final Policy testPolicy = Policy.builder()
                                            .policyId(policyId)
                                            .createdOn(createdOn)
                                            .validUntil(originalValidUntil)
                                            .permissions(permissions)
                                            .build();
            when(persistenceMock.readAll()).thenReturn(new HashMap<>(Map.of(originalBpn, List.of(testPolicy))));
            // get policies for bpn
            when(persistenceMock.readAll(originalBpn)).thenReturn(List.of(testPolicy));

            // ACT
            testee.updatePolicies(new UpdatePolicyRequest(expectedValidUntil, List.of(expectedBpn), List.of(policyId)));

            // ASSERT
            verify(persistenceMock).delete(originalBpn, policyId);

            verify(persistenceMock).save(eq(expectedBpn), policyCaptor.capture());
            assertThat(policyCaptor.getValue().getCreatedOn()).isEqualTo(createdOn);
            assertThat(policyCaptor.getValue().getValidUntil()).isEqualTo(expectedValidUntil);
        }

        @Test
        void updatePolicies_shouldAddPolicyToEachBpn() {

            // ARRANGE
            final String policyId = "testId";

            final OffsetDateTime createdOn = OffsetDateTime.now(clock).minusDays(10);
            final OffsetDateTime validUntil = createdOn.plusDays(14);

            final List<Permission> permissions = emptyList();

            // BPN1 without any policies

            // BPN2 with testPolicy
            final Policy testPolicy = Policy.builder()
                                            .policyId(policyId)
                                            .createdOn(createdOn)
                                            .validUntil(validUntil)
                                            .permissions(permissions)
                                            .build();
            when(persistenceMock.readAll()).thenReturn(new HashMap<>(Map.of("bpn2", List.of(testPolicy))));
            when(persistenceMock.readAll("bpn2")).thenReturn(List.of(testPolicy));

            // ACT
            testee.updatePolicies(new UpdatePolicyRequest(validUntil, List.of("bpn1", "bpn2"), List.of(policyId)));

            // ASSERT
            verify(persistenceMock).delete("bpn2", policyId);

            verify(persistenceMock, times(2)).save(bpnCaptor.capture(), policyCaptor.capture());

            // policy added to each BPN
            assertThat(policyCaptor.getAllValues().get(0).getPolicyId()).isEqualTo(policyId);
            assertThat(bpnCaptor.getAllValues().get(0)).isEqualTo("bpn1");
            assertThat(policyCaptor.getAllValues().get(1).getPolicyId()).isEqualTo(policyId);
            assertThat(bpnCaptor.getAllValues().get(1)).isEqualTo("bpn2");
        }

        @Test
        void updatePolicies_shouldAssociateEachGivenPolicyWithEachGivenBpn() {

            // ARRANGE
            final String policyId1 = "testId1";
            final String policyId2 = "testId2";

            final String bpn1 = "bpn1";
            final String bpn2 = "bpn2";

            final OffsetDateTime createdOn = OffsetDateTime.now(clock).minusDays(10);
            final OffsetDateTime originalValidUntil = createdOn.plusMinutes(1);

            final List<Permission> permissions = emptyList();

            final Policy testPolicy1 = Policy.builder()
                                             .policyId(policyId1)
                                             .createdOn(createdOn)
                                             .validUntil(originalValidUntil)
                                             .permissions(permissions)
                                             .build();
            final Policy testPolicy2 = Policy.builder()
                                             .policyId(policyId2)
                                             .createdOn(createdOn)
                                             .validUntil(originalValidUntil)
                                             .permissions(permissions)
                                             .build();

            // BPN1 without any policies

            // BPN2 with testPolicy1 and testPolicy2
            when(persistenceMock.readAll()).thenReturn(new HashMap<>(Map.of(bpn2, List.of(testPolicy1, testPolicy2))));
            when(persistenceMock.readAll(bpn2)).thenReturn(List.of(Policy.builder()
                                                                         .policyId(policyId1)
                                                                         .createdOn(createdOn)
                                                                         .validUntil(originalValidUntil)
                                                                         .permissions(permissions)
                                                                         .build(), Policy.builder()
                                                                                         .policyId(policyId2)
                                                                                         .createdOn(createdOn)
                                                                                         .validUntil(originalValidUntil)
                                                                                         .permissions(permissions)
                                                                                         .build()));

            // ACT
            testee.updatePolicies(
                    new UpdatePolicyRequest(originalValidUntil, List.of(bpn1, bpn2), List.of(policyId1, policyId2)));

            // ASSERT
            verify(persistenceMock).delete(bpn2, policyId1);
            verify(persistenceMock).delete(bpn2, policyId2);

            verify(persistenceMock, times(4)).save(bpnCaptor.capture(), policyCaptor.capture());

            // each BPNs added to policy 1
            assertThat(policyCaptor.getAllValues().get(0).getPolicyId()).isEqualTo(policyId1);
            assertThat(bpnCaptor.getAllValues().get(0)).isEqualTo("bpn1");
            assertThat(policyCaptor.getAllValues().get(1).getPolicyId()).isEqualTo(policyId1);
            assertThat(bpnCaptor.getAllValues().get(1)).isEqualTo("bpn2");

            // each BPNs added to policy 2
            assertThat(policyCaptor.getAllValues().get(2).getPolicyId()).isEqualTo(policyId2);
            assertThat(bpnCaptor.getAllValues().get(2)).isEqualTo("bpn1");
            assertThat(policyCaptor.getAllValues().get(3).getPolicyId()).isEqualTo(policyId2);
            assertThat(bpnCaptor.getAllValues().get(3)).isEqualTo("bpn2");
        }

        @Test
        void updatePolicies_exceptionFromPolicyPersistence_shouldReturnHttpStatus500() {

            // ARRANGE
            final String policyId = "testId";
            final OffsetDateTime validUntil = OffsetDateTime.now();
            doThrow(new PolicyStoreException("")).when(persistenceMock).readAll();

            // ACT
            final List<String> bpn = List.of("bpn");
            final ThrowableAssert.ThrowingCallable call = () -> testee.updatePolicy(policyId, validUntil, bpn);

            // ASSERT
            assertThatThrownBy(call).isInstanceOf(ResponseStatusException.class) //
                                    .extracting(e -> (ResponseStatusException) e)
                                    .satisfies(e -> assertThat(e.getStatusCode()).isEqualTo(
                                            HttpStatus.INTERNAL_SERVER_ERROR));
        }

    }

    private static String randomPolicyId() {
        return UUID.randomUUID().toString();
    }
}