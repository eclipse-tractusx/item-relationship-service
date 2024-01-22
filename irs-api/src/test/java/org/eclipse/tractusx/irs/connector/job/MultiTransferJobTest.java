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
package org.eclipse.tractusx.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;

import java.util.Collection;
import java.util.List;

import net.datafaker.Faker;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.util.TestMother;
import org.junit.jupiter.api.Test;

class MultiTransferJobTest {

    final String word = new Faker().lorem().word();

    TestMother generate = new TestMother();

    MultiTransferJob job = generate.job();

    @Test
    void getTransferProcessIds_Immutable() {
        final Collection<String> transferProcessIds = job.getTransferProcessIds();

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> transferProcessIds.add(word));
    }

    @Test
    void getJobData_Immutable() {
        final List<String> aspectTypes = jobParameter().getAspects();
        final String conversionId = AspectType.ID_CONVERSION.toString();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> aspectTypes.add(conversionId));
    }

    @Test
    void letJobTransistFromOneStateToAnother() {
        MultiTransferJob job2 = generate.job(JobState.INITIAL);
        MultiTransferJob newJob = job2.toBuilder().transitionInProgress().build();

        assertThat(newJob.getJob().getState()).isEqualTo(JobState.RUNNING);
    }

}