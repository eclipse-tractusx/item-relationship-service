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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildBranch;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobPhase;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobStatusResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.Test;

class RecursiveResponseMapperTest {

    private static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String DELIVERY_INFORMATION_ANONYMIZED =
            RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();

    @Test
    void shouldExposeOnlyRequestedAnonymizedAspectsInMaterialTree() {
        final RecursiveChildItem rawChild = RecursiveChildItem.builder()
                .materialNumber("MNR-1")
                .materialName("Semiconductor")
                .quantity(RecursiveQuantity.builder().value(2.0).unit("unit:piece").build())
                .items(List.of(
                        aspectItem(ITEM_STOCK_ANONYMIZED, Map.of("quantity", 10)),
                        aspectItem(DELIVERY_INFORMATION_ANONYMIZED, Map.of("quantity", 30)),
                        aspectItem("urn:samm:io.catenax.item_stock:2.0.0#ItemStock",
                                Map.of("quantity", 40))))
                .tombstones(List.of())
                .childItems(List.of())
                .build();
        final RecursiveJobResult rawResult = RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.COMPLETE)
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .requestedAspects(List.of("injectedAspect"))
                .childItems(List.of(rawChild))
                .tombstones(List.of())
                .build();

        final RecursiveJobResult externalResult = RecursiveResponseMapper.toExternalResult(
                rawResult, RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE, BomLifecycle.AS_PLANNED,
                List.of(ITEM_STOCK_ANONYMIZED, DELIVERY_INFORMATION_ANONYMIZED));

        assertThat(externalResult.getResultStatus()).isEqualTo(RecursiveResultStatus.COMPLETE);
        assertThat(externalResult.getUseCase())
                .isEqualTo(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE);
        assertThat(externalResult.getBomLifecycle()).isEqualTo(BomLifecycle.AS_PLANNED);
        assertThat(externalResult.getRequestedAspects()).containsExactly(
                ITEM_STOCK_ANONYMIZED, DELIVERY_INFORMATION_ANONYMIZED);
        assertThat(externalResult.getChildItems()).singleElement().satisfies(child -> {
            assertThat(child.getMaterialNumber()).isEqualTo("MNR-1");
            assertThat(child.getQuantity().getValue()).isEqualTo(2.0);
            assertThat(child.getItems()).extracting(RecursiveAspectItem::getAspect)
                                        .containsExactly(ITEM_STOCK_ANONYMIZED,
                                                DELIVERY_INFORMATION_ANONYMIZED);
            assertThat(child.getItems().get(0).getItems()).containsEntry("quantity", 10);
            assertThat(child.getItems().get(1).getItems()).containsEntry("quantity", 30);
        });
    }

    @Test
    void shouldKeepInternalChildBranchDetailsOutOfExternalStatusJson() throws Exception {
        final String filteredPartner = "BPNL0000CERS0001";
        final String filteredAssetId = "urn:uuid:33333333-3333-3333-3333-333333333333";
        final String childMessageId = "child-request-message-id";
        final RecursiveJobState state = RecursiveJobState.builder()
                .jobId("job-1")
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED))
                .requesterBpnl("BPNL0000ATLS0001")
                .messageId("parent-message-id")
                .createdOn(ZonedDateTime.parse("2026-08-10T10:00:00Z"))
                .lastModifiedOn(ZonedDateTime.parse("2026-08-10T10:01:00Z"))
                .deadline(ZonedDateTime.parse("2026-08-10T10:30:00Z"))
                .childResponseDeadline(ZonedDateTime.parse("2026-08-10T10:29:00Z"))
                .state(RecursiveJobPhase.COMPLETED)
                .rootJob(true)
                .bomChildren(List.of(new RecursiveBomChild(filteredAssetId, filteredPartner,
                        RecursiveQuantity.builder().value(1.0).unit("unit:piece").build())))
                .childBranches(List.of(RecursiveChildBranch.builder()
                        .messageId(childMessageId)
                        .partnerBpnl(filteredPartner)
                        .childGlobalAssetId(filteredAssetId)
                        .status(RecursiveResponseStatus.COMPLETED)
                        .build()))
                .result(RecursiveJobResult.builder()
                        .resultStatus(RecursiveResultStatus.COMPLETE)
                        .childItems(List.of())
                        .tombstones(List.of())
                        .build())
                .build();

        final RecursiveJobStatusResponse externalStatus = RecursiveResponseMapper.toStatusResponse(state);
        final String externalJson = new ObjectMapper().findAndRegisterModules().writeValueAsString(externalStatus);

        assertThat(externalJson).doesNotContain(filteredPartner)
                                .doesNotContain(filteredAssetId)
                                .doesNotContain(childMessageId)
                                .doesNotContain("childBranches")
                                .doesNotContain("bomChildren");
    }

    @Test
    void shouldDiscardPayloadsForLifecycleOutsideUseCasePolicy() {
        final RecursiveJobResult externalResult = RecursiveResponseMapper.toExternalResult(
                RecursiveJobResult.builder()
                        .childItems(List.of(RecursiveChildItem.builder()
                                .items(List.of(aspectItem(ITEM_STOCK_ANONYMIZED, Map.of("quantity", 10))))
                                .tombstones(List.of())
                                .childItems(List.of())
                                .build()))
                        .tombstones(List.of())
                        .build(),
                RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE,
                BomLifecycle.AS_SPECIFIED,
                List.of(ITEM_STOCK_ANONYMIZED));

        assertThat(externalResult.getChildItems()).isEmpty();
        assertThat(externalResult.getResultStatus()).isEqualTo(RecursiveResultStatus.FAILED);
        assertThat(externalResult.getUseCase()).isNull();
        assertThat(externalResult.getBomLifecycle()).isNull();
        assertThat(externalResult.getRequestedAspects()).isEmpty();
    }

    private RecursiveAspectItem aspectItem(final String aspect, final Map<String, Object> payload) {
        return RecursiveAspectItem.builder().aspect(aspect).items(payload).build();
    }
}
