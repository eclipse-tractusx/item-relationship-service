/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * IntegrityAspect
 */
@Data
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class IntegrityAspect {

    private String catenaXId;
    private Set<ChildData> childParts;

    /**
     * ChildData
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildData {
        private String catenaXId;
        private Set<Reference> references;

        /**
         *
         * @param catenaXId filter
         * @return Check if childCatenaXId matches argument
         */
        public boolean catenaXIdMatches(final String catenaXId) {
            return this.catenaXId.equals(catenaXId);
        }
    }

    /**
     * Reference
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Reference {
        private String semanticModelUrn;
        private String hash;
        private String signature;

        /**
         * @param aspectType filter
         * @return Check if semanticModelUrn matches argument
         */
        public boolean semanticModelUrnMatches(final String aspectType) {
            return this.getSemanticModelUrn().equals(aspectType);
        }
    }
}
