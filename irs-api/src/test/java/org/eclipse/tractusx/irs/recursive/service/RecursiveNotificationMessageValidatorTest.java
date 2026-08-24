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
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneScope;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveNotificationMessageValidatorTest {

    private static final RecursiveUseCase USE_CASE = RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String ASPECT = RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String DELIVERY_ASPECT = RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ValidatorFactory validatorFactory;
    private RecursiveNotificationMessageValidator messageValidator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        messageValidator = new RecursiveNotificationMessageValidator(validatorFactory.getValidator(), objectMapper);
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void acceptsCurrentRequestContract() {
        assertThat(messageValidator.isValid(request())).isTrue();
    }

    @Test
    void acceptsRequestWithPlainUuidGlobalAssetId() {
        final JsonNode payload = objectMapper.valueToTree(request());
        ((ObjectNode) payload.get("content")).put("globalAssetId",
                "68904173-ad59-4a77-8412-3e73fcafbd8b");
        final RecursiveNotificationMessage decoded = messageValidator.decode(payload).orElseThrow();

        assertThat(messageValidator.isValid(decoded)).isTrue();
    }

    @Test
    void acceptsCurrentResponseContract() {
        assertThat(messageValidator.isValid(response(result(USE_CASE)))).isTrue();
    }

    @Test
    void rejectsResponseWhoseResultDoesNotMatchTheEnvelope() {
        assertThat(messageValidator.isValid(response(result(USE_CASE, List.of(DELIVERY_ASPECT))))).isFalse();
    }

    @Test
    void rejectsMessageWhenStrictDecodingFails() {
        final JsonNode payload = objectMapper.valueToTree(response(result(USE_CASE)));
        ((ObjectNode) payload.get("content")).put("status", "UNKNOWN_STATUS");
        ((ObjectNode) payload.get("header")).remove("senderBpn");

        assertThat(messageValidator.validate(payload)).isEmpty();
    }

    @Test
    void acceptsUnknownHeaderExtension() {
        final JsonNode payload = objectMapper.valueToTree(request());
        ((ObjectNode) payload.get("header")).put("traceId", "trace-42");

        assertThat(messageValidator.decode(payload)).isPresent();
    }

    @Test
    void rejectsUnknownContentProperty() {
        final JsonNode payload = objectMapper.valueToTree(request());
        ((ObjectNode) payload.get("content")).put("unexpected", "value");

        assertThat(messageValidator.decode(payload)).isEmpty();
    }

    @Test
    void rejectsUnsupportedContextVersion() {
        final JsonNode payload = objectMapper.valueToTree(request());
        ((ObjectNode) payload.get("header")).put("context",
                "IndustryCore-RecursiveIrsNotificationApi-Receive:1.1.0");
        final RecursiveNotificationMessage decoded = messageValidator.decode(payload).orElseThrow();

        assertThat(messageValidator.isValid(decoded)).isFalse();
    }

    @Test
    void rejectsResponseWithoutBomLifecycle() {
        final JsonNode payload = objectMapper.valueToTree(response(result(USE_CASE)));
        ((ObjectNode) payload.get("content")).remove("bomLifecycle");
        final RecursiveNotificationMessage decoded = messageValidator.decode(payload).orElseThrow();

        assertThat(messageValidator.isValid(decoded)).isFalse();
    }

    @Test
    void rejectsTimestampWithRegionZoneId() {
        final JsonNode payload = objectMapper.valueToTree(request());
        ((ObjectNode) payload.get("header")).put("sentDateTime",
                "2026-08-06T10:00:00+02:00[Europe/Berlin]");
        final RecursiveNotificationMessage decoded = messageValidator.decode(payload).orElseThrow();

        assertThat(messageValidator.isValid(decoded)).isFalse();
    }

    @Test
    void acceptsRequestedAspectsInDifferentOrder() {
        final List<String> envelopeAspects = List.of(ASPECT, DELIVERY_ASPECT);
        final RecursiveJobResult result = result(USE_CASE, List.of(DELIVERY_ASPECT, ASPECT));

        assertThat(messageValidator.isValid(response(result, envelopeAspects))).isTrue();
    }

    @Test
    void rejectsDuplicateAspectItemsPerMaterialNode() {
        final RecursiveJobResult result = RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.COMPLETE)
                .useCase(USE_CASE)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .requestedAspects(List.of(ASPECT))
                .childItems(List.of(RecursiveChildItem.builder()
                        .materialNumber("MNR-1000")
                        .items(List.of(aspectItem(ASPECT), aspectItem(ASPECT)))
                        .tombstones(List.of())
                        .childItems(List.of())
                        .build()))
                .tombstones(List.of())
                .build();

        assertThat(messageValidator.isValid(response(result))).isFalse();
    }

    @Test
    void rejectsTombstoneWithoutReason() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().reason(null).build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithoutScope() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().scope(null).build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithoutType() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().type(null).build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithoutUsefulDetail() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().detail(" ").build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithUnsafeDetail() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().detail("first line\nsecond line").build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithRetryableMismatch() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().retryable(false).build())))).isFalse();
    }

    @Test
    void rejectsTombstoneWithInvalidErrorRef() {
        assertThat(messageValidator.isValid(response(resultWithNodeTombstone(
                validTombstone().errorRefs(List.of("not-a-uuid")).build())))).isFalse();
    }

    private RecursiveNotificationMessage request() {
        return RecursiveNotificationMessage.builder()
                .header(requestHeader())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(USE_CASE)
                        .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(ASPECT))
                        .build())
                .build();
    }

    private RecursiveNotificationMessage response(final RecursiveJobResult result) {
        return response(result, List.of(ASPECT));
    }

    private RecursiveNotificationMessage response(final RecursiveJobResult result, final List<String> aspects) {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId(UUID.randomUUID().toString())
                        .relatedMessageId(UUID.randomUUID().toString())
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime("2026-08-06T08:01:00Z")
                        .senderBpnl("BPNL0000BELF0001")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.RESPONSE)
                        .openingId("opening-42")
                        .useCase(USE_CASE)
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(aspects)
                        .status(RecursiveResponseStatus.COMPLETED)
                        .result(result)
                        .build())
                .build();
    }

    private RecursiveJobResult result(final RecursiveUseCase useCase) {
        return result(useCase, List.of(ASPECT));
    }

    private RecursiveJobResult result(final RecursiveUseCase useCase, final List<String> requestedAspects) {
        return RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.COMPLETE)
                .useCase(useCase)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .requestedAspects(requestedAspects)
                .childItems(List.of(RecursiveChildItem.builder()
                        .materialNumber("MNR-1000")
                        .items(List.of())
                        .tombstones(List.of())
                        .childItems(List.of())
                        .build()))
                .tombstones(List.of())
                .build();
    }

    private RecursiveJobResult resultWithNodeTombstone(final RecursiveTombstone tombstone) {
        return RecursiveJobResult.builder()
                .resultStatus(RecursiveResultStatus.PARTIAL)
                .useCase(USE_CASE)
                .bomLifecycle(BomLifecycle.AS_PLANNED)
                .requestedAspects(List.of(ASPECT))
                .childItems(List.of(RecursiveChildItem.builder()
                        .materialNumber("MNR-1000")
                        .items(List.of())
                        .tombstones(List.of(tombstone))
                        .childItems(List.of())
                        .build()))
                .tombstones(List.of())
                .build();
    }

    private RecursiveAspectItem aspectItem(final String aspect) {
        return RecursiveAspectItem.builder()
                .aspect(aspect)
                .items(Map.of("value", "available"))
                .build();
    }

    private RecursiveTombstone.RecursiveTombstoneBuilder validTombstone() {
        return RecursiveTombstone.builder()
                .type(RecursiveTombstoneType.RECURSIVE_TOMBSTONE)
                .scope(RecursiveTombstoneScope.CHILD_BRANCH)
                .aspects(List.of(ASPECT))
                .reason(RecursiveTombstoneReason.CHILD_BRANCH_FAILED)
                .retryable(RecursiveTombstoneReason.CHILD_BRANCH_FAILED.isRetryable())
                .detail("A recursive child branch failed.")
                .occurrences(1)
                .errorRefs(List.of(UUID.randomUUID().toString()));
    }

    private RecursiveNotificationMessage.Header requestHeader() {
        return RecursiveNotificationMessage.Header.builder()
                .messageId(UUID.randomUUID().toString())
                .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                .sentDateTime("2026-08-06T08:00:00Z")
                .senderBpnl("BPNL0000ATLS0001")
                .receiverBpnl("BPNL0000BELF0001")
                .expectedResponseBy("2026-08-06T08:10:00Z")
                .version(RecursiveNotificationMessage.HEADER_VERSION)
                .build();
    }
}
