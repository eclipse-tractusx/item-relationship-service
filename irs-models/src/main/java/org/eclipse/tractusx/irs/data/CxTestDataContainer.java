/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.eclipse.tractusx.irs.SemanticModelNames;

/**
 * Container to load test data from resources
 */
@Data
public class CxTestDataContainer {

    @JsonProperty("https://catenax.io/schema/TestDataContainer/1.0.0")
    private List<CxTestData> testData;

    /**
     * @param catenaXId catenaX id
     * @return test data for
     */
    public Optional<CxTestData> getByCatenaXId(final String catenaXId) {
        return this.getTestData().stream().filter(cxTestData -> catenaXId.equals(cxTestData.getCatenaXId())).findFirst();
    }

    /**
     * Single test data
     */
    @SuppressWarnings("PMD.DataClass")
    @Data
    public static class CxTestData {

        private String catenaXId;
        @JsonProperty(SemanticModelNames.SERIAL_PART_3_0_0)
        private List<Map<String, Object>> serialPart;
        @JsonProperty(SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0)
        private List<Map<String, Object>> singleLevelBomAsBuilt;
        @JsonProperty(SemanticModelNames.SINGLE_LEVEL_USAGE_AS_BUILT_3_0_0)
        private List<Map<String, Object>> singleLevelUsageAsBuilt;
        @JsonProperty(SemanticModelNames.SINGLE_LEVEL_BOM_AS_SPECIFIED_2_0_0)
        private List<Map<String, Object>> singleLevelBomAsSpecified;
        @JsonProperty(SemanticModelNames.PART_AS_PLANNED_2_0_0)
        private List<Map<String, Object>> partAsPlanned;
        @JsonProperty(SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0)
        private List<Map<String, Object>> singleLevelBomAsPlanned;
        @JsonProperty(SemanticModelNames.SINGLE_LEVEL_USAGE_AS_PLANNED_2_0_0)
        private List<Map<String, Object>> singleLevelUsageAsPlanned;
        @JsonProperty(SemanticModelNames.BATCH_3_0_0)
        private List<Map<String, Object>> batch;
        @JsonProperty(SemanticModelNames.MATERIAL_FOR_RECYCLING_1_1_0)
        private List<Map<String, Object>> materialForRecycling;
        @JsonProperty(SemanticModelNames.BATTERY_PRODUCT_DESCRIPTION_1_0_1)
        private List<Map<String, Object>> productDescription;
        @JsonProperty(SemanticModelNames.PHYSICAL_DIMENSION_1_0_0)
        private List<Map<String, Object>> physicalDimension;
        @JsonProperty(SemanticModelNames.PART_AS_SPECIFIED_2_0_0)
        private List<Map<String, Object>> partAsSpecified;

        public Optional<Map<String, Object>> getSerialPart() {
            return serialPart != null ? serialPart.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsBuilt() {
            return singleLevelBomAsBuilt != null ? singleLevelBomAsBuilt.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelUsageAsBuilt() {
            return singleLevelUsageAsBuilt != null ? singleLevelUsageAsBuilt.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsSpecified() {
            return singleLevelBomAsSpecified != null ? singleLevelBomAsSpecified.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getPartAsPlanned() {
            return partAsPlanned != null ? partAsPlanned.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsPlanned() {
            return singleLevelBomAsPlanned != null ? singleLevelBomAsPlanned.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelUsageAsPlanned() {
            return singleLevelUsageAsPlanned != null ? singleLevelUsageAsPlanned.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getBatch() {
            return batch != null ? batch.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getMaterialForRecycling() {
            return materialForRecycling != null ? materialForRecycling.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getProductDescription() {
            return productDescription != null ? productDescription.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getPhysicalDimension() {
            return physicalDimension != null ? physicalDimension.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getPartAsSpecified() {
            return partAsSpecified != null ? partAsSpecified.stream().findFirst() : Optional.empty();
        }
    }
}
