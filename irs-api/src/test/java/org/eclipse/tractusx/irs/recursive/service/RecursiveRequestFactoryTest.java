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
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.config.RecursiveProperties;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecursiveRequestFactoryTest {

    private static final String GLOBAL_ASSET_ID = "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b";
    private static final String LOCAL_BPNL = "BPNL0000LOCAL001";
    private static final RecursiveUseCase USE_CASE = RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE;
    private static final String ITEM_STOCK_ANONYMIZED =
            RecursiveAspect.ITEM_STOCK_ANONYMIZED.getSemanticId();
    private static final String DELIVERY_INFORMATION_ANONYMIZED =
            RecursiveAspect.DELIVERY_INFORMATION_ANONYMIZED.getSemanticId();

    private RecursiveRequestFactory factory;

    @BeforeEach
    void setUp() {
        final RecursiveProperties properties = new RecursiveProperties();
        properties.setLocalBpnl(LOCAL_BPNL);
        factory = new RecursiveRequestFactory(properties, Clock.systemUTC());
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
        final RecursiveJobRequest request = RecursiveJobRequest.builder()
                .openingId("opening-42")
                .useCase(USE_CASE)
                .globalAssetId(GLOBAL_ASSET_ID)
                .requesterBpn("BPNL0000PARENT01")
                .build();

        final RecursiveRequestFactory.PreparedRecursiveJob prepared =
                factory.prepare(request, false, ZonedDateTime.now().plusMinutes(10), "parent-msg-id");

        assertThat(prepared.requesterBpnl()).isEqualTo("BPNL0000PARENT01");
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
