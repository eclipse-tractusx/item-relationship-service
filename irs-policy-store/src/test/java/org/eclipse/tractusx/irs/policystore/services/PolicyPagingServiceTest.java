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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.BETWEEN;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.EQUALS;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.STARTS_WITH;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.irs.edc.client.policy.ConstraintConstants;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.policystore.models.PolicyWithBpn;
import org.eclipse.tractusx.irs.policystore.models.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.ToStringStyle;

@ExtendWith(MockitoExtension.class)
class PolicyPagingServiceTest {

    private static final List<SearchCriteria<?>> NO_SEARCH_CRITERIA = emptyList();

    private PolicyPagingService testee;

    @BeforeEach
    void setUp() {
        testee = new PolicyPagingService();
    }

    private final Map<String, List<Policy>> policiesMap = Map.of( //
            "BPN2", Arrays.asList( //
                    createPolicy("policy-3"), //
                    createPolicy("policy-2"), //
                    createPolicy("policy-5") //
            ), "BPN1", Arrays.asList( //
                    createPolicy("policy-4"), //
                    createPolicy("policy-1") //
            ) //
    );

    @Nested
    class SortTest {

        @Test
        public void whenUnsorted_shouldUseDefaultSortingBpnAscending() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    NO_SEARCH_CRITERIA);

            assertThat(result.getContent().stream().map(p -> p.policy().getPolicyId()).toList()).containsExactly(
                    // BPN1
                    "policy-4", "policy-1",
                    // BPN2
                    "policy-3", "policy-2", "policy-5");
        }

        @Test
        public void whenSortedByBpnAsc() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap,
                    PageRequest.of(0, 10, Sort.by("bpn").ascending()), NO_SEARCH_CRITERIA);

            assertThat(result.getContent().stream().map(p -> p.policy().getPolicyId()).toList()).containsExactly(
                    // BPN1
                    "policy-4", "policy-1",
                    // BPN2
                    "policy-3", "policy-2", "policy-5");
        }

        @Test
        public void whenSortedByBpnDesc() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap,
                    PageRequest.of(0, 10, Sort.by("bpn").descending()), NO_SEARCH_CRITERIA);

            assertThat(result.getContent().stream().map(PolicyWithBpn::bpn).toList()).containsExactly("BPN2", "BPN2",
                    "BPN2", "BPN1", "BPN1");
        }

        @Test
        public void whenSortedByBpnAscAndPolicyIdDesc() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap,
                    PageRequest.of(0, 10, Sort.by("bpn").ascending().and(Sort.by("policyId").descending())),
                    NO_SEARCH_CRITERIA);

            assertThat(result.getContent().stream().map(p -> p.policy().getPolicyId()).toList()).containsExactly(
                    // BPN1
                    "policy-4", "policy-1",
                    // BPN2
                    "policy-5", "policy-3", "policy-2");
        }

        @Test
        public void whenSortedByBpnDescAndPolicyIdAsc() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap,
                    PageRequest.of(0, 10, Sort.by("bpn").descending().and(Sort.by("policyId").ascending())),
                    NO_SEARCH_CRITERIA);

            assertThat(result.getContent().stream().map(p -> p.policy().getPolicyId()).toList()).containsExactly(
                    // BPN2
                    "policy-2", "policy-3", "policy-5",
                    // BPN1
                    "policy-1", "policy-4");
        }
    }

    @Nested
    class PageTests {

        @Test
        public void whenRequestedPageIsAvailable_thenCorrectPageIsReturned() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(2, 2),
                    NO_SEARCH_CRITERIA);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumberOfElements()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        public void whenNoPoliciesAvailable_thenEmptyPage() {

            final Page<PolicyWithBpn> result = testee.getPolicies(emptyMap(), PageRequest.of(0, 10),
                    NO_SEARCH_CRITERIA);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getNumberOfElements()).isEqualTo(0);
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        public void whenPageRequestedBeyondAvailableData_thenReturnEmptyPage() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(2, 10),
                    NO_SEARCH_CRITERIA);

            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumberOfElements()).isEqualTo(0);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    class FilterTests {

        @Test
        public void policyId_invalidOperation() {

            assertThatThrownBy(() -> testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    List.of(new SearchCriteria<>("policyId", BETWEEN, "policy-2")))).isInstanceOf(
                                                                                            IllegalArgumentException.class)
                                                                                    .hasMessageContaining(
                                                                                            "The property 'policyId' only supports the following operations");
        }

        @Test
        public void filterByPolicyIdEquals_shouldFindExactlyOne() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    List.of(new SearchCriteria<>("policyId", EQUALS, "policy-2")));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            final PolicyWithBpn policyWithBpn = result.getContent().get(0);
            assertThat(policyWithBpn.policy().getPolicyId()).isEqualTo("policy-2");
            assertThat(policyWithBpn.bpn()).isEqualTo("BPN2");
        }

        @Test
        public void filterByPolicyIdEqualsAndBpnEquals_noResult() {

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    List.of(new SearchCriteria<>("policyId", EQUALS, "policy-4"),
                            new SearchCriteria<>("bpn", EQUALS, "BPN2")));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        public void filterByPolicyIdStartingWith_shouldFindAllMatches() {

            final Map<String, List<Policy>> policiesMap = Map.of( //
                    "BPN2", Arrays.asList( //
                            createPolicy("policy-3"), //
                            createPolicy("policy-22"), //
                            createPolicy("policy-5") //
                    ), "BPN1", Arrays.asList( //
                            createPolicy("policy-2"), //
                            createPolicy("policy-1") //
                    ) //
            );

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    List.of(new SearchCriteria<>("policyId", STARTS_WITH, "policy-2")));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            final List<String> policies = result.getContent()
                                                .stream()
                                                .map(p -> toStringBuilder(p).append("BPN", p.bpn()) //
                                                                            .append("policyId",
                                                                                    p.policy().getPolicyId())
                                                                            .toString())
                                                .toList();
            assertThat(policies).containsExactlyInAnyOrder("[BPN=BPN1,policyId=policy-2]",
                    "[BPN=BPN2,policyId=policy-22]");
        }

        @Test
        public void filterByPolicyIdStartingWithAndByBpnEquals_shouldNarrowDown() {

            final Map<String, List<Policy>> policiesMap = Map.of( //
                    "BPN2", Arrays.asList( //
                            createPolicy("policy-3"), //
                            createPolicy("policy-22"), //
                            createPolicy("policy-5") //
                    ), "BPN1", Arrays.asList( //
                            createPolicy("policy-2"), //
                            createPolicy("policy-1") //
                    ) //
            );

            final Page<PolicyWithBpn> result = testee.getPolicies(policiesMap, PageRequest.of(0, 10),
                    List.of(new SearchCriteria<>("policyId", STARTS_WITH, "policy-2"),
                            new SearchCriteria<>("BPN", EQUALS, "BPN1")));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            final List<String> policies = result.getContent()
                                                .stream()
                                                .map(p -> toStringBuilder(p).append("BPN", p.bpn()) //
                                                                            .append("policyId",
                                                                                    p.policy().getPolicyId())
                                                                            .toString())
                                                .toList();
            assertThat(policies).containsExactlyInAnyOrder("[BPN=BPN1,policyId=policy-2]");
        }

    }

    private Policy createPolicy(final String policyId) {
        return Policy.builder()
                     .policyId(policyId)
                     .createdOn(OffsetDateTime.now())
                     .validUntil(OffsetDateTime.now())
                     .permissions(createPermissions())
                     .build();
    }

    private List<Permission> createPermissions() {
        return List.of(new Permission(PolicyType.USE, createConstraints()),
                new Permission(PolicyType.ACCESS, createConstraints()));
    }

    private Constraints createConstraints() {
        return new Constraints(emptyList(), List.of(ConstraintConstants.ACTIVE_MEMBERSHIP,
                ConstraintConstants.FRAMEWORK_AGREEMENT_TRACEABILITY_ACTIVE, ConstraintConstants.PURPOSE_ID_3_1_TRACE));
    }

    private ToStringBuilder toStringBuilder(final PolicyWithBpn p) {
        return new ToStringBuilder(p, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}