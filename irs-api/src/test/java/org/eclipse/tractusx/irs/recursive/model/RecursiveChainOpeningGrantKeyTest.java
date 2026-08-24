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

import org.junit.jupiter.api.Test;

class RecursiveChainOpeningGrantKeyTest {

    private static final String OPENING_ID = "opening-42";
    private static final String REQUESTER_BPN = "BPNL0000ATLS0001";
    private static final RecursiveUseCase USE_CASE =
            RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String PLAIN_GLOBAL_ASSET_ID = "68904173-ad59-4a77-8412-3e73fcafbd8b";

    @Test
    void shouldCreateKeyWhenAllPartsArePresent() {
        assertThat(RecursiveChainOpeningGrantKey.optionalOf(
                OPENING_ID, PLAIN_GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE))
                .get()
                .extracting(RecursiveChainOpeningGrantKey::globalAssetId)
                .isEqualTo("urn:uuid:" + PLAIN_GLOBAL_ASSET_ID);
    }

    @Test
    void shouldNotCreateKeyWhenAnyPartIsMissing() {
        assertThat(RecursiveChainOpeningGrantKey.optionalOf(null, PLAIN_GLOBAL_ASSET_ID, REQUESTER_BPN, USE_CASE))
                .isEmpty();
        assertThat(RecursiveChainOpeningGrantKey.optionalOf(OPENING_ID, null, REQUESTER_BPN, USE_CASE))
                .isEmpty();
        assertThat(RecursiveChainOpeningGrantKey.optionalOf(OPENING_ID, PLAIN_GLOBAL_ASSET_ID, null, USE_CASE))
                .isEmpty();
        assertThat(RecursiveChainOpeningGrantKey.optionalOf(OPENING_ID, PLAIN_GLOBAL_ASSET_ID, REQUESTER_BPN, null))
                .isEmpty();
    }
}
