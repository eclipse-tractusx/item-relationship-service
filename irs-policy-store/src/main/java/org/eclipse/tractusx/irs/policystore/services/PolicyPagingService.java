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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.models.PolicyWithBpn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Paging helper service for policies.
 */
@Service
@Slf4j
public class PolicyPagingService {

    /**
     * Finds policies by list of BPN. Results are returned as pages.
     *
     * @param bpnToPoliciesMap map that maps BPN to policies
     * @param pageable         the page request options
     * @return a paged list of policies including BPN
     */
    public Page<PolicyWithBpn> getPolicies(final Map<String, List<Policy>> bpnToPoliciesMap, final Pageable pageable) {

        // TODO (mfischer): #639 implement multi-sort:
        final Comparator<PolicyWithBpn> comparator = getComparator(pageable);

        // TODO (mfischer): #750 implement (multi-)filter
        final Predicate<Policy> filter = policy -> true;

        final List<PolicyWithBpn> policies = bpnToPoliciesMap.entrySet()
                                                             .stream()
                                                             .flatMap(bpnWithPolicies -> bpnWithPolicies.getValue()
                                                                                                        .stream()
                                                                                                        .filter(filter)
                                                                                                        .map(policy -> new PolicyWithBpn(
                                                                                                                bpnWithPolicies.getKey(),
                                                                                                                policy)))
                                                             .sorted(comparator)
                                                             .toList();

        return applyPaging(pageable, policies);
    }

    private PageImpl<PolicyWithBpn> applyPaging(final Pageable pageable, final List<PolicyWithBpn> policies) {
        final int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), policies.size());
        final int end = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), policies.size());
        final List<PolicyWithBpn> pagedPolicies = policies.subList(start, end);

        return new PageImpl<>(pagedPolicies,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()), policies.size());
    }

    private Comparator<PolicyWithBpn> getComparator(final Pageable pageable) {

        Comparator<PolicyWithBpn> comparator = null;

        final List<Sort.Order> sort = pageable.getSort().stream().toList();
        for (final Sort.Order order : sort) {

            Comparator<PolicyWithBpn> fieldComparator;
            final String property = order.getProperty();
            if (property.equals("bpn")) {
                fieldComparator = Comparator.comparing(PolicyWithBpn::bpn);
            } else if (property.equals("validUntil")) {
                fieldComparator = Comparator.comparing(p -> p.policy().getValidUntil());
            } else if (property.equals("policyId")) {
                fieldComparator = Comparator.comparing(p -> p.policy().getPolicyId());
            } else if (property.equals("createdOn")) {
                fieldComparator = Comparator.comparing(p -> p.policy().getCreatedOn());
            } else if (property.equals("action")) {
                fieldComparator = Comparator.comparing(p -> {
                    final List<Permission> permissions = p.policy().getPermissions();
                    return permissions.isEmpty() ? null : permissions.get(0).getAction();
                });
            } else {
                log.warn("Sorting by field '{}' is not supported", order.getProperty());
                throw new IllegalArgumentException("Sorting by this field is not supported");
            }

            if (getSortDirection(pageable, order.getProperty()) == Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }

        }

        if (comparator == null) {
            comparator = Comparator.comparing(PolicyWithBpn::bpn);
        }

        return comparator;
    }

    public Sort.Direction getSortDirection(final Pageable pageable, final String fieldName) {

        if (pageable.getSort().isUnsorted()) {
            return Sort.Direction.ASC;
        }

        final Sort sort = pageable.getSort();
        for (final Sort.Order order : sort) {
            if (order.getProperty().equals(fieldName)) {
                return order.getDirection();
            }
        }

        log.warn("Sort field '{}' not found", fieldName);
        throw new IllegalArgumentException("Property not found");
    }
}
