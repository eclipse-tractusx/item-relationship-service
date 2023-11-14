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
package org.eclipse.tractusx.irs.semanticshub;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Pagination wrapper Object.
 *
 * @param <T> Type of paginated item
 */
@SuppressWarnings({ "PMD.UnusedFormalParameter" })
public class PaginatedResponse<T> {
    private final List<T> items;
    private final int totalItems;
    private final int currentPage;

    public PaginatedResponse(final @JsonProperty("items") List<T> items,
            final @JsonProperty("totalItems") int totalItems, final @JsonProperty("currentPage") int currentPage) {
        this.items = List.copyOf(items);
        this.totalItems = totalItems;
        this.currentPage = currentPage;
    }

    public PageImpl<T> toPageImpl(final int pageSize) {
        return new PageImpl<>(items, PageRequest.of(currentPage, pageSize), totalItems);

    }
}
