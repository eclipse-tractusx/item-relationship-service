/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.configuration.local;

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
    @Data
    public static class CxTestData {
        private String catenaXId;
        @JsonProperty("urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization")
        private List<Map<String, Object>> serialPartTypization;
        @JsonProperty("urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship")
        private List<Map<String, Object>> assemblyPartRelationship;
        @JsonProperty("urn:bamm:io.catenax.part_as_planned:1.0.0#PartAsPlanned")
        private List<Map<String, Object>> partAsPlanned;
        @JsonProperty("urn:bamm:io.catenax.single_level_bom_as_planned:1.0.2#SingleLevelBomAsPlanned")
        private List<Map<String, Object>> singleLevelBomAsPlanned;
        @JsonProperty("urn:bamm:io.catenax.batch:1.0.0#Batch")
        private List<Map<String, Object>> batch;
        @JsonProperty("urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling")
        private List<Map<String, Object>> materialForRecycling;
        @JsonProperty("urn:bamm:io.catenax.battery.product_description:1.0.1#ProductDescription")
        private List<Map<String, Object>> productDescription;
        @JsonProperty("urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension")
        private List<Map<String, Object>> physicalDimension;

        public Optional<Map<String, Object>> getSerialPartTypization() {
            return serialPartTypization != null ? serialPartTypization.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getAssemblyPartRelationship() {
            return assemblyPartRelationship != null ? assemblyPartRelationship.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getPartAsPlanned() {
            return partAsPlanned != null ? partAsPlanned.stream().findFirst() : Optional.empty();
        }

        public Optional<Map<String, Object>> getSingleLevelBomAsPlanned() {
            return singleLevelBomAsPlanned != null ? singleLevelBomAsPlanned.stream().findFirst() : Optional.empty();
        }
    }
}
