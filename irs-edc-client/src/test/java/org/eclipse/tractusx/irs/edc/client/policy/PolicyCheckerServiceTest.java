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

import java.util.List;

import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.junit.jupiter.api.Test;

class PolicyCheckerServiceTest {

    private final PolicyCheckerService policyCheckerService = new PolicyCheckerService(List.of("ID 3.0 Trace"));

    @Test
    void shouldConfirmValidPolicy() {
        // given
        Policy policy = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance()
                                                                    .action(Action.Builder.newInstance()
                                                                                          .type("USE")
                                                                                          .build())
                                                                    .constraint(AtomicConstraint.Builder.newInstance()
                                                                                                        .leftExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "idsc:PURPOSE"))
                                                                                                        .rightExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "ID 3.0 Trace"))

                                                                                                        .operator(
                                                                                                                Operator.EQ)
                                                                                                        .build())
                                                                    .build())
                                      .build();
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectWrongPolicy() {
        // given
        Policy policy = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance()
                                                                    .action(Action.Builder.newInstance()
                                                                                          .type("USE")
                                                                                          .build())
                                                                    .constraint(AtomicConstraint.Builder.newInstance()
                                                                                                        .leftExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "idsc:PURPOSE"))
                                                                                                        .rightExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "Wrong_Trace"))

                                                                                                        .operator(
                                                                                                                Operator.EQ)
                                                                                                        .build())
                                                                    .build())
                                      .build();
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void shouldConfirmValidPolicyEvenEncodingVersion() {
        // given
        Policy policy = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance()
                                                                    .action(Action.Builder.newInstance()
                                                                                          .type("USE")
                                                                                          .build())
                                                                    .constraint(AtomicConstraint.Builder.newInstance()
                                                                                                        .leftExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "idsc:PURPOSE"))
                                                                                                        .rightExpression(
                                                                                                                new LiteralExpression(
                                                                                                                        "ID%203.0%20Trace"))

                                                                                                        .operator(
                                                                                                                Operator.EQ)
                                                                                                        .build())
                                                                    .build())
                                      .build();
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldValidatePolicyForMultiplePolicies() {
        // given
        final AtomicConstraint idTrace3_0 = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression("idsc:PURPOSE"))
                                                                          .rightExpression(
                                                                                  new LiteralExpression("ID 3.0 Trace"))
                                                                          .operator(Operator.EQ)
                                                                          .build();
        final AtomicConstraint idTrace3_1 = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .rightExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .operator(Operator.EQ)
                                                                          .build();
        final Action use = Action.Builder.newInstance().type("USE").build();
        Policy policy3_0 = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance().action(use).constraint(idTrace3_0).build())
                                      .build();
        Policy policy3_1 = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance().action(use).constraint(idTrace3_1).build())
                                      .build();

        // when
        boolean result = policyCheckerService.isValid(List.of(policy3_0, policy3_1));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void shouldFailIfNoPolicyMathces() {
        // given
        final AtomicConstraint idTrace3_0 = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .rightExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .operator(Operator.EQ)
                                                                          .build();
        final AtomicConstraint invalid = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .rightExpression(
                                                                                  new LiteralExpression("invalid"))
                                                                          .operator(Operator.EQ)
                                                                          .build();
        final Action use = Action.Builder.newInstance().type("USE").build();
        Policy policy3_0 = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance().action(use).constraint(idTrace3_0).build())
                                      .build();
        Policy policy3_1 = Policy.Builder.newInstance()
                                      .permission(Permission.Builder.newInstance().action(use).constraint(invalid).build())
                                      .build();

        // when
        boolean result = policyCheckerService.isValid(List.of(policy3_0, policy3_1));

        // then
        assertThat(result).isFalse();
    }
}