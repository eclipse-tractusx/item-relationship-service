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
package org.eclipse.tractusx.irs.policystore.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Search criteria.
 *
 * @param <T> type for value
 */
@AllArgsConstructor
@Getter
public class SearchCriteria<T> {

    private String property;
    private Operation operation;
    private T value;

    /**
     * Search filter operation.
     */
    public enum Operation {
        EQUALS,
        STARTS_WITH,
        BETWEEN
    }
}
