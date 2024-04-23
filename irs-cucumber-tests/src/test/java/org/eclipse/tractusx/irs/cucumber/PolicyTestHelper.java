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
package org.eclipse.tractusx.irs.cucumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for policy tests.
 */
public class PolicyTestHelper {

    public static final String policyTemplate = """
            {
                "@context": {
                    "odrl": "http://www.w3.org/ns/odrl/2/"
                },
                "@id": "%s",
                "policy": {
                    "odrl:permission": [
                        {
                            "odrl:action": "USE",
                            "odrl:constraint": {
                                "odrl:and": [
                                    {
                                        "odrl:leftOperand": "Membership",
                                        "odrl:operator": {
                                            "@id": "odrl:eq"
                                        },
                                        "odrl:rightOperand": "active"
                                    },
                                    {
                                        "odrl:leftOperand": "PURPOSE",
                                        "odrl:operator": {
                                            "@id": "odrl:eq"
                                        },
                                        "odrl:rightOperand": "ID 3.1 Trace"
                                    }
                                ]
                            }
                        }
                    ]
                }
            }
            """;

    public static Stream<String> extractPolicyIdsForBpn(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String bpn) {
        return extractPolicyIds(bpnToPoliciesMap.get(bpn).stream());
    }

    public static Stream<String> extractPolicyIdsStartingWith(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String policyIdPrefix) {
        return extractPolicyIds(bpnToPoliciesMap).filter(policyId -> StringUtils.startsWith(policyId, policyIdPrefix));
    }

    public static Stream<String> extractPolicyIds(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap) {
        return extractPolicyIds(bpnToPoliciesMap.values().stream().flatMap(Collection::stream));
    }

    @SuppressWarnings("rawtypes")
    private static Stream<String> extractPolicyIds(final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return linkedHashMapStream.map(v -> (LinkedHashMap) v.get("payload"))
                                  .map(v -> (LinkedHashMap) v.get("policy"))
                                  .map(v -> (String) v.get("policyId"));
    }

}
