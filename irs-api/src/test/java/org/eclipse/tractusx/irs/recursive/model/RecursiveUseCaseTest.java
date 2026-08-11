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

import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.junit.jupiter.api.Test;

class RecursiveUseCaseTest {

    private static final RecursiveUseCase USE_CASE =
            RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;

    @Test
    void shouldDefinePurisLifecycleAndAspectBundle() {
        assertThat(USE_CASE.getDefaultBomLifecycle()).isEqualTo(BomLifecycle.AS_PLANNED);
        assertThat(USE_CASE.getAllowedBomLifecycles())
                .containsExactly(BomLifecycle.AS_PLANNED);
        assertThat(USE_CASE.getAllowedAspects()).containsExactly(
                RecursiveAspect.ITEM_STOCK_ANONYMIZED,
                RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED,
                RecursiveAspect.PLANNED_PRODUCTION_OUTPUT_ANONYMIZED);
    }

    @Test
    void shouldReturnOnlyRequestedAspectsFromBundleAtResultBoundary() {
        final String itemStock = RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();

        final Set<String> allowed = USE_CASE.selectAspectIds(BomLifecycle.AS_PLANNED, List.of(itemStock))
                .orElseThrow();

        assertThat(allowed).containsExactly(itemStock);
    }

    @Test
    void shouldRejectMissingOrInvalidSelectionsAtResultBoundary() {
        assertThat(USE_CASE.selectAspectIds(BomLifecycle.AS_PLANNED, List.of())).isEmpty();
        assertThat(USE_CASE.selectAspectIds(null, USE_CASE.getAspectSemanticIds())).isEmpty();
        assertThat(USE_CASE.selectAspectIds(BomLifecycle.AS_PLANNED,
                List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId(),
                        "urn:samm:io.catenax.item_stock:2.0.0#ItemStock"))).isEmpty();
        assertThat(USE_CASE.selectAspectIds(BomLifecycle.AS_SPECIFIED,
                USE_CASE.getAspectSemanticIds())).isEmpty();
        assertThat(USE_CASE.selectAspectIds(BomLifecycle.AS_BUILT,
                USE_CASE.getAspectSemanticIds())).isEmpty();
    }

    @Test
    void shouldRequireExactUseCaseAndAspectIdentifiers() {
        assertThat(RecursiveUseCase.valueOf(USE_CASE.name())).isEqualTo(USE_CASE);
        assertThat(RecursiveAspect.fromSemanticId("ItemStockAnonymized")).isEmpty();
        assertThat(RecursiveAspect.ITEM_STOCK_ANONYMIZED.matchesDescriptor(
                "urn:samm:example:1.0.0#ItemStockAnonymized", "ItemStockAnonymized")).isFalse();
    }
}
