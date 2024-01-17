/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.util.TestMother;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MeterRegistryServiceTest {

    static MeterRegistryService meterRegistryService; // = TestMother.fakeMeterRegistryService();

    @BeforeAll
    static void setup() {
        meterRegistryService = TestMother.simpleMeterRegistryService();
    }

    @Test
    void checkJobStateMetricsIfCorrectlyIncremented() {
        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.TRANSFERS_FINISHED);
        assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.COMPLETED);
        assertThat(meterRegistryService.getJobMetric().getJobSuccessful().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(2);

        meterRegistryService.recordJobStateMetric(JobState.ERROR);
        assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.CANCELED);
        assertThat(meterRegistryService.getJobMetric().getJobCancelled().count()).isEqualTo(1);

        meterRegistryService.setNumberOfJobsInJobStore(10L);
        assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(10);

        meterRegistryService.setNumberOfJobsInJobStore(9L);
        meterRegistryService.setNumberOfJobsInJobStore(6L);
        assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(6);

    }

}