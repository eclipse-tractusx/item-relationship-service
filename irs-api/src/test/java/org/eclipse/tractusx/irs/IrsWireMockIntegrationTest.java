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
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_NEGOTIATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_STATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_TRANSFER;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.awaitility.Awaitility;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    public static final String SEMANTIC_HUB_URL = "http://semantic.hub/models";
    public static final String EDC_URL = "http://edc.test";

    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);

    @Autowired
    private IrsItemGraphQueryService irsService;

    @Autowired
    private SemanticHubService semanticHubService;

    @Autowired
    private EndpointDataReferenceStorage endpointDataReferenceStorage;

    @Autowired
    private CacheManager cacheManager;

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
        registry.add("irs-edc-client.controlplane.endpoint.data", () -> EDC_URL);
        registry.add("irs-edc-client.controlplane.endpoint.catalog", () -> PATH_CATALOG);
        registry.add("irs-edc-client.controlplane.endpoint.contract-negotiation", () -> PATH_NEGOTIATE);
        registry.add("irs-edc-client.controlplane.endpoint.transfer-process", () -> PATH_TRANSFER);
        registry.add("irs-edc-client.controlplane.endpoint.state-suffix", () -> PATH_STATE);
        registry.add("irs-edc-client.controlplane.api-key.header", () -> "X-Api-Key");
        registry.add("irs-edc-client.controlplane.api-key.secret", () -> "test");
        registry.add("resilience4j.retry.configs.default.waitDuration", () -> "1s");
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames()
                    .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    @Test
    void shouldStartApplicationAndCollectSemanticModels() throws SchemaNotFoundException {
        // Arrange
        WiremockSupport.successfulSemanticModelRequest();

        // Act
        final AspectModels allAspectModels = semanticHubService.getAllAspectModels();

        // Assert
        assertThat(allAspectModels.models()).hasSize(99);
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
        waitForCompletion(jobHandle);

        Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        // Assert
        WiremockSupport.verifyDiscoveryCalls(1);
        WiremockSupport.verifyNegotiationCalls(3);

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

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, 1, WiremockSupport.CALLBACK_URL);

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
            waitForCompletion(jobHandle);
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
        waitForCompletion(jobHandle);
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
        waitForCompletion(jobHandle);
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
        waitForCompletion(jobHandle);
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobForJobId.getShells()).hasSize(3);
        assertThat(jobForJobId.getRelationships()).hasSize(2);
        assertThat(jobForJobId.getTombstones()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).hasSize(6);

        WiremockSupport.verifyDiscoveryCalls(1);
        WiremockSupport.verifyNegotiationCalls(6);
    }

    @Test
    void shouldCreateDetailedTombstoneForMismatchPolicy() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        failedRegistryRequestMissmatchPolicy();

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle);
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        assertThat(jobForJobId.getTombstones()).hasSize(1);

        final Tombstone actualTombstone = jobForJobId.getTombstones().get(0);
        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(actualTombstone.getEndpointURL()).isEqualTo(CONTROLPLANE_PUBLIC_URL);

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
        waitForCompletion(jobHandle);
        final Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), false);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);

        assertThat(jobForJobId.getShells()).isEmpty();
        assertThat(jobForJobId.getRelationships()).isEmpty();
        assertThat(jobForJobId.getSubmodels()).isEmpty();

        final List<Tombstone> tombstones = jobForJobId.getTombstones();
        assertThat(tombstones).hasSize(1);

        final Tombstone actualTombstone = tombstones.get(0);
        assertThat(actualTombstone.getBusinessPartnerNumber()).isEqualTo(TEST_BPN);
        assertThat(actualTombstone.getEndpointURL()).isEqualTo(CONTROLPLANE_PUBLIC_URL);

        final List<String> rootCauses = actualTombstone.getProcessingError().getRootCauses();
        assertThat(rootCauses).hasSize(1);
        assertThat(rootCauses.get(0)).contains("502 Bad Gateway");
    }

    @Test
    void whenEmptyCatalogIsReturnedFromAllEndpoints() {
        // Arrange
        final String globalAssetId = "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc";
        final List<String> edcUrls = List.of("https://test.edc1.io", "https://test.edc2.io");

        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery(edcUrls);

        edcUrls.forEach(edcUrl -> emptyCatalog(TEST_BPN, edcUrl));

        // Act
        final RegisterJob request = WiremockSupport.jobRequest(globalAssetId, TEST_BPN, 4);
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();
        waitForCompletion(jobHandle);
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
        assertThat(actualTombstone.getEndpointURL()).describedAs("Tombstone should contain all EDC URLs")
                                                    .isEqualTo(String.join("; ", edcUrls));

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
        waitForCompletion(jobHandle);
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

    private void successfulRegistryAndDataRequest(final String globalAssetId, final String idShort, final String bpn,
            final String batchFileName, final String sbomFileName) {

        final String edcAssetId = WiremockSupport.randomUUIDwithPrefix();
        final String batch = WiremockSupport.submodelRequest(edcAssetId, BATCH, BATCH_3_0_0, batchFileName);

        final String singleLevelBomAsBuilt = WiremockSupport.submodelRequest(edcAssetId, SINGLE_LEVEL_BOM_AS_BUILT,
                SINGLE_LEVEL_BOM_AS_BUILT_3_0_0, sbomFileName);

        final List<String> submodelDescriptors = List.of(batch, singleLevelBomAsBuilt);

        final String shellId = WiremockSupport.randomUUIDwithPrefix();
        final String registryEdcAssetId = "registry-asset";
        successfulNegotiation(registryEdcAssetId);
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

    private void failedRegistryRequestMissmatchPolicy() {
        final String registryEdcAssetId = "registry-asset";
        failedPolicyMissmatchNegotiation(registryEdcAssetId);
    }

    private void failedRegistryRequestEdcError() {
        failedNegotiation();
    }

    private void failedPolicyMissmatchNegotiation(final String edcAssetId) {
        final String contractAgreementId = "%s:%s:%s".formatted(randomUUID(), edcAssetId, randomUUID());
        SubmodelFacadeWiremockSupport.prepareMissmatchPolicyCatalog(edcAssetId, contractAgreementId);
    }

    private void failedNegotiation() {
        SubmodelFacadeWiremockSupport.prepareFailingCatalog();
    }

    private void emptyCatalog(final String bpn, final String edcUrl) {
        SubmodelFacadeWiremockSupport.prepareEmptyCatalog(bpn, edcUrl);
    }

    private void waitForCompletion(final JobHandle jobHandle) {
        Awaitility.await()
                  .timeout(Duration.ofSeconds(35))
                  .pollInterval(Duration.ofMillis(500))
                  .until(() -> irsService.getJobForJobId(jobHandle.getId(), false)
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

}
