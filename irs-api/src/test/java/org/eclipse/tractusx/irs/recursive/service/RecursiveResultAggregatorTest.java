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

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneScope;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.Test;

class RecursiveResultAggregatorTest {

    private static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String ERROR_REF_A = "b0019a1e-e249-4f0d-bd68-2ad6b586ed80";
    private static final String ERROR_REF_B = "c390ed32-8836-4dac-8564-cf16bb9e453b";
    private static final String ERROR_REF_C = "0fa2949c-105c-4c77-9716-dcd45908eca0";

    private final RecursiveResultAggregator aggregator = new RecursiveResultAggregator();

    @Test
    void shouldBuildRootTreeAndApplyQuantityFromBomRelationship() {
        final RecursiveJobState state = state(List.of(childRequest("message-1", quantity(2.0))));
        final RecursiveChildItem grandchild = node("MNR-2", "Transistor", List.of(), List.of());
        final RecursiveChildItem child = node("MNR-1", "Semiconductor",
                List.of(aspectItem(Map.of("stock", 10))), List.of(grandchild));

        final RecursiveJobResult result = aggregate(state, Map.of("message-1", childResult(child)));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.COMPLETE);
        assertThat(result.getTombstones()).isEmpty();
        assertThat(result.getChildItems()).singleElement().satisfies(material -> {
            assertThat(material.getMaterialNumber()).isEqualTo("MNR-1");
            assertThat(material.getMaterialName()).isEqualTo("Semiconductor");
            assertThat(material.getQuantity()).isEqualTo(quantity(2.0));
            assertThat(material.getItems()).singleElement().satisfies(item ->
                    assertThat(item.getItems()).containsEntry("stock", 10));
            assertThat(material.getChildItems()).singleElement().satisfies(descendant ->
                    assertThat(descendant.getMaterialNumber()).isEqualTo("MNR-2"));
        });
    }

    @Test
    void shouldAggregateTombstonesOnlyWithinTheirMaterialNode() {
        final RecursiveJobState state = state(List.of(
                childRequest("message-1", quantity(1.0)),
                childRequest("message-2", quantity(1.0))));
        final RecursiveChildItem first = node("MNR-1", "First", List.of(), List.of());
        final RecursiveChildItem second = node("MNR-2", "Second", List.of(), List.of());
        final RecursiveJobResult firstResult = childResult(first.toBuilder()
                .tombstones(List.of(localFailure(ERROR_REF_A), localFailure(ERROR_REF_B)))
                .build());
        final RecursiveJobResult secondResult = childResult(second.toBuilder()
                .tombstones(List.of(localFailure(ERROR_REF_C)))
                .build());

        final RecursiveJobResult result = aggregate(state,
                Map.of("message-1", firstResult, "message-2", secondResult));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(result.getTombstones()).isEmpty();
        assertThat(result.getChildItems()).hasSize(2);
        assertThat(result.getChildItems().get(0).getTombstones()).singleElement().satisfies(tombstone -> {
            assertThat(tombstone.getOccurrences()).isEqualTo(2);
            assertThat(Set.copyOf(tombstone.getErrorRefs())).isEqualTo(Set.of(ERROR_REF_A, ERROR_REF_B));
        });
        assertThat(result.getChildItems().get(1).getTombstones()).singleElement().satisfies(tombstone ->
                assertThat(tombstone.getOccurrences()).isEqualTo(1));
    }

    @Test
    void shouldAddNodeTombstoneWhenBomQuantityIsIncomplete() {
        final RecursiveJobState state = state(List.of(childRequest("message-1", null)));

        final RecursiveJobResult result = aggregate(state,
                Map.of("message-1", childResult(node("MNR-1", "Semiconductor", List.of(), List.of()))));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(result.getChildItems()).singleElement().satisfies(child -> {
            assertThat(child.getQuantity()).isNull();
            assertThat(child.getTombstones()).singleElement().satisfies(tombstone -> {
                assertThat(tombstone.getReason())
                        .isEqualTo(RecursiveTombstoneReason.BOM_QUANTITY_NOT_AVAILABLE);
                assertThat(tombstone.getAspects()).isEmpty();
            });
        });
    }

    @Test
    void shouldAddNodeTombstoneWhenBomQuantityIsInvalid() {
        final RecursiveQuantity invalidQuantity = RecursiveQuantity.builder()
                .value(-1.0)
                .unit("unit:piece")
                .build();
        final RecursiveJobState state = state(List.of(childRequest("message-1", invalidQuantity)));

        final RecursiveJobResult result = aggregate(state,
                Map.of("message-1", childResult(node("MNR-1", "Semiconductor", List.of(), List.of()))));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.PARTIAL);
        assertThat(result.getChildItems()).singleElement().satisfies(child -> {
            assertThat(child.getQuantity()).isNull();
            assertThat(child.getTombstones()).singleElement().satisfies(tombstone ->
                    assertThat(tombstone.getReason())
                            .isEqualTo(RecursiveTombstoneReason.BOM_QUANTITY_NOT_AVAILABLE));
        });
    }

    @Test
    void shouldRejectNullMaterialNodeInChildResponse() {
        final RecursiveJobState state = state(List.of(childRequest("message-1", quantity(2.0))));
        final RecursiveJobResult invalidChildResult = RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.COMPLETE)
                .childItems(Collections.singletonList(null))
                .tombstones(List.of())
                .build();

        final RecursiveJobResult result = aggregate(state, Map.of("message-1", invalidChildResult));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(result.getChildItems()).singleElement().satisfies(child -> {
            assertThat(child.getQuantity()).isEqualTo(quantity(2.0));
            assertThat(child.getTombstones()).singleElement().satisfies(tombstone ->
                    assertThat(tombstone.getReason())
                            .isEqualTo(RecursiveTombstoneReason.CHILD_RESPONSE_INVALID));
        });
    }

    @Test
    void shouldAttachEarlyChildRejectionWithoutInvalidResponseTombstone() {
        final RecursiveJobState state = state(List.of(childRequest("message-1", quantity(2.0))));
        final RecursiveTombstone rejection = RecursiveTombstone.builder()
                .type("RECURSIVE_TOMBSTONE")
                .scope(RecursiveTombstoneScope.RECURSIVE_CHAIN)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .reason(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED)
                .retryable(false)
                .detail("The recursive chain opening grant was rejected.")
                .occurrences(1)
                .errorRefs(List.of("185ec47c-fcd6-4c69-b2ab-e3af3b725d3d"))
                .build();
        final RecursiveJobResult childResult = RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.FAILED)
                .childItems(List.of())
                .tombstones(List.of(rejection))
                .build();

        final RecursiveJobResult result = aggregate(state, Map.of("message-1", childResult));

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(result.getChildItems()).singleElement().satisfies(child -> {
            assertThat(child.getQuantity()).isEqualTo(quantity(2.0));
            assertThat(child.getTombstones()).singleElement().satisfies(tombstone ->
                    assertThat(tombstone.getReason()).isEqualTo(RecursiveTombstoneReason.CHAIN_OPENING_REJECTED));
        });
    }

    @Test
    void shouldReturnFailedResultOutsideUseCasePolicy() {
        final RecursiveJobState state = state(List.of()).toBuilder()
                .bomLifecycle(BomLifecycle.AS_SPECIFIED)
                .build();

        final RecursiveJobResult result = aggregator.aggregate(state, List.of(), null);

        assertThat(result.getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(result.getChildItems()).isEmpty();
        assertThat(result.getTombstones()).isNotEmpty();
    }

    private RecursiveJobResult childResult(final RecursiveChildItem child) {
        return RecursiveJobResult.builder()
                                 .resultStatus(RecursiveResultStatus.COMPLETE)
                                 .childItems(List.of(child))
                                 .tombstones(List.of())
                                 .build();
    }

    private RecursiveChildItem node(final String materialNumber, final String materialName,
            final List<RecursiveAspectItem> items, final List<RecursiveChildItem> children) {
        return RecursiveChildItem.builder()
                                 .materialNumber(materialNumber)
                                 .materialName(materialName)
                                 .items(items)
                                 .tombstones(List.of())
                                 .childItems(children)
                                 .build();
    }

    private RecursiveAspectItem aspectItem(final Map<String, Object> payload) {
        return RecursiveAspectItem.builder()
                                  .aspect(ITEM_STOCK_ANONYMIZED)
                                  .items(payload)
                                  .build();
    }

    private RecursiveTombstone localFailure(final String errorRef) {
        return RecursiveTombstone.builder()
                                 .type("RECURSIVE_TOMBSTONE")
                                 .scope(RecursiveTombstoneScope.LOCAL_NODE)
                                 .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                                 .reason(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED)
                                 .retryable(true)
                                 .detail("Requested aspect could not be retrieved.")
                                 .occurrences(1)
                                 .errorRefs(List.of(errorRef))
                                 .build();
    }

    private RecursiveChildBranch childRequest(final String messageId,
            final RecursiveQuantity quantity) {
        return RecursiveChildBranch.builder()
                                   .messageId(messageId)
                                   .partnerBpnl("BPNL0000CHILD001")
                                   .childGlobalAssetId("urn:uuid:" + messageId)
                                   .quantity(quantity)
                                   .status(RecursiveResponseStatus.COMPLETED)
                                   .build();
    }

    private RecursiveQuantity quantity(final double value) {
        return RecursiveQuantity.builder().value(value).unit("unit:piece").build();
    }

    private RecursiveJobState state(final List<RecursiveChildBranch> childBranches) {
        final ZonedDateTime now = ZonedDateTime.now();
        return RecursiveJobState.builder()
                                .jobId("job-1")
                                .openingId("opening-1")
                                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                                .globalAssetId("urn:uuid:11111111-1111-1111-1111-111111111111")
                                .bomLifecycle(BomLifecycle.AS_PLANNED)
                                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                                .requesterBpnl("BPNL0000ATLS0001")
                                .receiverBpnl("BPNL0000BELF0001")
                                .messageId("message-root")
                                .createdOn(now)
                                .lastModifiedOn(now)
                                .deadline(now.plusMinutes(30))
                                .childResponseDeadline(now.plusMinutes(29))
                                .state(RecursiveJobPhase.AWAITING_CHILDREN)
                                .rootJob(true)
                                .bomChildren(List.of())
                                .childBranches(childBranches)
                                .build();
    }

    private RecursiveJobResult aggregate(final RecursiveJobState state,
            final Map<String, RecursiveJobResult> payloads) {
        final List<RecursiveChildBranch> childBranches = state.getChildBranches().stream()
                .map(childBranch -> childBranch.toBuilder()
                        .responsePayload(payloads.get(childBranch.getMessageId()))
                        .build())
                .toList();
        return aggregator.aggregate(state.toBuilder().childBranches(childBranches).build(), List.of(), null);
    }
}
