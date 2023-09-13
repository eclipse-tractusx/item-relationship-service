/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.ess.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IrsFacadeTest {

    @Mock
    private IrsClient irsClient;

    @InjectMocks
    private IrsFacade irsFacade;

    @Test
    void shouldFetchIrsJobResponse() {
        // given
        final UUID jobId = UUID.randomUUID();
        final Jobs expectedResponse = Jobs.builder()
                                          .job(Job.builder().state(JobState.COMPLETED).id(jobId).build())
                                          .relationships(new ArrayList<>())
                                          .shells(new ArrayList<>())
                                          .build();

        given(irsClient.getJobDetails(jobId.toString())).willReturn(expectedResponse);

        // when
        final Jobs actualResponse = irsFacade.getIrsJob(jobId.toString());

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void shouldStartIrsJobAndReturnJobId() {
        // given
        final UUID jobId = UUID.randomUUID();
        final JobHandle expectedResponse = JobHandle.builder().id(jobId).build();

        given(irsClient.startJob(any())).willReturn(JobHandle.builder().id(jobId).build());

        // when
        final JobHandle actualResponse = irsFacade.startIrsJob(PartChainIdentificationKey.builder()
                                                                                         .globalAssetId(
                                                                                                 UUID.randomUUID()
                                                                                                     .toString())
                                                                                         .bpn("BPNL000000000DD")
                                                                                         .build(),
                BomLifecycle.AS_PLANNED);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}
