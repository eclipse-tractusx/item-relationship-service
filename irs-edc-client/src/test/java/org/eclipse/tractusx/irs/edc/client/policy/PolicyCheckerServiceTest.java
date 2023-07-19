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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyCheckerServiceTest {

    private PolicyCheckerService policyCheckerService;
    @Mock
    private AcceptedPoliciesProvider policyStore;

    private static Policy createPolicy(final String leftExpr, final String rightExpr) {
        final AtomicConstraint atomicConstraint = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression(leftExpr))
                                                                          .rightExpression(
                                                                                  new LiteralExpression(rightExpr))
                                                                          .operator(Operator.EQ)
                                                                          .build();
        final Permission permission = Permission.Builder.newInstance()
                                                        .action(Action.Builder.newInstance().type("USE").build())
                                                        .constraint(atomicConstraint)
                                                        .build();
        return Policy.Builder.newInstance().permission(permission).build();
    }

    @BeforeEach
    void setUp() {
        final var policyList = List.of(new AcceptedPolicy("ID 3.0 Trace", OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy("FrameworkAgreement.traceability", OffsetDateTime.now().plusYears(1)));
        when(policyStore.getAcceptedPolicies()).thenReturn(policyList);
        policyCheckerService = new PolicyCheckerService(policyStore);
    }

    @ParameterizedTest
    @CsvSource(value = { "idsc:PURPOSE,ID 3.0 Trace",
                         "idsc:PURPOSE,ID%203.0%20Trace",
                         "FrameworkAgreement.traceability,active"
    }, delimiter = ',')
    void shouldConfirmValidPolicy(final String leftExpr, final String rightExpr) {
        // given
        Policy policy = createPolicy(leftExpr, rightExpr);
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWrongPolicy() {
        // given
        Policy policy = createPolicy("idsc:PURPOSE", "Wrong_Trace");
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectWhenPolicyStoreIsEmpty() {
        // given
        Policy policy = createPolicy("idsc:PURPOSE", "ID 3.0 Trace");
        when(policyStore.getAcceptedPolicies()).thenReturn(List.of());
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

}