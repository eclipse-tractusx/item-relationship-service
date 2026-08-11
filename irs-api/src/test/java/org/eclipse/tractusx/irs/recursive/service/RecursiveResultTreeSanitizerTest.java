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
package org.eclipse.tractusx.irs.recursive.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.junit.jupiter.api.Test;

class RecursiveResultTreeSanitizerTest {

    private static final String ITEM_STOCK = RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String DELIVERY_INFORMATION =
            RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();

    @Test
    void shouldSanitizeNestedMaterialNodes() {
        final RecursiveChildItem child = RecursiveChildItem.builder()
                .materialNumber("MNR-2")
                .materialName("Transistor")
                .quantity(quantity(1.0))
                .items(List.of(aspectItem(ITEM_STOCK)))
                .build();
        final RecursiveChildItem root = RecursiveChildItem.builder()
                .materialNumber("MNR-1")
                .materialName("Semiconductor")
                .quantity(quantity(-1.0))
                .items(List.of(aspectItem(ITEM_STOCK), aspectItem(DELIVERY_INFORMATION)))
                .childItems(List.of(child))
                .build();

        final RecursiveChildItem result = RecursiveResultTreeSanitizer.sanitizeAndAggregateNode(
                root, List.of(ITEM_STOCK));

        assertThat(result.getQuantity()).isNull();
        assertThat(result.getItems()).singleElement()
                .satisfies(item -> assertThat(item.getAspect()).isEqualTo(ITEM_STOCK));
        assertThat(result.getChildItems()).singleElement().satisfies(material -> {
            assertThat(material.getMaterialNumber()).isEqualTo("MNR-2");
            assertThat(material.getQuantity()).isEqualTo(quantity(1.0));
            assertThat(material.getItems()).singleElement()
                    .satisfies(item -> assertThat(item.getAspect()).isEqualTo(ITEM_STOCK));
        });
    }

    @Test
    void shouldNotTreatBomQuantityAsUsableData() {
        final RecursiveChildItem quantityOnly = RecursiveChildItem.builder()
                .quantity(quantity(2.0))
                .build();
        final RecursiveChildItem parentWithMaterialBelow = RecursiveChildItem.builder()
                .childItems(List.of(RecursiveChildItem.builder()
                        .materialName("Transistor")
                        .build()))
                .build();

        assertThat(RecursiveResultTreeSanitizer.hasUsableData(quantityOnly)).isFalse();
        assertThat(RecursiveResultTreeSanitizer.hasUsableData(parentWithMaterialBelow)).isTrue();
    }

    private RecursiveAspectItem aspectItem(final String aspect) {
        return RecursiveAspectItem.builder()
                .aspect(aspect)
                .items(Map.of("value", 10))
                .build();
    }

    private RecursiveQuantity quantity(final double value) {
        return RecursiveQuantity.builder()
                .value(value)
                .unit("unit:piece")
                .build();
    }
}
