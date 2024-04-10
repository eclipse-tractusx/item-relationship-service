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
package org.eclipse.tractusx.irs.policystore.services;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.HashMap;
import java.util.List;

import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.junit.jupiter.api.Test;

class PolicyHelperTest {

    @Test
    void findBpnsByPolicyId_shouldFilterMapByPolicyId() {

        // ARRANGE
        final HashMap<String, List<Policy>> policyMap = new HashMap<>();
        final String policyIdToFind = "policyIdToFind";
        policyMap.put("BPN1", List.of(Policy.builder().policyId(policyIdToFind).build(),
                Policy.builder().policyId("policy1").build()));
        policyMap.put("BPN2", List.of(Policy.builder().policyId("policy2").build(),
                Policy.builder().policyId(policyIdToFind).build()));
        policyMap.put("BPN3",
                List.of(Policy.builder().policyId("policy3").build(), Policy.builder().policyId("policy4").build()));
        policyMap.put("BPN4", List.of(Policy.builder().policyId(policyIdToFind).build()));

        // ACT
        final List<String> result = PolicyHelper.findBpnsByPolicyId(policyMap, policyIdToFind);

        // ASSERT
        assertThat(result).containsExactlyInAnyOrder("BPN1", "BPN2", "BPN4");
    }

    @Test
    void havingPolicyId_whenAppliedAsFilterToAListOfPolicies_shouldFilterByPolicyId() {

        // ARRANGE
        final String policyIdToFind = "policyToFind";
        List<Policy> policies = List.of( //
                Policy.builder().policyId("policy1").build(), //
                Policy.builder().policyId(policyIdToFind).build(), //
                Policy.builder().policyId("policy3").build(), //
                Policy.builder().policyId("policy4").build(), //
                Policy.builder().policyId(policyIdToFind).build());

        // ACT
        final List<Policy> result = policies.stream().filter(PolicyHelper.havingPolicyId(policyIdToFind)).toList();

        // ASSERT
        assertThat(result.stream().map(Policy::getPolicyId).toList()).hasSize(2).containsOnly(policyIdToFind);
    }

}
