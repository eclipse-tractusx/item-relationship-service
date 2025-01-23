/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.SemanticModelNames.BATCH_3_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0;
import static org.eclipse.tractusx.irs.WiremockSupport.createEndpointDataReference;
import static org.eclipse.tractusx.irs.WiremockSupport.encodedAssetIds;
import static org.eclipse.tractusx.irs.WiremockSupport.randomUUID;
import static org.eclipse.tractusx.irs.component.enums.AspectType.AspectTypesConstants.BATCH;
import static org.eclipse.tractusx.irs.component.enums.AspectType.AspectTypesConstants.SINGLE_LEVEL_BOM_AS_BUILT;
import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.CONTROLPLANE_PUBLIC_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.EDC_DISCOVERY_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.TEST_BPN;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postDiscoveryFinder200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postDiscoveryFinder404;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.postEdcDiscoveryEmpty200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.LOOKUP_SHELLS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.PUBLIC_LOOKUP_SHELLS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.PUBLIC_SHELL_DESCRIPTORS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.SHELL_DESCRIPTORS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getLookupShells200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.getShellDescriptor200;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_CATALOG;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_EDR_NEGOTIATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_NEGOTIATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_STATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_TRANSFER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.awaitility.Awaitility;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.eclipse.tractusx.irs.connector.batch.PersistentBatchStore;
import org.eclipse.tractusx.irs.edc.client.ContractNegotiationService;
import org.eclipse.tractusx.irs.edc.client.EdcCallbackController;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.edc.client.OngoingNegotiationStorage;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport;
import org.eclipse.tractusx.irs.services.CreationBatchService;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

@WireMockTest(httpPort = 8085)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WireMockTestConfig.class)
@ContextConfiguration(initializers = IrsWireMockIntegrationTest.MinioConfigInitializer.class)
@ActiveProfiles("integrationtest")
class IrsWireMockIntegrationTest {

    private static final String BATCH_PREFIX = "batch:";
    private static final String DSP_PATH = "/api/v1/dsp";
    public static final String SEMANTIC_HUB_URL = "http://semantic.hub/models";
    public static final String EDC_URL = "http://edc.test";

    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

    @Autowired
    private IrsItemGraphQueryService irsService;

    @Autowired
    private CreationBatchService batchService;

    @Autowired
    private SemanticHubService semanticHubService;

    @Autowired
    private EndpointDataReferenceStorage endpointDataReferenceStorage;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PersistentBatchStore persistentBatchStore;

    @Autowired
    @Qualifier(JOB_BLOB_PERSISTENCE)
    private BlobPersistence blobStore;

    @SpyBean
    private OngoingNegotiationStorage ongoingNegotiationStorage;

    @SpyBean
    private ContractNegotiationService contractNegotiationService;

    @Autowired
    private EdcCallbackController edcCallbackController;

    @Autowired
    private EdcConfiguration edcConfiguration;

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
        WiremockSupport.successfulSemanticModelRequest();
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("digitalTwinRegistry.discovery.discoveryFinderUrl", () -> DISCOVERY_FINDER_URL);
        registry.add("digitalTwinRegistry.shellDescriptorTemplate", () -> SHELL_DESCRIPTORS_TEMPLATE);
        registry.add("digitalTwinRegistry.lookupShellsTemplate", () -> LOOKUP_SHELLS_TEMPLATE);
        registry.add("digitalTwinRegistry.type", () -> "decentral");
        registry.add("semanticshub.url", () -> SEMANTIC_HUB_URL);
        registry.add("semanticshub.modelJsonSchemaEndpoint", () -> SemanticHubWireMockSupport.SEMANTIC_HUB_SCHEMA_URL);
        registry.add("semanticshub.defaultUrns", () -> "");
        registry.add("semanticshub.pageSize", () -> 101);
        registry.add("irs-edc-client.controlplane.endpoint.data", () -> EDC_URL);
        registry.add("irs-edc-client.controlplane.endpoint.catalog", () -> PATH_CATALOG);
        registry.add("irs-edc-client.controlplane.endpoint.contract-negotiation", () -> PATH_NEGOTIATE);
        registry.add("irs-edc-client.controlplane.edr-management-enabled", () -> false);
        registry.add("irs-edc-client.controlplane.endpoint.edr-management", () -> PATH_EDR_NEGOTIATE);
        registry.add("irs-edc-client.controlplane.endpoint.transfer-process", () -> PATH_TRANSFER);
        registry.add("irs-edc-client.controlplane.endpoint.state-suffix", () -> PATH_STATE);
        registry.add("irs-edc-client.controlplane.api-key.header", () -> "X-Api-Key");
        registry.add("irs-edc-client.controlplane.api-key.secret", () -> "test");
        registry.add("irs-edc-client.controlplane.orchestration.thread-pool-size", () -> "2");
        registry.add("resilience4j.retry.configs.default.waitDuration", () -> "1s");
    }

    @BeforeEach
    void setUp() {
        edcConfiguration.getControlplane().setEdrManagementEnabled(false);
    }

    @AfterEach
    void tearDown(WireMockRuntimeInfo wmRuntimeInfo) {
        cacheManager.getCacheNames()
                    .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        wmRuntimeInfo.getWireMock().resetMappings();
        endpointDataReferenceStorage.clear();
    }

    @Test
    void shouldStartApplicationAndCollectSemanticModels() throws SchemaNotFoundException {
        // Arrange
        WiremockSupport.successfulSemanticModelRequest();

        // Act
        final AspectModels allAspectModels = semanticHubService.getAllAspectModels();

        // Assert
        assertThat(allAspectModels.models()).hasSize(101);
    }

    @Test
    void shouldStopJobAfterDepthIsReached() {
        // Arrange
        final String globalAssetIdLevel1 = "globalAssetId";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 1);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());

        Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        // Assert
        WiremockSupport.verifyDiscoveryCalls(1);
        WiremockSupport.verifyNegotiationCalls(2);
        WiremockSupport.verifyCatalogCalls(3);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobForJobId.getShells()).hasSize(2);
        assertThat(jobForJobId.getRelationships()).hasSize(1);
        assertThat(jobForJobId.getTombstones()).isEmpty();
    }

    @Test
    void shouldStopJobAfterDepthIsReachedWithEdr() {
        // Arrange
        final String globalAssetIdLevel1 = "globalAssetId";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";
        edcConfiguration.getControlplane().setEdrManagementEnabled(true);

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        successfulEdrRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulEdrRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN,
                "integrationtesting/batch-2.json", "integrationtesting/singleLevelBomAsBuilt-2.json");

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 1);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());

        Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        // Assert
        WiremockSupport.verifyDiscoveryCalls(1);
        WiremockSupport.verifyEdrNegotiationCalls(2);
        WiremockSupport.verifyCatalogCalls(3);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobForJobId.getShells()).hasSize(2);
        assertThat(jobForJobId.getRelationships()).hasSize(1);
        assertThat(jobForJobId.getTombstones()).isEmpty();
    }

    @Test
    void shouldSendOneCallbackAfterJobCompletion() {
        // Arrange
        final String globalAssetIdLevel1 = "globalAssetId";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();
        WiremockSupport.successfulCallbackRequest();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 1,
                WiremockSupport.CALLBACK_URL);

        // Act
        final List<JobHandle> startedJobs = new ArrayList<>();

        // Start 50 jobs in parallel. The bug #755 occurred when multiple (>10 Jobs) were started at the same time.
        // To definitely provoke the cases where callbacks were triggered multiple times, we start 50 jobs.
        final int numberOfParallelJobs = 50;
        for (int i = 0; i < numberOfParallelJobs; i++) {
            startedJobs.add(irsService.registerItemJob(request));
        }

        for (JobHandle jobHandle : startedJobs) {
            assertThat(jobHandle.getId()).isNotNull();
            waitForCompletion(jobHandle.getId());
        }

        // Assert
        for (JobHandle jobHandle : startedJobs) {
            WiremockSupport.verifyCallbackCall(jobHandle.getId().toString(), JobState.COMPLETED, 1);
        }
    }

    @Test
    void shouldCreateTombstoneWhenDiscoveryServiceNotAvailable() {
        // Arrange
        WiremockSupport.successfulSemanticModelRequest();
        stubFor(postDiscoveryFinder404());
        final String globalAssetId = "globalAssetId";
        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 1);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        verify(1, postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(0, postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();

        final List<Tombstone> tombstones = jobForJobId.getTombstones();
        assertThat(tombstones).hasSize(1);
        assertThat(tombstones.get(0).getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(tombstones.get(0).getEndpointURL()).describedAs(
                "Endpoint URL should be empty because discovery not successful").isEmpty();
    }

    @Test
    void shouldCreateTombstoneWhenEdcDiscoveryIsEmpty() {
        // Arrange
        WiremockSupport.successfulSemanticModelRequest();
        stubFor(postDiscoveryFinder200());
        stubFor(postEdcDiscoveryEmpty200());
        final String globalAssetId = "globalAssetId";
        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 1);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        WiremockSupport.verifyDiscoveryCalls(1);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();

        final List<Tombstone> tombstones = jobForJobId.getTombstones();
        assertThat(tombstones).hasSize(1);
        assertThat(tombstones.get(0).getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(tombstones.get(0).getEndpointURL()).describedAs(
                "Endpoint URL should be empty because discovery not successful").isEmpty();
    }

    @Test
    void shouldStartRecursiveProcesses() {
        // Arrange
        final String globalAssetIdLevel1 = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";
        final String globalAssetIdLevel3 = "urn:uuid:a314ad6b-77ea-417e-ae2d-193b3e249e99";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel3, "GenericChemical", TEST_BPN,
                "integrationtesting/batch-3.json", "integrationtesting/singleLevelBomAsBuilt-3.json");

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 4);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobForJobId.getShells()).hasSize(3);
        assertThat(jobForJobId.getRelationships()).hasSize(2);
        assertThat(jobForJobId.getTombstones()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).hasSize(6);

        WiremockSupport.verifyDiscoveryCalls(1);
        // expected 4 negotiations. 3 different submodel assets, 1 dtr
        WiremockSupport.verifyNegotiationCalls(4);
        // expected 6 catalog requests. 3 different submodel assets, 3 times dtr
        WiremockSupport.verifyCatalogCalls(6);
    }

    @Test
    @Disabled("Flaky test")
    void shouldLimitParallelEdcNegotiationsForMultipleJobs() throws EdcClientException {
        // Arrange
        final String globalAssetIdLevel1 = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";
        final String globalAssetIdLevel3 = "urn:uuid:a314ad6b-77ea-417e-ae2d-193b3e249e99";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel3, "GenericChemical", TEST_BPN,
                "integrationtesting/batch-3.json", "integrationtesting/singleLevelBomAsBuilt-3.json");

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 4);

        // Act
        final ArrayList<JobHandle> jobHandles = new ArrayList<>();
        jobHandles.add(irsService.registerItemJob(request));
        jobHandles.add(irsService.registerItemJob(request));
        jobHandles.add(irsService.registerItemJob(request));
        jobHandles.add(irsService.registerItemJob(request));

        // Assert
        for (JobHandle jobHandle : jobHandles) {

            assertThat(jobHandle.getId()).isNotNull();
            waitForCompletion(jobHandle.getId());
            final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

            assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
            assertThat(jobForJobId.getShells()).hasSize(3);
            assertThat(jobForJobId.getRelationships()).hasSize(2);
            assertThat(jobForJobId.getTombstones()).isEmpty();
            assertThat(jobForJobId.getSubmodels()).hasSize(6);
        }

        // expected 4 negotiations. 3 different submodel assets, 1 dtr
        Mockito.verify(contractNegotiationService, times(4)).negotiate(any(), any(), any(), any());
        assertThat(ongoingNegotiationStorage.getOngoingNegotiations()).isEmpty();
        WiremockSupport.verifyNegotiationCalls(4);
        // 3 requests for the submodel assets, 12 for registry assets
        WiremockSupport.verifyCatalogCalls(15);
    }

    @Test
    void shouldCreateDetailedTombstoneForMismatchPolicy() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        failedRegistryRequestMismatchPolicy();

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        assertThat(jobForJobId.getTombstones()).hasSize(1);

        final Tombstone actualTombstone = jobForJobId.getTombstones().get(0);
        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(actualTombstone.getEndpointURL()).isEqualTo(CONTROLPLANE_PUBLIC_URL + DSP_PATH);

        final List<String> rootCauses = actualTombstone.getProcessingError().getRootCauses();
        assertThat(rootCauses).hasSize(1);
        assertThat(rootCauses.get(0)).contains(
                "UsagePolicyPermissionException: Policies [default-policy] did not match with policy from %s.".formatted(
                        TEST_BPN));
    }

    @Test
    void shouldCreateDetailedTombstoneForEdcErrors() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        failedRegistryRequestEdcError();

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        final List<Tombstone> tombstones = jobForJobId.getTombstones();
        assertThat(tombstones).hasSize(1);

        final Tombstone actualTombstone = tombstones.get(0);
        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(actualTombstone.getEndpointURL()).isEqualTo(CONTROLPLANE_PUBLIC_URL + DSP_PATH);

        final List<String> rootCauses = actualTombstone.getProcessingError().getRootCauses();
        assertThat(rootCauses).hasSize(1);
        assertThat(rootCauses.get(0)).contains("502 Bad Gateway");
    }

    @Test
    void whenEmptyCatalogIsReturnedFromAllEndpoints() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";
        final List<String> edcUrls = List.of("https://test.edc1.io", "https://test.edc2.io");
        final List<String> expectedEdcUrls = List.of("https://test.edc1.io" + DSP_PATH,
                "https://test.edc2.io" + DSP_PATH);
        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery(edcUrls);

        edcUrls.forEach(edcUrl -> emptyCatalog(TEST_BPN, edcUrl));

        // Act
        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        // Assert

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        final List<Tombstone> tombstones = jobForJobId.getTombstones();
        assertThat(tombstones).hasSize(1);

        final Tombstone actualTombstone = tombstones.get(0);
        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        final List<String> actualEndpointUrlsInTombstone = List.of(
                actualTombstone.getEndpointURL().replace(" ", "").split(";"));
        actualEndpointUrlsInTombstone.forEach(s -> assertThat(expectedEdcUrls.contains(s)).describedAs("Tombstone should contain all EDC URLs").isTrue());
    }

    @Test
    void shouldCreateDetailedTombstoneForDiscoveryErrors() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.failedEdcDiscovery();

        failedRegistryRequestEdcError();

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle.getId());
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        assertThat(jobForJobId.getTombstones()).hasSize(1);
        final Tombstone actualTombstone = jobForJobId.getTombstones().get(0);

        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(actualTombstone.getEndpointURL()).describedAs(
                "Endpoint url empty because it could not be discovered").isEmpty();

        final List<String> rootCauses = actualTombstone.getProcessingError().getRootCauses();
        assertThat(rootCauses).hasSize(1);
        assertThat(rootCauses.get(0)).contains("No EDC Endpoints could be discovered for BPN '%s'".formatted(TEST_BPN));
    }

    @Test
    void shouldDoABatchRequestAndFinishAllJobs_regularJob() {
        // Arrange
        final String globalAssetIdLevel1 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf4";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";
        final String globalAssetIdLevel3 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf6";
        final String globalAssetIdLevel4 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf7";
        final String globalAssetIdLevel5 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf8";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");

        Set<PartChainIdentificationKey> keys = Set.of(
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel1).build(),
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel2).build(),
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel3).build(),
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel4).build(),
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel5).build());

        // Act
        final UUID batchOrderId = batchService.create(
                WiremockSupport.batchOrderRequest(keys, 1, WiremockSupport.CALLBACK_URL));

        assertThat(batchOrderId).isNotNull();

        waitForBatchOrderEventListenerFired();

        List<Batch> allBatches = persistentBatchStore.findAll();

        allBatches.stream()
                  .map(Batch::getJobProgressList)
                  .flatMap(List::stream)
                  .forEach(jobProgress -> waitForCompletion(jobProgress.getJobId()));

        // Assert
        WiremockSupport.verifyDiscoveryCalls(1);
        WiremockSupport.verifyNegotiationCalls(3);

        List<UUID> jobIds = allBatches.stream()
                                      .flatMap(batch -> batch.getJobProgressList().stream())
                                      .map(JobProgress::getJobId)
                                      .toList();

        assertThat(jobIds).hasSize(5);

        List<Jobs> jobs = jobIds.stream().map(jobId -> irsService.getJobForJobId(jobId, true)).toList();

        Jobs job1 = jobs.stream()
                        .filter(job -> job.getJob().getGlobalAssetId().getGlobalAssetId().equals(globalAssetIdLevel1))
                        .findFirst()
                        .get();

        assertThat(job1.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(job1.getShells()).hasSize(2);
        assertThat(job1.getRelationships()).hasSize(1);
        assertThat(job1.getTombstones()).isEmpty();
        assertThat(job1.getSubmodels()).hasSize(2);

        WiremockSupport.verifyCallbackCall(job1.getJob().getId().toString(), JobState.COMPLETED, 1);

        Jobs job2 = jobs.stream()
                        .filter(job -> job.getJob().getGlobalAssetId().getGlobalAssetId().equals(globalAssetIdLevel2))
                        .findFirst()
                        .get();

        assertThat(job2.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(job2.getShells()).hasSize(1);
        assertThat(job2.getRelationships()).hasSize(1);
        assertThat(job2.getTombstones()).hasSize(1);
        assertThat(job2.getSubmodels()).hasSize(2);

        WiremockSupport.verifyCallbackCall(job2.getJob().getId().toString(), JobState.COMPLETED, 1);

        //cleanup
        allBatches.forEach(batch -> {
            try {
                blobStore.delete(toBlobId(batch.getBatchId().toString()), new ArrayList<>());
            } catch (BlobPersistenceException e) {
                // ignoring
            }
        });
    }

    @Test
    void shouldDoABatchRequestAndFinishAllJobs_essJob() {
        // Arrange
        final String globalAssetIdLevel1 = "globalAssetId";
        final String globalAssetIdLevel2 = "urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();
        WiremockSupport.successfulBatchCallbackRequest();

        successfulRegistryAndDataRequest(globalAssetIdLevel1, "Cathode", TEST_BPN, "integrationtesting/batch-1.json",
                "integrationtesting/singleLevelBomAsBuilt-1.json");
        successfulRegistryAndDataRequest(globalAssetIdLevel2, "Polyamid", TEST_BPN, "integrationtesting/batch-2.json",
                "integrationtesting/singleLevelBomAsBuilt-2.json");

        Set<PartChainIdentificationKey> keys = Set.of(
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel1).build(),
                PartChainIdentificationKey.builder().bpn(TEST_BPN).globalAssetId(globalAssetIdLevel2).build());

        // Act
        final UUID batchOrderId = batchService.create(
                WiremockSupport.bpnInvestigationBatchOrderRequest(keys, WiremockSupport.CALLBACK_BATCH_URL));

        assertThat(batchOrderId).isNotNull();

        waitForBatchOrderEventListenerFired();

        List<Batch> allBatches = persistentBatchStore.findAll();

        allBatches.stream()
                  .map(Batch::getJobProgressList)
                  .flatMap(List::stream)
                  .forEach(jobProgress -> waitForCompletion(jobProgress.getJobId()));

        // Assert
        WiremockSupport.verifyDiscoveryCalls(1);
        // since there are no submodels related to asPlanned lifecycle, only the registry asset is negotiated
        WiremockSupport.verifyNegotiationCalls(1);

        List<UUID> jobIds = allBatches.stream()
                                      .flatMap(batch -> batch.getJobProgressList().stream())
                                      .map(JobProgress::getJobId)
                                      .toList();

        assertThat(jobIds).hasSize(2);

        List<Jobs> jobs = jobIds.stream().map(jobId -> irsService.getJobForJobId(jobId, true)).toList();

        Jobs job1 = jobs.stream()
                        .filter(job -> job.getJob().getGlobalAssetId().getGlobalAssetId().equals(globalAssetIdLevel1))
                        .findFirst()
                        .get();

        assertThat(job1.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(job1.getShells()).hasSize(1);
        assertThat(job1.getRelationships()).hasSize(0);
        assertThat(job1.getTombstones()).isEmpty();
        assertThat(job1.getSubmodels()).hasSize(0);

        Jobs job2 = jobs.stream()
                        .filter(job -> job.getJob().getGlobalAssetId().getGlobalAssetId().equals(globalAssetIdLevel2))
                        .findFirst()
                        .get();

        assertThat(job2.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(job2.getShells()).hasSize(1);
        assertThat(job2.getRelationships()).hasSize(0);
        assertThat(job2.getTombstones()).hasSize(0);
        assertThat(job2.getSubmodels()).hasSize(0);

        WiremockSupport.verifyBatchCallbackCall(allBatches.get(0).getBatchId().toString(), JobState.COMPLETED, 1);
        WiremockSupport.verifyBatchCallbackCall(allBatches.get(1).getBatchId().toString(), JobState.COMPLETED, 1);
    }

    private void waitForBatchOrderEventListenerFired() {
        Awaitility.await()
                  .timeout(Duration.ofSeconds(30))
                  .pollInterval(Duration.ofMillis(500))
                  .until(() -> persistentBatchStore.findAll()
                                                   .stream()
                                                   .map(Batch::getJobProgressList)
                                                   .flatMap(List::stream)
                                                   .allMatch(jobProgress -> jobProgress.getJobId() != null));
    }

    protected String toBlobId(final String batchId) {
        return BATCH_PREFIX + batchId;
    }

    private void successfulRegistryAndDataRequest(final String globalAssetId, final String idShort, final String bpn,
            final String batchFileName, final String sbomFileName) {

        final String edcAssetId = WiremockSupport.randomUUIDwithPrefix();
        final String batch = WiremockSupport.submodelRequest(edcAssetId, BATCH, BATCH_3_0_0, batchFileName);

        final String singleLevelBomAsBuilt = WiremockSupport.submodelRequest(edcAssetId, SINGLE_LEVEL_BOM_AS_BUILT,
                SINGLE_LEVEL_BOM_AS_BUILT_3_0_0, sbomFileName);

        final List<String> submodelDescriptors = List.of(batch, singleLevelBomAsBuilt);

        final String shellId = WiremockSupport.randomUUIDwithPrefix();
        final String registryEdcAssetId = "registry-asset";
        successfulRegistryNegotiation(registryEdcAssetId);
        stubFor(getLookupShells200(PUBLIC_LOOKUP_SHELLS_PATH, List.of(shellId)).withQueryParam("assetIds",
                equalTo(encodedAssetIds(globalAssetId))));
        stubFor(getShellDescriptor200(PUBLIC_SHELL_DESCRIPTORS_PATH + WiremockSupport.encodedId(shellId), bpn,
                submodelDescriptors, globalAssetId, shellId, idShort));
        successfulNegotiation(edcAssetId);
    }

    private void successfulNegotiation(final String edcAssetId) {
        final String negotiationId = randomUUID();
        final String transferProcessId = randomUUID();
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareNegotiation(negotiationId, transferProcessId, contractAgreementId,
                edcAssetId);
        endpointDataReferenceStorage.put(contractAgreementId, createEndpointDataReference(contractAgreementId));
    }

    private void successfulRegistryNegotiation(final String edcAssetId) {
        final String negotiationId = randomUUID();
        final String transferProcessId = randomUUID();
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareRegistryNegotiation(negotiationId, transferProcessId, contractAgreementId,
                edcAssetId);
        endpointDataReferenceStorage.put(contractAgreementId, createEndpointDataReference(contractAgreementId));
    }

    private void successfulEdrRegistryAndDataRequest(final String globalAssetId, final String idShort, final String bpn,
            final String batchFileName, final String sbomFileName) {

        final String edcAssetId = WiremockSupport.randomUUIDwithPrefix();
        final String batch = WiremockSupport.submodelRequest(edcAssetId, BATCH, BATCH_3_0_0, batchFileName);

        final String singleLevelBomAsBuilt = WiremockSupport.submodelRequest(edcAssetId, SINGLE_LEVEL_BOM_AS_BUILT,
                SINGLE_LEVEL_BOM_AS_BUILT_3_0_0, sbomFileName);

        final List<String> submodelDescriptors = List.of(batch, singleLevelBomAsBuilt);

        final String shellId = WiremockSupport.randomUUIDwithPrefix();
        final String registryEdcAssetId = "registry-asset";
        successfulEdrRegistryNegotiation(registryEdcAssetId);
        stubFor(getLookupShells200(PUBLIC_LOOKUP_SHELLS_PATH, List.of(shellId)).withQueryParam("assetIds",
                equalTo(encodedAssetIds(globalAssetId))));
        stubFor(getShellDescriptor200(PUBLIC_SHELL_DESCRIPTORS_PATH + WiremockSupport.encodedId(shellId), bpn,
                submodelDescriptors, globalAssetId, shellId, idShort));
        successfulEdrNegotiation(edcAssetId);
    }

    private void successfulEdrNegotiation(final String edcAssetId) {
        final String negotiationId = randomUUID();
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareEdrNegotiation(negotiationId, contractAgreementId, edcAssetId);

        edcCallbackController.receiveNegotiationsCallback(mockNegotiationCallback(negotiationId, contractAgreementId));
        edcCallbackController.receiveEdcCallback(mockCallback(contractAgreementId, edcAssetId,
                createEndpointDataReference(contractAgreementId).getAuthCode()));
    }

    private void successfulEdrRegistryNegotiation(final String edcAssetId) {
        final String negotiationId = randomUUID();
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareEdrRegistryNegotiation(negotiationId, contractAgreementId, edcAssetId);

        edcCallbackController.receiveNegotiationsCallback(mockNegotiationCallback(negotiationId, contractAgreementId));
        edcCallbackController.receiveEdcCallback(mockCallback(contractAgreementId, edcAssetId,
                createEndpointDataReference(contractAgreementId).getAuthCode()));
    }

    private void failedRegistryRequestMismatchPolicy() {
        final String registryEdcAssetId = "registry-asset-policy-missmatch";
        failedPolicyMismatchNegotiation(registryEdcAssetId);
    }

    private void failedRegistryRequestEdcError() {
        failedNegotiation();
    }

    private void failedPolicyMismatchNegotiation(final String edcAssetId) {
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareMissmatchPolicyCatalog(edcAssetId, contractAgreementId);
    }

    private void failedNegotiation() {
        SubmodelFacadeWiremockSupport.prepareFailingCatalog();
    }

    private void emptyCatalog(final String bpn, final String edcUrl) {
        SubmodelFacadeWiremockSupport.prepareEmptyCatalog(bpn, edcUrl);
    }

    private void waitForCompletion(final UUID jobHandleId) {
        Awaitility.await()
                  .pollDelay(Duration.ZERO)
                  .timeout(Duration.ofSeconds(35))
                  .pollInterval(Duration.ofMillis(1000))
                  .until(() -> irsService.getJobForJobId(jobHandleId, false)
                                         .getJob()
                                         .getState()
                                         .equals(JobState.COMPLETED));
    }

    public static class MinioConfigInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            final String hostAddress = minioContainer.getHostAddress();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "blobstore.endpoint=http://" + hostAddress, "blobstore.accessKey=" + ACCESS_KEY,
                    "blobstore.secretKey=" + SECRET_KEY, "policystore.persistence.endpoint=http://" + hostAddress,
                    "policystore.persistence.accessKey=" + ACCESS_KEY,
                    "policystore.persistence.secretKey=" + SECRET_KEY,
                    "policystore.persistence.bucketName=policy-test");
        }
    }

    private String mockCallback(final String contractAgreementId, final String edcAssetId, final String authCode) {
        final String dataplaneEndpoint =
                SubmodelFacadeWiremockSupport.DATAPLANE_HOST + SubmodelFacadeWiremockSupport.PATH_DATAPLANE_PUBLIC;
        return """
                {
                  "id": "bc916834-61b8-4754-b3e2-1eb041d253c2",
                  "at": 1714645750814,
                  "payload": {
                    "assetId": "%s",
                    "contractId": "%s",
                    "dataAddress": {
                      "properties": {
                        "process_id": "%s",
                        "https://w3id.org/edc/v0.0.1/ns/endpoint": "%s",
                        "asset_id": "%s",
                        "agreement_id": "%s",
                        "https://w3id.org/edc/v0.0.1/ns/authorization": "%s"
                      }
                    }
                  }
                }
                """.formatted(edcAssetId, contractAgreementId, UUID.randomUUID().toString(), dataplaneEndpoint,
                edcAssetId, contractAgreementId, authCode);

    }

    private String mockNegotiationCallback(String contractNegotiationId, String contractAgreementId) {
        return """
                {
                  "id": "c87cbd8a-363a-41eb-a9f5-214da3a08bdc",
                  "at": 1732284831946,
                  "payload": {
                    "contractNegotiationId": "%s",
                    "counterPartyAddress": "https://tracexb-provider-edc-edc.enablement.integration.cofinity-x.com/api/v1/dsp",
                    "counterPartyId": "BPNL000000002CS4",
                    "callbackAddresses": [
                      {
                        "uri": "https://irs.callback/edr",
                        "events": [
                          "transfer.process.started"
                        ],
                        "transactional": false,
                        "authKey": null,
                        "authCodeId": null
                      },
                      {
                        "uri": "https://irs.callback/negotiation",
                        "events": [
                          "contract.negotiation.finalized"
                        ],
                        "transactional": false,
                        "authKey": null,
                        "authCodeId": null
                      },
                      {
                        "uri": "local://adapter",
                        "events": [
                          "contract.negotiation",
                          "transfer.process"
                        ],
                        "transactional": true,
                        "authKey": null,
                        "authCodeId": null
                      }
                    ],
                    "contractOffers": [
                      {
                        "id": "ZmNiZTVmMDgtOTUzNi00OWM2LTgyYzMtODYwYzcxNGE0M2Ex:dXJuOnV1aWQ6YjA4ZjU0ZTAtOTJjYS00Y2E1LTkxMTUtNzUzMWMzYTQ3YmJj:OTcxNmVkYmQtZDc2OS00Nzc5LTgwZGItMTFkODRlNmEzYTJh",
                        "policy": {
                          "permissions": [
                            {
                              "edctype": "dataspaceconnector:permission",
                              "action": {
                                "type": "use",
                                "includedIn": null,
                                "constraint": null
                              },
                              "constraints": [
                                {
                                  "edctype": "AndConstraint",
                                  "constraints": [
                                    {
                                      "edctype": "AtomicConstraint",
                                      "leftExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                      },
                                      "rightExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "traceability:1.0"
                                      },
                                      "operator": "EQ"
                                    },
                                    {
                                      "edctype": "AtomicConstraint",
                                      "leftExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                      },
                                      "rightExpression": {
                                        "edctype": "dataspaceconnector:literalexpression",
                                        "value": "cx.core.industrycore:1"
                                      },
                                      "operator": "EQ"
                                    }
                                  ]
                                }
                              ],
                              "duties": []
                            }
                          ],
                          "prohibitions": [],
                          "obligations": [],
                          "extensibleProperties": {},
                          "inheritsFrom": null,
                          "assigner": "BPNL000000002CS4",
                          "assignee": null,
                          "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                          "@type": {
                            "@policytype": "offer"
                          }
                        },
                        "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc"
                      }
                    ],
                    "protocol": "dataspace-protocol-http",
                    "contractAgreement": {
                      "id": "%s",
                      "providerId": "BPNL000000002CS4",
                      "consumerId": "BPNL000000002BR4",
                      "contractSigningDate": 1732284827,
                      "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                      "policy": {
                        "permissions": [
                          {
                            "edctype": "dataspaceconnector:permission",
                            "action": {
                              "type": "use",
                              "includedIn": null,
                              "constraint": null
                            },
                            "constraints": [
                              {
                                "edctype": "AndConstraint",
                                "constraints": [
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "traceability:1.0"
                                    },
                                    "operator": "EQ"
                                  },
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "cx.core.industrycore:1"
                                    },
                                    "operator": "EQ"
                                  }
                                ]
                              }
                            ],
                            "duties": []
                          }
                        ],
                        "prohibitions": [],
                        "obligations": [],
                        "extensibleProperties": {},
                        "inheritsFrom": null,
                        "assigner": "BPNL000000002CS4",
                        "assignee": "BPNL000000002BR4",
                        "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                        "@type": {
                          "@policytype": "contract"
                        }
                      }
                    },
                    "lastContractOffer": {
                      "id": "ZmNiZTVmMDgtOTUzNi00OWM2LTgyYzMtODYwYzcxNGE0M2Ex:dXJuOnV1aWQ6YjA4ZjU0ZTAtOTJjYS00Y2E1LTkxMTUtNzUzMWMzYTQ3YmJj:OTcxNmVkYmQtZDc2OS00Nzc5LTgwZGItMTFkODRlNmEzYTJh",
                      "policy": {
                        "permissions": [
                          {
                            "edctype": "dataspaceconnector:permission",
                            "action": {
                              "type": "use",
                              "includedIn": null,
                              "constraint": null
                            },
                            "constraints": [
                              {
                                "edctype": "AndConstraint",
                                "constraints": [
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/FrameworkAgreement"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "traceability:1.0"
                                    },
                                    "operator": "EQ"
                                  },
                                  {
                                    "edctype": "AtomicConstraint",
                                    "leftExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "https://w3id.org/catenax/policy/UsagePurpose"
                                    },
                                    "rightExpression": {
                                      "edctype": "dataspaceconnector:literalexpression",
                                      "value": "cx.core.industrycore:1"
                                    },
                                    "operator": "EQ"
                                  }
                                ]
                              }
                            ],
                            "duties": []
                          }
                        ],
                        "prohibitions": [],
                        "obligations": [],
                        "extensibleProperties": {},
                        "inheritsFrom": null,
                        "assigner": "BPNL000000002CS4",
                        "assignee": null,
                        "target": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc",
                        "@type": {
                          "@policytype": "offer"
                        }
                      },
                      "assetId": "urn:uuid:b08f54e0-92ca-4ca5-9115-7531c3a47bbc"
                    }
                  },
                  "type": "ContractNegotiationFinalized"
                }
                """.formatted(contractNegotiationId, contractAgreementId);
    }

}
