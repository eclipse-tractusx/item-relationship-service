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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneScope;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneType;
import org.junit.jupiter.api.Test;

/**
 * Privacy guard for sanitized recursive tombstones: only the allowed reason vocabulary leaves the
 * instance and no field carries a foreign BPN, URL, UUID/globalAssetId or asset identifier.
 */
class RecursiveTombstonesTest {

    @Test
    void externalReasonMapsInternalCodesToAllowedVocabulary() {
        assertThat(RecursiveTombstoneReason.fromInternalReason("SUBMODEL_REQUEST_FAILED"))
                .isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
        assertThat(RecursiveTombstoneReason.fromInternalReason("CHAIN_OPENING_REJECTED"))
                .isEqualTo(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED);
        assertThat(RecursiveTombstoneReason.fromInternalReason("CHILD_RESPONSE_TIMEOUT"))
                .isEqualTo(RecursiveTombstoneReason.CHILD_RESPONSE_TIMEOUT);
        assertThat(RecursiveTombstoneReason.fromInternalReason("SHELL_NOT_FOUND"))
                .isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_NOT_AVAILABLE);
    }

    @Test
    void externalReasonFallsBackForUnknownOrLeakyReason() {
        assertThat(RecursiveTombstoneReason.fromInternalReason("BPNL000000000065 at https://edc.example"))
                .isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
        assertThat(RecursiveTombstoneReason.fromInternalReason(null))
                .isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
        assertThat(RecursiveTombstoneReason.fromInternalReason("   "))
                .isEqualTo(RecursiveTombstoneReason.CHILD_BRANCH_FAILED);
    }

    @Test
    void sanitizedTombstoneLeaksNoForeignIdentifiers() {
        final RecursiveTombstone raw = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.CHILD_BRANCH)
                .aspects(List.of("BPNL000000000065-leak",
                        "urn:uuid:2c57b0e9-a653-411d-bdcd-64787e9fd3a7"))
                .reason(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED)
                .retryable(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED.isRetryable())
                .detail("connectorEndpoint=https://edc-belfast.example/dsp "
                        + "assetId=belfast-gearbox-item-stock-asset BPNL000000000065")
                .occurrences(1)
                .errorRefs(List.of("BPNL000000000065-at-https://edc-belfast.example/dsp"))
                .build();

        final RecursiveTombstone tombstone = RecursiveTombstones.sanitized(raw,
                List.of("urn:samm:io.catenax.item_stock_anonymized:1.0.0#ItemStockAnonymized"));

        assertThat(tombstone.getType()).isEqualTo(RecursiveTombstoneType.RECURSIVE_TOMBSTONE);
        assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
        assertThat(tombstone.getScope()).isEqualTo(RecursiveTombstoneScope.CHILD_BRANCH);
        assertThat(tombstone.getErrorRefs()).hasSize(1);
        assertThatCode(() -> UUID.fromString(tombstone.getErrorRefs().get(0)))
                .doesNotThrowAnyException();
        assertThat(tombstone.getOccurrences()).isEqualTo(1);
        assertThat(tombstone.toString())
                .doesNotContain("BPNL000000000065", "BPNS0000000001VY",
                        "https://edc-belfast.example/dsp", "belfast-gearbox-item-stock-asset",
                        "urn:uuid:2c57b0e9-a653-411d-bdcd-64787e9fd3a7");
    }

    @Test
    void sanitizedKeepsExistingErrorRefForCorrelation() {
        final String secondRef = "4e3bea84-a48f-4319-8380-a50e4d615560";
        final String thirdRef = "f410c848-2b87-48ae-9da4-294079226a1f";
        final RecursiveTombstone raw = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.CHILD_BRANCH)
                .aspects(List.of())
                .reason(RecursiveTombstoneReason.CHILD_BRANCH_FAILED)
                .retryable(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.isRetryable())
                .detail("A recursive child branch failed.")
                .errorRefs(List.of(secondRef, thirdRef))
                .occurrences(2)
                .build();

        final RecursiveTombstone sanitized = RecursiveTombstones.sanitized(raw, List.of());
        assertThat(sanitized.getErrorRefs()).isEqualTo(List.of(secondRef, thirdRef));
        assertThat(sanitized.getOccurrences()).isEqualTo(2);
    }

    @Test
    void sanitizedKeepsAspectListMachineReadable() {
        final RecursiveTombstone raw = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.CHILD_BRANCH)
                .reason(RecursiveTombstoneReason.CHILD_BRANCH_FAILED)
                .retryable(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.isRetryable())
                .aspects(List.of("urn:samm:io.catenax.item_stock_anonymized:1.0.0#ItemStockAnonymized",
                        "urn:samm:io.catenax.delivery_information_anonymized:1.0.0#DeliveryInformationAnonymized",
                        "urn:samm:io.catenax.item_stock:2.0.0#ItemStock"))
                .detail("A recursive child branch failed.")
                .occurrences(1)
                .errorRefs(List.of("4e3bea84-a48f-4319-8380-a50e4d615560"))
                .build();

        final RecursiveTombstone tombstone = RecursiveTombstones.sanitized(raw,
                List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId(),
                        RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId()));

        // Aspects stay individual, untruncated entries - never a joined or shortened string.
        assertThat(tombstone.getAspects()).isEqualTo(
                List.of("urn:samm:io.catenax.item_stock_anonymized:1.0.0#ItemStockAnonymized",
                        "urn:samm:io.catenax.delivery_information_anonymized:1.0.0#DeliveryInformationAnonymized"));
    }

    @Test
    void sanitizedRejectsIncompleteTombstone() {
        final RecursiveTombstone raw = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .reason(RecursiveTombstoneReason.CHILD_BRANCH_FAILED)
                .retryable(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.isRetryable())
                .detail("A recursive child branch failed.")
                .occurrences(1)
                .aspects(List.of())
                .errorRefs(List.of("4e3bea84-a48f-4319-8380-a50e4d615560"))
                .build();

        assertThatThrownBy(() -> RecursiveTombstones.sanitized(raw, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sanitizedRejectsUnsafeDetail() {
        final RecursiveTombstone raw = RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.CHILD_BRANCH)
                .reason(RecursiveTombstoneReason.CHILD_BRANCH_FAILED)
                .retryable(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.isRetryable())
                .detail("first line\nsecond line")
                .occurrences(1)
                .aspects(List.of())
                .errorRefs(List.of("4e3bea84-a48f-4319-8380-a50e4d615560"))
                .build();

        assertThatThrownBy(() -> RecursiveTombstones.sanitized(raw, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void factoryReplacesUnsafeDetail() {
        final RecursiveTombstone tombstone = RecursiveTombstones.childBranch(
                List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId()),
                RecursiveTombstoneReason.CHILD_BRANCH_FAILED,
                "first line\nsecond line");

        assertThat(tombstone.getDetail()).isEqualTo("Recursive tombstone reason: CHILD_BRANCH_FAILED");
    }
}
