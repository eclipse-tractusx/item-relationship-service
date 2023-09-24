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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAndConstraintPolicy;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createAtomicConstraint;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.edc.policy.model.Policy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = PolicyCheckerService.class)
class PolicyCheckerSpringBootTest {

    @MockBean
    private AcceptedPoliciesProvider.DefaultAcceptedPoliciesProvider policiesProvider;

    @Autowired
    private PolicyCheckerService policyCheckerService;

    @Test
    void shouldValidateWithDefaultOperands() {
        // given
        final var policyList = List.of(
                new AcceptedPolicy(policy("FrameworkAgreement.traceability"), OffsetDateTime.now().plusYears(1)),
                new AcceptedPolicy(policy("ID 3.1 Trace"), OffsetDateTime.now().plusYears(1)));
        when(policiesProvider.getAcceptedPolicies()).thenReturn(policyList);
        Policy policy = createAndConstraintPolicy(
                List.of(createAtomicConstraint("FrameworkAgreement.traceability", "active"),
                        createAtomicConstraint("PURPOSE", "ID 3.1 Trace")));
        // when
        boolean result = policyCheckerService.isValid(policy);

        // then
        assertThat(result).isTrue();
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(String policyId) {
        return new org.eclipse.tractusx.irs.edc.client.policy.Policy(
                policyId,
                OffsetDateTime.now().plusYears(1),
                OffsetDateTime.now().plusYears(1),
                Collections.emptyList()
        );
    }

}
