/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
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
import static org.awaitility.Awaitility.given;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspect;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspectAndCollectAspects;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepth;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobErrorDetails;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.exceptions.EntityNotFoundException;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JobMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test", "stubtest" })
@Import(TestConfig.class)
class IrsItemGraphQueryServiceSpringBootTest {

    private final UUID jobId = UUID.randomUUID();

    @Autowired
    private JobStore jobStore;

    @Autowired
    private IrsItemGraphQueryService service;

    @Autowired
    private MeterRegistryService meterRegistryService;

    @MockBean
    private JsonValidatorService jsonValidatorService;

    @Test
    void registerJobWithoutDepthShouldBuildFullTree() {
        // given
        final RegisterJob registerJob = registerJobWithoutDepth();
        final int expectedRelationshipsSizeFullTree = 1; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getJobId()), equalTo(expectedRelationshipsSizeFullTree));
    }

    @Test
    void registerJobWithCollectAspectsShouldIncludeSubmodels() throws InvalidSchemaException {
        // given
        when(jsonValidatorService.validate(any(), any())).thenReturn(ValidationResult.builder().valid(true).build());
        final RegisterJob registerJob = registerJobWithDepthAndAspectAndCollectAspects(3,
                List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP));
        final int expectedRelationshipsSizeFullTree = 1; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getSubmodelsSize(registeredJob.getJobId()), equalTo(expectedRelationshipsSizeFullTree));
    }

    @Test
    void registerJobShouldCreateTombstonesWhenNotPassingJsonSchemaValidation() throws InvalidSchemaException {
        // given
        when(jsonValidatorService.validate(any(), any())).thenReturn(ValidationResult.builder().valid(false).build());
        final RegisterJob registerJob = registerJobWithDepthAndAspectAndCollectAspects(3,
                List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP));
        final int expectedTombstonesSizeFullTree = 1; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getTombstonesSize(registeredJob.getJobId()), equalTo(expectedTombstonesSizeFullTree));
    }

    @Test
    void registerJobWithDepthShouldBuildTreeUntilGivenDepth() {
        // given
        final RegisterJob registerJob = registerJobWithDepthAndAspect(0, null);
        final int expectedRelationshipsSizeFirstDepth = 1; // stub

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(EntityNotFoundException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getJobId()),
                       equalTo(expectedRelationshipsSizeFirstDepth));
    }

    @Test
    void cancelJobById() {
        final String idAsString = String.valueOf(jobId);
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .job(Job.builder()
                                                                          .jobId(UUID.fromString(idAsString))
                                                                          .jobState(JobState.UNSAVED)
                                                                          .exception(JobErrorDetails.builder()
                                                                                                    .errorDetail(
                                                                                                            "Job should be canceled")
                                                                                                    .exceptionDate(
                                                                                                            ZonedDateTime.now())
                                                                                                    .build())
                                                                          .build())
                                                                  .build();

        jobStore.create(multiTransferJob);

        assertThat(service.cancelJobById(jobId)).isNotNull();

        final Optional<MultiTransferJob> fetchedJob = jobStore.find(idAsString);
        assertThat(fetchedJob).isNotEmpty();

        final JobState state = fetchedJob.get().getJob().getJobState();
        assertThat(state).isEqualTo(JobState.CANCELED);

        final ZonedDateTime lastModifiedOn = fetchedJob.get().getJob().getLastModifiedOn();
        assertThat(lastModifiedOn).isNotNull().isBefore(ZonedDateTime.now());
    }

    @Test
    void registerJobWithoutAspectsShouldUseDefault() {
        // given
        final AspectType defaultAspectType = AspectType.SERIAL_PART_TYPIZATION;
        final List<AspectType> emptyAspectTypeFilterList = List.of();
        final RegisterJob registerJob = registerJobWithDepthAndAspect(null, emptyAspectTypeFilterList);

        // when
        final JobHandle jobHandle = service.registerItemJob(registerJob);
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobHandle.toString());

        // then
        assertThat(multiTransferJob).isPresent();
        assertThat(multiTransferJob.get().getJobParameter().getAspects()).contains(defaultAspectType);
        assertThat(multiTransferJob.get().getJobParameter().isCollectAspects()).isFalse();
    }

    private int getRelationshipsSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false).getRelationships().size();
    }

    private int getSubmodelsSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false).getSubmodels().size();
    }

    @Test
    public void checkMetricsRecordingTest() {
        meterRegistryService.incrementJobFailed();
        meterRegistryService.incrementJobRunning();
        meterRegistryService.incrementJobSuccessful();
        meterRegistryService.incrementJobCancelled();
        meterRegistryService.incrementJobProcessed();
        meterRegistryService.setNumberOfJobsInJobStore(7L);
        meterRegistryService.setNumberOfJobsInJobStore(12L);
        meterRegistryService.setNumberOfJobsInJobStore(5L);

        JobMetrics metrics = meterRegistryService.getJobMetric();

        assertThat(metrics.getJobFailed().count()).isEqualTo(1.0);
        assertThat(metrics.getJobRunning().count()).isEqualTo(1.0);
        assertThat(metrics.getJobSuccessful().count()).isEqualTo(1.0);
        assertThat(metrics.getJobCancelled().count()).isEqualTo(1.0);
        assertThat(metrics.getJobProcessed().count()).isEqualTo(1.0);
        assertThat(metrics.getJobInJobStore().value()).isEqualTo(5.0);
    }

    private int getTombstonesSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false).getTombstones().size();
    }

}