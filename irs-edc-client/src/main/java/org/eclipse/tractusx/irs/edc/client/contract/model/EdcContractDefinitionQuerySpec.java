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
package org.eclipse.tractusx.irs.edc.client.contract.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * EdcContractDefinitionQuerySpec used for requesting contract definitions
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EdcContractDefinitionQuerySpec {
    @JsonProperty("@context")
    private EdcContext edcContext;

    @JsonProperty("filterExpression")
    private List<FilterExpression> filterExpression;


    /**
     * EdcContext used for EdcContractDefinitionQuerySpec
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EdcContext {
        @JsonProperty("@vocab")
        private String vocab = "https://w3id.org/edc/v0.0.1/ns/";
    }

    /**
     * FilterExpression used for EdcContractDefinitionQuerySpec
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FilterExpression {
        @JsonProperty("operandLeft")
        private String operandLeft;
        @JsonProperty("operator")
        private String operator;
        @JsonProperty("operandRight")
        private Object operandRight;
    }
}
