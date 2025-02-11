/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.eclipse.tractusx.irs.InMemoryBlobStore;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.DigitalTwinDelegate;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AASTransferProcessManagerTest {

    DigitalTwinDelegate digitalTwinProcessor = mock(DigitalTwinDelegate.class);
    ExecutorService pool = mock(ExecutorService.class);

    final AASTransferProcessManager manager = new AASTransferProcessManager(digitalTwinProcessor, pool,
            new InMemoryBlobStore(), new JsonUtil());

    @Test
    void shouldExecuteThreadForProcessing() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(
                PartChainIdentificationKey.builder().globalAssetId(UUID.randomUUID().toString()).bpn("bpn123").build());

        // when
        manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter(), "jobId");

        // then
        verify(pool, times(1)).execute(any(Runnable.class));
    }

    @Test
    void shouldInitiateProcessingAndReturnOkStatus() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(
                PartChainIdentificationKey.builder().globalAssetId(UUID.randomUUID().toString()).bpn("bpn123").build());

        // when
        final TransferInitiateResponse initiateResponse = manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter(), "jobId");

        // then
        assertThat(initiateResponse.getTransferId()).isNotBlank();
        assertThat(initiateResponse.getStatus()).isEqualTo(ResponseStatus.OK);
    }

}
