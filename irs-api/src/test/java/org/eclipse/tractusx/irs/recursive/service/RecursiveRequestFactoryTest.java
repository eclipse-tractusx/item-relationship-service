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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RecursiveRequestFactoryTest {

    private static final String GLOBAL_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String LOCAL_BPNL = "BPNL0000LOCAL001";
    private static final ZonedDateTime NOW = ZonedDateTime.parse("2026-01-01T00:00:00Z");
    private static final RecursiveUseCase USE_CASE = RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String DELIVERY_INFORMATION_ANONYMIZED =
            RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();

    private RecursiveProperties properties;
    private RecursiveRequestFactory factory;

    @BeforeEach
    void setUp() {
        properties = new RecursiveProperties();
        properties.setLocalBpnl(LOCAL_BPNL);
        properties.getTimeout().setDefaultJobTtl(Duration.ofMinutes(45));
        factory = new RecursiveRequestFactory(properties, Clock.fixed(NOW.toInstant(), NOW.getZone()));
    }

    @Test
    void rootJobUsesConfiguredDefaultWhenTtlIsMissing() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared = factory.prepare(request, true, null, null);

        assertThat(prepared.createdOn()).isEqualTo(NOW);
        assertThat(prepared.deadline()).isEqualTo(NOW.plusMinutes(45));
        assertThat(prepared.childResponseDeadline()).isEqualTo(NOW.plusMinutes(44));
    }

    @ParameterizedTest
    @ValueSource(strings = { "PT15M", "PT1S", "PT0.000000001S" })
    void rootJobAcceptsPositiveTtl(final String ttl) {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .ttl(ttl)
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared = factory.prepare(request, true, null, null);

        assertThat(prepared.deadline()).isEqualTo(NOW.plus(Duration.parse(ttl)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "PT3H", "PT1000000000000000H" })
    void rootJobCapsLargeTtlBeforeCalculatingDeadline(final String ttl) {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .ttl(ttl)
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared = factory.prepare(request, true, null, null);

        assertThat(prepared.deadline()).isEqualTo(NOW.plusHours(2));
        assertThat(prepared.childResponseDeadline()).isEqualTo(NOW.plusHours(2).minusMinutes(1));
    }

    @Test
    void rootJobRejectsDefaultTtlBeyondSupportedDeadlineRange() {
        final Duration oversizedTtl = Duration.parse("PT1000000000000000H");
        properties.getTimeout().setDefaultJobTtl(oversizedTtl);
        properties.getTimeout().setMaxJobTtl(oversizedTtl);
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ttl exceeds the supported deadline range")
                .hasCauseInstanceOf(DateTimeException.class);
    }

    @Test
    void shortTtlUsesUnbufferedChildDeadline() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .ttl("PT0.000000001S")
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared = factory.prepare(request, true, null, null);

        assertThat(prepared.childResponseDeadline()).isEqualTo(prepared.deadline());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\t", "not-a-duration", "PT0S", "-PT1S",
            "PT999999999999999999999999999999999S" })
    void rootJobRejectsInvalidTtl(final String ttl) {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .ttl(ttl)
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ttl must be a positive ISO-8601 duration");
    }

    @Test
    void rootJobBindsRequesterToLocalBpnlAndIgnoresBody() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .requesterBpn("BPNL0000SPOOFED9")
                .ttl("PT15M")
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, true, null, null);

        assertThat(prepared.requesterBpnl()).isEqualTo(LOCAL_BPNL);
    }

    @Test
    void childRequestKeepsParentRequesterBpn() {
        final ZonedDateTime inheritedDeadline = NOW.plusMinutes(10);
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .requesterBpn("BPNL0000PARENT01")
                .ttl("not-a-duration")
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, false, inheritedDeadline, "parent-msg-id");

        assertThat(prepared.requesterBpnl()).isEqualTo("BPNL0000PARENT01");
        assertThat(prepared.deadline()).isEqualTo(inheritedDeadline);
        assertThat(prepared.childResponseDeadline()).isEqualTo(inheritedDeadline.minusMinutes(1));
    }

    @Test
    void usesFullAspectBundleWhenRequestDoesNotSelectAspects() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, true, null, null);

        assertThat(prepared.aspects()).containsExactlyElementsOf(
                RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE.getAspectSemanticIds());
        assertThat(prepared.bomLifecycle()).isEqualTo(BomLifecycle.AS_PLANNED);
    }

    @Test
    void canonicalizesPlainUuidGlobalAssetId() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, true, null, null);

        assertThat(prepared.globalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
    }

    @Test
    void usesOnlyExplicitSubsetFromUseCaseBundle() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .aspects(List.of(ITEM_STOCK_ANONYMIZED, DELIVERY_INFORMATION_ANONYMIZED))
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, true, null, null);

        assertThat(prepared.aspects()).containsExactly(
                ITEM_STOCK_ANONYMIZED,
                DELIVERY_INFORMATION_ANONYMIZED);
    }

    @Test
    void rejectsAsBuiltLifecycleForPuris() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .bomLifecycle(BomLifecycle.AS_BUILT)
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bomLifecycle asBuilt is not supported");
    }

    @Test
    void rejectsLifecycleOutsideUseCaseBundle() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .bomLifecycle(BomLifecycle.AS_SPECIFIED)
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bomLifecycle asSpecified is not supported");
    }

    @Test
    void rejectsMissingUseCaseInsteadOfUsingFallbacks() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(null)
                .globalAssetId(GLOBAL_ASSET_ID)
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("useCase must be provided");
    }

    @Test
    void rejectsAspectOutsideUseCaseBundle() {
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .aspects(List.of("urn:samm:io.catenax.unknown:1.0.0#Unknown"))
                .build();

        assertThatThrownBy(() -> factory.prepare(request, true, null, null))
                .isInstanceOf(RecursiveUnsupportedAspectException.class);
    }

}
