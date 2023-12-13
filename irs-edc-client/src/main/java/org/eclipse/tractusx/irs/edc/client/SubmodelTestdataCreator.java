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
package org.eclipse.tractusx.irs.edc.client;

import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;

/**
 * Class to create Submodel Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
public class SubmodelTestdataCreator {

    private final CxTestDataContainer cxTestDataContainer;
    private final ObjectMapper objectMapper;

    /* package */
    public SubmodelTestdataCreator(final CxTestDataContainer cxTestDataContainer) {
        this.cxTestDataContainer = cxTestDataContainer;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings({ "PMD.CyclomaticComplexity" })
    public Map<String, Object> createSubmodelForId(final String endpointAddress) {
        final String catenaXId = StringUtils.substringBefore(endpointAddress, "_");
        if (endpointAddress.contains("singleLevelBomAsBuilt")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsBuilt).orElse(Map.of());
        } else if (endpointAddress.contains("singleLevelUsageAsBuilt")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getSingleLevelUsageAsBuilt).orElse(Map.of());
        } else if (endpointAddress.contains("singleLevelBomAsSpecified")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsSpecified).orElse(Map.of());
        } else if (endpointAddress.contains("serialPart")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getSerialPart).orElse(Map.of());
        } else if (endpointAddress.contains("singleLevelBomAsPlanned")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned).orElse(Map.of());
        } else if (endpointAddress.contains("partAsPlanned")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getPartAsPlanned).orElse(Map.of());
        } else if (endpointAddress.contains("batch")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getBatch).orElse(Map.of());
        } else if (endpointAddress.contains("materialForRecycling")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getMaterialForRecycling).orElse(Map.of());
        } else if (endpointAddress.contains("productDescription")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getProductDescription).orElse(Map.of());
        } else if (endpointAddress.contains("physicalDimension")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getPhysicalDimension).orElse(Map.of());
        } else if (endpointAddress.contains("partAsSpecified")) {
            return this.cxTestDataContainer.getByCatenaXId(catenaXId).flatMap(CxTestDataContainer.CxTestData::getPartAsSpecified).orElse(Map.of());
        }
        return Map.of();
    }

    public <T> T createSubmodelForId(final String catenaXId, final Class<T> submodelClass) {
        final Map<String, Object> submodelForId = createSubmodelForId(catenaXId);
        return objectMapper.convertValue(submodelForId, submodelClass);
    }

}
