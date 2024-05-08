/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.policystore.validators;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Operator;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.junit.jupiter.api.Test;

class PolicyValidatorTest {

    @Test
    void invalidPolicyId() {
        final Policy policy = Policy.builder().policyId("_invalid_policy_id_").permissions(createPermissions()).build();
        assertThatThrownBy(() -> PolicyValidator.validate(policy)).isInstanceOf(ConstraintViolationException.class)
                                                                  .hasMessageContaining("policyId")
                                                                  .hasMessageContaining("must be a valid UUID");
    }

    private List<Permission> createPermissions() {
        return List.of(Permission.builder().action(PolicyType.USE).constraint(createConstraints()).build(),
                Permission.builder().action(PolicyType.ACCESS).constraint(createConstraints()).build());
    }

    private Constraints createConstraints() {
        return new Constraints(Collections.emptyList(),
                List.of(new Constraint("Membership", new Operator(OperatorType.EQ), "active"),
                        new Constraint("FrameworkAgreement.traceability", new Operator(OperatorType.EQ), "active"),
                        new Constraint("PURPOSE", new Operator(OperatorType.EQ), "ID 3.1 Trace")));
    }

}