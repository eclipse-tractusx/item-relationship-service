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
import static org.eclipse.tractusx.irs.util.TestMother.shell;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutHref;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class DigitalTwinDelegateTest {

    final DigitalTwinRegistryService digitalTwinRegistryService = mock(DigitalTwinRegistryService.class);
    final DigitalTwinDelegate digitalTwinDelegate = new DigitalTwinDelegate(null, digitalTwinRegistryService);

    @Test
    void shouldFillItemContainerWithShell() throws RegistryServiceException {
        // given
        when(digitalTwinRegistryService.fetchShells(any())).thenReturn(
                List.of(shell("", shellDescriptor(List.of(submodelDescriptorWithoutHref("any"))))));

        // when
        final ItemContainer result = digitalTwinDelegate.process(ItemContainer.builder(), jobParameter(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells()).isNotEmpty();
        assertThat(result.getShells().get(0).payload().getSubmodelDescriptors()).isNotEmpty();
    }

    @Test
    void shouldFillItemContainerWithShellAndFilteredSubmodelDescriptorsWhenDepthReached() throws RegistryServiceException {
        // given
        when(digitalTwinRegistryService.fetchShells(any())).thenReturn(
                List.of(shell("", shellDescriptor(List.of(submodelDescriptorWithoutHref("any"))))));
        final JobParameter jobParameter = JobParameter.builder().depth(1).aspects(List.of()).build();

        // when
        final ItemContainer result = digitalTwinDelegate.process(ItemContainer.builder(), jobParameter,
                new AASTransferProcess("id", 1), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells()).isNotEmpty();
        assertThat(result.getShells().get(0).payload().getSubmodelDescriptors()).isEmpty();
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() throws RegistryServiceException {
        // given
        when(digitalTwinRegistryService.fetchShells(any())).thenThrow(
                new RestClientException("Unable to call endpoint"));

        // when
        final ItemContainer result = digitalTwinDelegate.process(ItemContainer.builder(), jobParameter(),
                new AASTransferProcess("id", 0), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getRetryCounter()).isEqualTo(
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts());
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.DIGITAL_TWIN_REQUEST);
    }

    @Test
    void shouldCreateTombstoneIfBPNEmpty() {
        // when
        final ItemContainer result = digitalTwinDelegate.process(ItemContainer.builder(), jobParameter(),
                new AASTransferProcess("id", 0), createKeyWithoutBpn());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getErrorDetail()).isEqualTo("Can't get relationship without a BPN");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.DIGITAL_TWIN_REQUEST);
    }

    private static PartChainIdentificationKey createKey() {
        return PartChainIdentificationKey.builder().globalAssetId("itemId").bpn("bpn123").build();
    }
    private static PartChainIdentificationKey createKeyWithoutBpn() {
        return PartChainIdentificationKey.builder().globalAssetId("itemId").build();
    }
}
