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
package org.eclipse.tractusx.irs.policystore.common;

import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_ACTION;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_BPN;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_CREATED_ON;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_POLICY_ID;
import static org.eclipse.tractusx.irs.policystore.common.CommonConstants.PROPERTY_VALID_UNTIL;
import static org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation.BETWEEN;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.policystore.models.SearchCriteria;
import org.eclipse.tractusx.irs.policystore.models.SearchCriteria.Operation;

/**
 * Parser for search parameters.
 */
@Getter
@SuppressWarnings({ "PMD.TooManyStaticImports" })
public class SearchParameterParser {

    public static final List<String> SUPPORTED_PROPERTIES = List.of(PROPERTY_BPN, PROPERTY_VALID_UNTIL,
            PROPERTY_POLICY_ID, PROPERTY_CREATED_ON, PROPERTY_ACTION);
    public static final List<String> DATE_PROPERTIES = List.of(PROPERTY_VALID_UNTIL, PROPERTY_CREATED_ON);

    public static final String CRITERIA_INNER_SEPARATOR = ",";
    public static final int NUM_PARTS_OF_FILTERS = 3;

    private final List<SearchCriteria<?>> searchCriteria;

    public SearchParameterParser(final List<String> searchParameters) {
        searchCriteria = parseSearchParameters(searchParameters);
    }

    private List<SearchCriteria<?>> parseSearchParameters(final List<String> searchParameterList) {
        final List<SearchCriteria<?>> searchCriteria = new ArrayList<>();
        if (searchParameterList != null) {
            for (int i = 0; i < searchParameterList.size(); i++) {

                final String searchParameter = searchParameterList.get(i);
                final String[] splittedSearchParam = StringUtils.split(searchParameter, CRITERIA_INNER_SEPARATOR);

                if (splittedSearchParam.length < NUM_PARTS_OF_FILTERS) {
                    throw new IllegalArgumentException(("Illegal search parameter at index %s. "
                            + "Format should be <propertyName>,<operation>,<value>.").formatted(i));
                }

                final String property = getProperty(splittedSearchParam[0]);
                final SearchCriteria.Operation operation = getOperation(splittedSearchParam[1], property);
                final String value = getValue(splittedSearchParam[2]);

                searchCriteria.add(new SearchCriteria<>(property, operation, value));
            }
        }

        return searchCriteria;
    }

    private static String getValue(final String value) {
        return StringUtils.trimToEmpty(value);
    }

    private static String getProperty(final String property) {
        final String trimmedProperty = StringUtils.trimToEmpty(property);
        if (SUPPORTED_PROPERTIES.stream().noneMatch(p -> p.equalsIgnoreCase(trimmedProperty))) {
            throw new IllegalArgumentException("Only the following properties support filtering: %s".formatted(
                    String.join(", ", SUPPORTED_PROPERTIES)));
        }
        return trimmedProperty;
    }

    private Operation getOperation(final String operationStr, final String property) {
        final Operation operation = Operation.valueOf(StringUtils.trimToEmpty(operationStr));
        if (operation == BETWEEN && DATE_PROPERTIES.stream().noneMatch(p -> p.equalsIgnoreCase(property))) {
            throw new IllegalArgumentException("Operation BETWEEN is only supported for date properties");
        }
        return operation;
    }

}
