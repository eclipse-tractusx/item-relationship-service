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

import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_ACTION;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_BPN;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_CREATED_ON;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_POLICY_ID;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_VALID_UNTIL;
import static org.eclipse.tractusx.irs.policystore.common.DateUtils.isDateAfter;
import static org.eclipse.tractusx.irs.policystore.common.DateUtils.isDateBefore;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.AFTER_LOCAL_DATE;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.BEFORE_LOCAL_DATE;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.EQUALS;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.STARTS_WITH;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.models.PolicyWithBpn;
import org.eclipse.tractusx.irs.policystore.models.SearchCriteria;
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
@SuppressWarnings({ "PMD.TooManyStaticImports",
                    "PMD.ExcessiveImports"
})
public class PolicyPagingService {

    /**
     * Finds policies by list of BPN. Results are returned as pages.
     *
     * @param bpnToPoliciesMap map that maps BPN to policies
     * @param pageable         the page request options
     * @param searchCriteria   the search criteria list
     * @return a paged list of policies including BPN
     */
    public Page<PolicyWithBpn> getPolicies(final Map<String, List<Policy>> bpnToPoliciesMap, final Pageable pageable,
            final List<SearchCriteria<?>> searchCriteria) {

        final Comparator<PolicyWithBpn> comparator = new PolicyComparatorBuilder(pageable).build();
        final Predicate<PolicyWithBpn> filter = new PolicyFilterBuilder(searchCriteria).build();
        final List<PolicyWithBpn> policies = getPolicyWithBpnStream(bpnToPoliciesMap).filter(filter)
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

    public List<String> autocomplete(final Map<String, List<Policy>> bpnToPoliciesMap, final String field,
            final String value, final int limit) {

        if (PROPERTY_BPN.equalsIgnoreCase(field)) {
            return bpnToPoliciesMap.keySet().stream().filter(t -> StringUtils.startsWithIgnoreCase(t, value)).toList();
        } else {
            final Function<PolicyWithBpn, String> fieldSelector = getFieldSelector(field);
            final Stream<PolicyWithBpn> policyWithBpnStream = getPolicyWithBpnStream(bpnToPoliciesMap);
            return policyWithBpnStream.map(fieldSelector)
                                      .filter(s -> StringUtils.startsWithIgnoreCase(s, value))
                                      .distinct()
                                      .sorted()
                                      .limit(limit)
                                      .toList();
        }
    }

    private Stream<PolicyWithBpn> getPolicyWithBpnStream(final Map<String, List<Policy>> bpnToPoliciesMap) {
        return bpnToPoliciesMap.entrySet()
                               .stream()
                               .flatMap(bpnWithPolicies -> bpnWithPolicies.getValue()
                                                                          .stream()
                                                                          .map(policy -> new PolicyWithBpn(
                                                                                  bpnWithPolicies.getKey(), policy)));
    }

    private Function<PolicyWithBpn, String> getFieldSelector(final String field) {

        final Function<PolicyWithBpn, String> fieldSelector;

        if (PROPERTY_BPN.equalsIgnoreCase(field)) {
            fieldSelector = PolicyWithBpn::bpn;
        } else if (PROPERTY_POLICY_ID.equalsIgnoreCase(field)) {
            fieldSelector = p -> p.policy().getPolicyId();
        } else if (PROPERTY_CREATED_ON.equalsIgnoreCase(field)) {
            fieldSelector = p -> DateTimeFormatter.ofPattern("yyyy-MM-dd").format(p.policy().getCreatedOn());
        } else if (PROPERTY_VALID_UNTIL.equalsIgnoreCase(field)) {
            fieldSelector = p -> DateTimeFormatter.ofPattern("yyyy-MM-dd").format(p.policy().getValidUntil());
        } else if (PROPERTY_ACTION.equalsIgnoreCase(field)) {
            fieldSelector = p -> {
                final List<Permission> permissions = p.policy().getPermissions();
                return permissions == null || permissions.isEmpty() ? null : permissions.get(0).getAction().getValue();
            };
        } else {
            log.warn("Field '{}' does not support autocomplete", field);
            throw new IllegalArgumentException("Field does not support autocomplete");
        }

        return fieldSelector;
    }

    /**
     * Builder for {@link Comparator} for sorting a list of {@link PolicyWithBpn} objects.
     */
    private static class PolicyComparatorBuilder {

        private final Pageable pageable;

        /* package */ PolicyComparatorBuilder(final Pageable pageable) {
            this.pageable = pageable;
        }

        /* package */ Comparator<PolicyWithBpn> build() {

            Comparator<PolicyWithBpn> comparator = null;

            final List<Sort.Order> sort = pageable.getSort().stream().toList();
            for (final Sort.Order order : sort) {
                if (comparator == null) {
                    comparator = getPolicyComparator(pageable, order);
                } else {
                    comparator = comparator.thenComparing(getPolicyComparator(pageable, order));
                }
            }

            if (comparator == null) {
                comparator = Comparator.comparing(PolicyWithBpn::bpn);
            }

            return comparator;
        }

        @SuppressWarnings({ "PMD.CognitiveComplexity" })
        private Comparator<PolicyWithBpn> getPolicyComparator(final Pageable pageable, final Sort.Order order) {
            Comparator<PolicyWithBpn> fieldComparator;
            final String property = order.getProperty();
            if (PROPERTY_BPN.equalsIgnoreCase(property)) {
                fieldComparator = Comparator.comparing(PolicyWithBpn::bpn);
            } else if (PROPERTY_VALID_UNTIL.equalsIgnoreCase(property)) {
                fieldComparator = Comparator.comparing(p -> p.policy().getValidUntil());
            } else if (PROPERTY_POLICY_ID.equalsIgnoreCase(property)) {
                fieldComparator = Comparator.comparing(p -> p.policy().getPolicyId());
            } else if (PROPERTY_CREATED_ON.equalsIgnoreCase(property)) {
                fieldComparator = Comparator.comparing(p -> p.policy().getCreatedOn());
            } else if (PROPERTY_ACTION.equalsIgnoreCase(property)) {
                fieldComparator = Comparator.comparing(p -> {
                    final List<Permission> permissions = p.policy().getPermissions();
                    if (permissions == null || permissions.isEmpty()) {
                        return null;
                    } else {
                        // we use the action of the first permission in the list for sorting
                        return permissions.get(0).getAction();
                    }
                });
            } else {
                log.warn("Sorting by field '{}' is not supported", order.getProperty());
                throw new IllegalArgumentException("Sorting by this field is not supported");
            }

            if (getSortDirection(pageable, order.getProperty()) == Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            return fieldComparator;
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

    /**
     * Builder for {@link Predicate} for filtering a list of {@link PolicyWithBpn} objects.
     */
    private static class PolicyFilterBuilder {

        public static final String MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS = "The property '%s' only supports the following operations: %s";
        private final List<SearchCriteria<?>> searchCriteriaList;

        /* package */ PolicyFilterBuilder(final List<SearchCriteria<?>> searchCriteriaList) {
            this.searchCriteriaList = searchCriteriaList;
        }

        /* package */ Predicate<PolicyWithBpn> build() {

            Predicate<PolicyWithBpn> policyFilter = policy -> true;
            for (final SearchCriteria<?> searchCriteria : searchCriteriaList) {
                final Predicate<PolicyWithBpn> fieldFilter = getPolicyPredicate(searchCriteria);
                policyFilter = policyFilter.and(fieldFilter);
            }
            return policyFilter;
        }

        private Predicate<PolicyWithBpn> getPolicyPredicate(final SearchCriteria<?> searchCriteria) {
            if (PROPERTY_BPN.equalsIgnoreCase(searchCriteria.getProperty())) {
                return getBpnFilter(searchCriteria);
            } else if (PROPERTY_POLICY_ID.equalsIgnoreCase(searchCriteria.getProperty())) {
                return getPolicyIdFilter(searchCriteria);
            } else if (PROPERTY_ACTION.equalsIgnoreCase(searchCriteria.getProperty())) {
                return getActionFilter(searchCriteria);
            } else if (PROPERTY_CREATED_ON.equalsIgnoreCase(searchCriteria.getProperty())) {
                return getCreatedOnFilter(searchCriteria);
            } else if (PROPERTY_VALID_UNTIL.equalsIgnoreCase(searchCriteria.getProperty())) {
                return getValidUntilFilter(searchCriteria);
            } else {
                throw new IllegalArgumentException("Not supported");
            }
        }

        private Predicate<PolicyWithBpn> getPolicyIdFilter(final SearchCriteria<?> searchCriteria) {
            return switch (searchCriteria.getOperation()) {
                case EQUALS -> p -> p.policy().getPolicyId().equalsIgnoreCase((String) searchCriteria.getValue());
                case STARTS_WITH -> p -> StringUtils.startsWithIgnoreCase(p.policy().getPolicyId(),
                        (String) searchCriteria.getValue());
                default -> throw new IllegalArgumentException(
                        MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS.formatted(searchCriteria.getProperty(),
                                List.of(EQUALS, STARTS_WITH)));
            };
        }

        private Predicate<PolicyWithBpn> getBpnFilter(final SearchCriteria<?> searchCriteria) {
            return switch (searchCriteria.getOperation()) {
                case EQUALS -> p -> p.bpn().equalsIgnoreCase((String) searchCriteria.getValue());
                case STARTS_WITH -> p -> StringUtils.startsWithIgnoreCase(p.bpn(), (String) searchCriteria.getValue());
                default -> throw new IllegalArgumentException(
                        MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS.formatted(searchCriteria.getProperty(),
                                List.of(EQUALS, STARTS_WITH)));
            };
        }

        private Predicate<PolicyWithBpn> getActionFilter(final SearchCriteria<?> searchCriteria) {
            if (EQUALS.equals(searchCriteria.getOperation())) {
                return p -> {
                    final List<Permission> permissions = p.policy().getPermissions();
                    if (permissions == null || permissions.isEmpty()) {
                        return false;
                    } else {
                        // we use the action of the first permission in the list for filtering
                        return permissions.get(0)
                                          .getAction()
                                          .getValue()
                                          .equalsIgnoreCase((String) searchCriteria.getValue());
                    }
                };
            } else {
                throw new IllegalArgumentException(
                        MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS.formatted(searchCriteria.getProperty(),
                                List.of(EQUALS)));
            }
        }

        private Predicate<PolicyWithBpn> getCreatedOnFilter(final SearchCriteria<?> searchCriteria) {
            return switch (searchCriteria.getOperation()) {
                case BEFORE_LOCAL_DATE -> p -> {
                    final OffsetDateTime createdOn = p.policy().getCreatedOn();
                    return isDateBefore(createdOn, searchCriteria.getValue().toString());
                };
                case AFTER_LOCAL_DATE -> p -> {
                    final OffsetDateTime createdOn = p.policy().getCreatedOn();
                    return isDateAfter(createdOn, searchCriteria.getValue().toString());
                };
                default -> throw new IllegalArgumentException(
                        MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS.formatted(searchCriteria.getProperty(),
                                List.of(BEFORE_LOCAL_DATE, AFTER_LOCAL_DATE)));
            };
        }

        private Predicate<PolicyWithBpn> getValidUntilFilter(final SearchCriteria<?> searchCriteria) {
            return switch (searchCriteria.getOperation()) {
                case BEFORE_LOCAL_DATE -> p -> {
                    final OffsetDateTime createdOn = p.policy().getValidUntil();
                    return isDateBefore(createdOn, searchCriteria.getValue().toString());
                };
                case AFTER_LOCAL_DATE -> p -> {
                    final OffsetDateTime createdOn = p.policy().getValidUntil();
                    return isDateAfter(createdOn, searchCriteria.getValue().toString());
                };
                default -> throw new IllegalArgumentException(
                        MSG_PROPERTY_ONLY_SUPPORTS_THE_FOLLOWING_OPERATIONS.formatted(searchCriteria.getProperty(),
                                List.of(BEFORE_LOCAL_DATE, AFTER_LOCAL_DATE)));
            };
        }
    }
}

