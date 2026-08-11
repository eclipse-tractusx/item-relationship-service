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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveInputValidationTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectLineBreaksInJobRequest() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42\nforged-entry")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .build();

        final Set<ConstraintViolation<RecursiveJobRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("openingId");
    }

    @Test
    void shouldValidateNestedNotificationFields() {
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId("68904173-ad59-4a77-8412-3e73fcafbd8b\rforged-entry")
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime("2026-08-06T08:00:00Z")
                        .senderBpnl("BPNL0000PARENT01")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .expectedResponseBy("2026-08-06T08:10:00Z")
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId()))
                        .build())
                .build();

        final Set<ConstraintViolation<RecursiveNotificationMessage>> violations = validator.validate(message);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("header.messageId");
    }

    @Test
    void shouldAcceptSupportedMessageIdFormats() {
        final RecursiveNotificationMessage plainUuidMessage = notificationWithMessageId(
                "68904173-ad59-4a77-8412-3e73fcafbd8b");
        final RecursiveNotificationMessage urnUuidMessage = notificationWithMessageId(
                "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b");

        assertThat(validator.validate(plainUuidMessage)).isEmpty();
        assertThat(validator.validate(urnUuidMessage)).isEmpty();
    }

    @Test
    void shouldAcceptSupportedGlobalAssetIdFormats() {
        final RecursiveJobRequest plainUuidRequest = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                .build();
        final RecursiveChainOpeningGrant plainUuidGrant = RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn("BPNL0000ATLS0001")
                .build();
        final RecursiveNotificationMessage plainUuidNotification = notificationWithGlobalAssetId(
                "68904173-ad59-4a77-8412-3e73fcafbd8b");

        assertThat(validator.validate(plainUuidRequest)).isEmpty();
        assertThat(validator.validate(plainUuidGrant)).isEmpty();
        assertThat(validator.validate(plainUuidNotification)).isEmpty();
    }

    @Test
    void shouldRejectBlankRelatedMessageIdWhenPresent() {
        final RecursiveNotificationMessage validMessage = notificationWithMessageId(
                "68904173-ad59-4a77-8412-3e73fcafbd8b");
        final RecursiveNotificationMessage message = RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                        .relatedMessageId("")
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime("2026-08-06T08:00:00Z")
                        .senderBpnl("BPNL0000PARENT01")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .expectedResponseBy("2026-08-06T08:10:00Z")
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(validMessage.getContent())
                .build();

        assertThat(validator.validate(message))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("header.relatedMessageId");
    }

    @Test
    void shouldRejectInvalidGrantPartnerBpn() {
        final RecursiveChainOpeningGrant grant = RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .requesterBpn("BPNL0000ATLS0001")
                .allowedBpnlSet(Set.of("BPNL0000CHLD0001\nforged-entry"))
                .build();

        final Set<ConstraintViolation<RecursiveChainOpeningGrant>> violations = validator.validate(grant);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("allowedBpnlSet must contain valid BPNLs");
    }

    private RecursiveNotificationMessage notificationWithMessageId(final String messageId) {
        return notification(messageId, "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b");
    }

    private RecursiveNotificationMessage notificationWithGlobalAssetId(final String globalAssetId) {
        return notification("68904173-ad59-4a77-8412-3e73fcafbd8b", globalAssetId);
    }

    private RecursiveNotificationMessage notification(final String messageId, final String globalAssetId) {
        return RecursiveNotificationMessage.builder()
                .header(RecursiveNotificationMessage.Header.builder()
                        .messageId(messageId)
                        .context(RecursiveNotificationMessage.HEADER_CONTEXT)
                        .sentDateTime("2026-08-06T08:00:00Z")
                        .senderBpnl("BPNL0000PARENT01")
                        .receiverBpnl("BPNL0000ATLS0001")
                        .expectedResponseBy("2026-08-06T08:10:00Z")
                        .version(RecursiveNotificationMessage.HEADER_VERSION)
                        .build())
                .content(RecursiveNotificationMessage.Content.builder()
                        .type(RecursiveNotificationType.REQUEST)
                        .openingId("opening-42")
                        .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                        .globalAssetId(globalAssetId)
                        .bomLifecycle(BomLifecycle.AS_PLANNED)
                        .aspects(List.of(RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId()))
                        .build())
                .build();
    }
}
