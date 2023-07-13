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
 * https://www.apache.org/licenses/LICENSE-2.0. *
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

        public static final String SERIAL_PART_ASPECT_TYPE = "urn:bamm:io.catenax.serial_part:1.1.0#SerialPart";
        public static final String SINGLE_LEVEL_BOM_AS_BUILT_ASPECT_TYPE = "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt";
        public static final String SINGLE_LEVEL_USAGE_BUILT_ASPECT_TYPE = "urn:bamm:io.catenax.single_level_usage_as_built:1.0.1#SingleLevelUsageAsBuilt";
        public static final String PART_AS_PLANNED_ASPECT_TYPE = "urn:bamm:io.catenax.part_as_planned:1.0.0#PartAsPlanned";
        public static final String SINGLE_LEVEL_BOM_AS_PLANNED_ASPECT_TYPE = "urn:bamm:io.catenax.single_level_bom_as_planned:2.0.0#SingleLevelBomAsPlanned";
        public static final String BATCH_ASPECT_TYPE = "urn:bamm:io.catenax.batch:1.0.0#Batch";
        public static final String MATERIAL_FOR_RECYCLING_ASPECT_TYPE = "urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling";
        public static final String PRODUCT_DESCRIPTION_ASPECT_TYPE = "urn:bamm:io.catenax.battery.product_description:1.0.1#ProductDescription";
        public static final String PHYSICAL_DIMENSION_ASPECT_TYPE = "urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension";

        private String catenaXId;
        @JsonProperty(SERIAL_PART_ASPECT_TYPE)
        private List<Map<String, Object>> serialPart;
        @JsonProperty(SINGLE_LEVEL_BOM_AS_BUILT_ASPECT_TYPE)
        private List<Map<String, Object>> singleLevelBomAsBuilt;
        @JsonProperty(SINGLE_LEVEL_USAGE_BUILT_ASPECT_TYPE)
        private List<Map<String, Object>> singleLevelUsageAsBuilt;
        @JsonProperty(PART_AS_PLANNED_ASPECT_TYPE)
        private List<Map<String, Object>> partAsPlanned;
        @JsonProperty(SINGLE_LEVEL_BOM_AS_PLANNED_ASPECT_TYPE)
        private List<Map<String, Object>> singleLevelBomAsPlanned;
        @JsonProperty(BATCH_ASPECT_TYPE)
        private List<Map<String, Object>> batch;
        @JsonProperty(MATERIAL_FOR_RECYCLING_ASPECT_TYPE)
        private List<Map<String, Object>> materialForRecycling;
        @JsonProperty(PRODUCT_DESCRIPTION_ASPECT_TYPE)
        private List<Map<String, Object>> productDescription;
        @JsonProperty(PHYSICAL_DIMENSION_ASPECT_TYPE)
        private List<Map<String, Object>> physicalDimension;

        public Optional<Map<String, Object>> getSerialPart() {
            return serialPart != null ? serialPart.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsBuilt() {
            return singleLevelBomAsBuilt != null ? singleLevelBomAsBuilt.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelUsageAsBuilt() {
            return singleLevelUsageAsBuilt != null ? singleLevelUsageAsBuilt.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getPartAsPlanned() {
            return partAsPlanned != null ? partAsPlanned.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsPlanned() {
            return singleLevelBomAsPlanned != null ? singleLevelBomAsPlanned.stream().findFirst() : Optional.empty();
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
    }
}
