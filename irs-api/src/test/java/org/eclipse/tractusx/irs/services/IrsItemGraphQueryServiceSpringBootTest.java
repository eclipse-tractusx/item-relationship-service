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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.eclipse.tractusx.irs.util.TestMother.EXISTING_GLOBAL_ASSET_ID;
import static org.eclipse.tractusx.irs.util.TestMother.PRODUCT_DESCRIPTION_1_0_1;
import static org.eclipse.tractusx.irs.util.TestMother.registerJob;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspect;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDepthAndAspectAndCollectAspects;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithDirection;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepth;
import static org.eclipse.tractusx.irs.util.TestMother.SERIAL_PART_3_0_0;
import static org.eclipse.tractusx.irs.util.TestMother.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.semanticshub.AspectModel;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JobMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test", "stubtest", "local" })
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

    @MockBean
    private SemanticsHubFacade semanticsHubFacade;

    @MockBean
    private ConnectorEndpointsService connectorEndpointsService;

    @BeforeEach
    void setUp() throws SchemaNotFoundException {
        final List<AspectModel> models = List.of(
                getAspectModel(AspectType.SERIAL_PART.toString(), SERIAL_PART_3_0_0),
                getAspectModel(AspectType.PRODUCT_DESCRIPTION.toString(), PRODUCT_DESCRIPTION_1_0_1),
                getAspectModel(AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString(), SINGLE_LEVEL_BOM_AS_BUILT_3_0_0));
        final AspectModels aspectModels = new AspectModels(models, "2023-02-13T08:18:11.990659500Z");
        when(semanticsHubFacade.getAllAspectModels()).thenReturn(aspectModels);
    }

    @Test
    void registerJobWithoutDepthShouldBuildFullTree() {
        // given
        final RegisterJob registerJob = registerJobWithoutDepth();
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("http://localhost/discovery"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void registerJobWithCollectAspectsShouldIncludeSubmodels() throws InvalidSchemaException {
        // given
        when(jsonValidatorService.validate(any(), any())).thenReturn(ValidationResult.builder().valid(true).build());
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("https://connector.endpoint.nl"));
        final RegisterJob registerJob = registerJob(EXISTING_GLOBAL_ASSET_ID, 100,
                List.of(SERIAL_PART_3_0_0, PRODUCT_DESCRIPTION_1_0_1, SINGLE_LEVEL_BOM_AS_BUILT_3_0_0),
                true, false, Direction.DOWNWARD);
        when(connectorEndpointsService.fetchConnectorEndpoints(registerJob.getKey().getBpn())).thenReturn(
                List.of("singleLevelBomAsBuilt"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getSubmodelsSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void registerJobShouldCreateTombstonesWhenNotPassingJsonSchemaValidation() throws InvalidSchemaException {
        // given
        when(jsonValidatorService.validate(any(), any())).thenReturn(ValidationResult.builder().valid(false).build());
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("https://connector.endpoint.nl"));

        final RegisterJob registerJob = registerJobWithDepthAndAspectAndCollectAspects(3,
                List.of(SINGLE_LEVEL_BOM_AS_BUILT_3_0_0));
        when(connectorEndpointsService.fetchConnectorEndpoints(registerJob.getKey().getBpn())).thenReturn(
                List.of("singleLevelBomAsBuilt"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getTombstonesSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void registerJobWithDepthShouldBuildTreeUntilGivenDepth() {
        // given
        final RegisterJob registerJob = registerJobWithDepthAndAspect(1, List.of());
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("http://localhost/discovery"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void registerJobWithUpwardDirectionShouldBuildRelationships() {
        // given
        final RegisterJob registerJob = registerJobWithDirection(EXISTING_GLOBAL_ASSET_ID,
                Direction.UPWARD);
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("http://localhost/discovery"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void cancelJobById() {
        final String idAsString = String.valueOf(jobId);
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .job(Job.builder()
                                                                          .id(UUID.fromString(idAsString))
                                                                          .state(JobState.UNSAVED)
                                                                          .parameter(JobParameter.builder()
                                                                                                 .callbackUrl(
                                                                                                         "example.com")
                                                                                                 .build())
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

        final JobState state = fetchedJob.get().getJob().getState();
        assertThat(state).isEqualTo(JobState.CANCELED);

        final ZonedDateTime lastModifiedOn = fetchedJob.get().getJob().getLastModifiedOn();
        assertThat(lastModifiedOn).isNotNull().isBeforeOrEqualTo(ZonedDateTime.now());
    }

    @Test
    void registerJobWithoutAspectsShouldUseDefault() {
        // given
        final RegisterJob registerJob = registerJobWithDepthAndAspect(10, List.of());
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("http://localhost/discovery"));

        // when
        final JobHandle jobHandle = service.registerItemJob(registerJob);
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobHandle.toString());

        // then
        assertThat(multiTransferJob).isPresent();
        assertThat(multiTransferJob.get().getJobParameter().getAspects()).isEmpty();
        assertThat(multiTransferJob.get().getJobParameter().isCollectAspects()).isFalse();
    }

    @Test
    void registerJobWithUpwardDirectionAsPlannedShouldBuildRelationships() {
        // given
        final RegisterJob registerJob = registerJobWithDirection("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97",
                Direction.UPWARD);
        registerJob.setBomLifecycle(BomLifecycle.AS_PLANNED);

        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("http://localhost/discovery"));

        // when
        final JobHandle registeredJob = service.registerItemJob(registerJob);

        // then
        given().ignoreException(ResponseStatusException.class)
               .await()
               .atMost(5, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getId()), greaterThan(0));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForLifecycleAsSpecifiedAndDirectionUpward() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setKey(PartChainIdentificationKey.builder().globalAssetId(UUID.randomUUID().toString()).build());
        registerJob.setDirection(Direction.UPWARD);
        registerJob.setBomLifecycle(BomLifecycle.AS_SPECIFIED);

        assertThrows(IllegalArgumentException.class, () -> service.registerItemJob(registerJob));
    }

    private int getRelationshipsSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false).getRelationships().size();
    }

    private int getSubmodelsSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false).getSubmodels().size();
    }

    @Test
    void checkMetricsRecordingTest() {
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

    private static AspectModel getAspectModel(final String aspect, final String urn) {
        return AspectModel.builder().name(aspect).urn(urn).build();
    }

}