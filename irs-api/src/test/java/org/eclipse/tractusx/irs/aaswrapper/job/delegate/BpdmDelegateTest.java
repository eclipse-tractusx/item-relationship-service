/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterCollectBpns;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class BpdmDelegateTest {

    final BpdmFacade bpdmFacade = mock(BpdmFacade.class);
    final BpdmDelegate bpdmDelegate = new BpdmDelegate(null, bpdmFacade);

    @Test
    void shouldFillItemContainerWithBpn() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getBpns().stream().findFirst().get().getManufacturerName()).isEqualTo("Tier A");
    }

    private static PartChainIdentificationKey createKey() {
        return PartChainIdentificationKey.builder().globalAssetId("itemId").build();
    }

    @Test
    void shouldCreateTombstoneForNotValidBpn() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("dsfvzsdfvzsvdszvzdsvdszvdszvds"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.BPDM_VALIDATION);
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() {
        // given
        when(bpdmFacade.findManufacturerName(any())).thenThrow(RestClientException.class);
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainerWithShell, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.BPDM_REQUEST);
    }

    @Test
    void shouldCreateTombstoneForMissingBpnForGivenManufacturerId() {
        // given
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.BPDM_REQUEST);
    }

    @Test
    void shouldNotResolveBPNsWithoutFlag() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameter(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getMetrics()).isEmpty();
    }

    @Test
    void shouldResolveBPNsWhenFlagIsTrue() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getBpns().stream().findFirst().get().getManufacturerName()).isEqualTo("Tier A");
        assertThat(result.getMetrics()).isNotEmpty();
        assertThat(result.getMetrics().stream().findFirst().get().getCompleted()).isOne();
    }

    @Test
    void shouldIncrementFailedMetricWhenFacadeResultIsEmpty() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.empty());
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getMetrics()).isNotEmpty();
        assertThat(result.getMetrics().stream().findFirst().get().getFailed()).isOne();
        assertThat(result.getMetrics().stream().findFirst().get().getCompleted()).isZero();
    }

    @Test
    void shouldIncrementFailedMetricWhenExceptionIsThrown() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willThrow(new RestClientException("test"));
        final ItemContainer.ItemContainerBuilder itemContainer = ItemContainer.builder().bpn(Bpn.withManufacturerId("BPNL00000003AYRE"));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainer, jobParameterCollectBpns(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getMetrics()).isNotEmpty();
        assertThat(result.getMetrics().stream().findFirst().get().getFailed()).isOne();
        assertThat(result.getMetrics().stream().findFirst().get().getCompleted()).isZero();
    }
}
