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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @Builder
    public record CreatePolicyRequest(OffsetDateTime validUntil, String businessPartnerNumber, JsonNode payload) {
    }

    @Builder
    public record CreatePoliciesResponse(String policyId) {
    }

    @Builder
    public record UpdatePolicyRequest(OffsetDateTime validUntil, List<String> businessPartnerNumbers,
                                      List<String> policyIds) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BpnToPolicyId {
        private String bpn;
        private String policyId;
    }

    @DataTableType
    public BpnToPolicyId bpnToPolicyEntryTransformer(final Map<String, String> row) {
        return new BpnToPolicyId(row.get("BPN"), row.get("policyId"));
    }

    public static HashMap<String, HashSet<String>> getExpectedBpnToPolicyIdsMapping(
            final List<BpnToPolicyId> bpnToPolicyIdTable) {

        final HashMap<String, HashSet<String>> expectedBpnToPolicyIdsMapping = new HashMap<>();
        bpnToPolicyIdTable.forEach(entry -> {

            HashSet<String> policyIds = expectedBpnToPolicyIdsMapping.get(entry.getBpn());
            if (policyIds == null) {
                policyIds = new HashSet<>();
            }

            policyIds.add(entry.getPolicyId());

            expectedBpnToPolicyIdsMapping.put(entry.getBpn(), policyIds);
        });
        return expectedBpnToPolicyIdsMapping;
    }

    public static Stream<String> extractPolicyIdsForBpn(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String bpn) {
        return extractPolicyIds(bpnToPoliciesMap.get(bpn).stream());
    }

    @SuppressWarnings("rawtypes")
    public static Stream<LinkedHashMap> extractPoliciesForBpn(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String bpn) {
        return extractPolicy(bpnToPoliciesMap.get(bpn).stream());
    }

    public static Stream<String> extractPolicyIdsStartingWith(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String policyIdPrefix) {
        return extractPolicyIds(bpnToPoliciesMap).filter(policyId -> StringUtils.startsWith(policyId, policyIdPrefix));
    }

    public static Stream<String> extractPolicyIds(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap) {
        return extractPolicyIds(bpnToPoliciesMap.values().stream().flatMap(Collection::stream));
    }

    private static Stream<String> extractPolicyIds(final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return extractPolicy(linkedHashMapStream).map(v -> (String) v.get("policyId"));
    }

    @SuppressWarnings("rawtypes")
    private static Stream<LinkedHashMap> extractPolicy(final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return extractPolicyPayloads(linkedHashMapStream).map(v -> (LinkedHashMap) v.get("policy"));
    }

    @SuppressWarnings("rawtypes")
    private static Stream<LinkedHashMap> extractPolicyPayloads(
            final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return linkedHashMapStream.map(v -> (LinkedHashMap) v.get("payload"));
    }

    public static JsonNode jsonFromString(final ObjectMapper objectMapper, final String jsonObjectStr)
            throws JsonProcessingException {
        return objectMapper.readTree(jsonObjectStr);
    }
}
