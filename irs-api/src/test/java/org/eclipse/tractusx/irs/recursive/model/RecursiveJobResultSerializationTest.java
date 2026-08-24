/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RecursiveJobResultSerializationTest {

    @Test
    void shouldExposeRecursiveMaterialTree() {
        final String aspectType = RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
        final RecursiveJobResult result = RecursiveJobResult.builder()
                .childItems(List.of(RecursiveChildItem.builder()
                        .materialNumber("MNR-1")
                        .materialName("Semiconductor")
                        .quantity(RecursiveQuantity.builder().value(2.0).unit(ItemUnitEnumeration.UNIT_PIECE).build())
                        .items(List.of(RecursiveAspectItem.builder()
                                .aspect(aspectType)
                                .items(Map.of("direction", "OUTBOUND"))
                                .build()))
                        .tombstones(List.of())
                        .childItems(List.of())
                        .build()))
                .tombstones(List.of())
                .build();

        final JsonNode serialized = new ObjectMapper().valueToTree(result);
        final JsonNode childItem = serialized.path("childItems").path(0);
        final JsonNode aspectItem = childItem.path("items").path(0);

        assertThat(childItem.path("materialNumber").asText()).isEqualTo("MNR-1");
        assertThat(childItem.path("materialName").asText()).isEqualTo("Semiconductor");
        assertThat(childItem.path("quantity").path("value").asDouble()).isEqualTo(2.0);
        assertThat(childItem.path("quantity").path("unit").asText()).isEqualTo("unit:piece");
        assertThat(aspectItem.path("aspect").asText()).isEqualTo(aspectType);
        assertThat(aspectItem.path("items").path("direction").asText()).isEqualTo("OUTBOUND");
        assertThat(serialized.has("submodels")).isFalse();
        assertThat(serialized.has("submodelsByAspect")).isFalse();
        assertThat(serialized.has("supportedAspects")).isFalse();
        assertThat(serialized.has("supportedAspectsByLifecycle")).isFalse();
    }

    @Test
    void shouldRejectUnsupportedQuantityUnit() {
        final ObjectMapper objectMapper = new ObjectMapper();

        assertThatThrownBy(() -> objectMapper.readValue("""
                {"value": 1.0, "unit": "unit:unsupported"}
                """, RecursiveQuantity.class))
                .hasMessageContaining("Unsupported item unit");
    }

    @Test
    void shouldExposeMissingMaterialDataAsNullAndCollectionsAsArrays() {
        final RecursiveJobResult result = RecursiveJobResult.builder()
                .childItems(List.of(RecursiveChildItem.builder().build()))
                .build();

        final JsonNode childItem = new ObjectMapper().valueToTree(result).path("childItems").path(0);

        assertThat(childItem.has("materialNumber")).isTrue();
        assertThat(childItem.path("materialNumber").isNull()).isTrue();
        assertThat(childItem.has("materialName")).isTrue();
        assertThat(childItem.path("materialName").isNull()).isTrue();
        assertThat(childItem.has("quantity")).isTrue();
        assertThat(childItem.path("quantity").isNull()).isTrue();
        assertThat(childItem.path("items").isArray()).isTrue();
        assertThat(childItem.path("tombstones").isArray()).isTrue();
        assertThat(childItem.path("childItems").isArray()).isTrue();
    }
}
