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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.tractusx.irs.edc.client.policy.Policy;

/**
 * Helper methods for policies.
 */
public final class PolicyHelper {

    private PolicyHelper() {
    }

    public static List<String> findBpnsByPolicyId(final Map<String, List<Policy>> policyMap, final String policyId) {
        return policyMap.entrySet().stream().filter(mapEntriesByPolicyId(policyId)).map(Map.Entry::getKey).toList();
    }

    private static Predicate<Map.Entry<String, List<Policy>>> mapEntriesByPolicyId(final String policyId) {
        return entry -> entry.getValue().stream().anyMatch(havingPolicyId(policyId));
    }

    public static Predicate<Policy> havingPolicyId(final String policyId) {
        return policy -> policy.getPolicyId().equals(policyId);
    }

}
