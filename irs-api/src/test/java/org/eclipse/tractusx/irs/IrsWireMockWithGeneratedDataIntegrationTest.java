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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.WiremockSupport.encodedAssetIds;
import static org.eclipse.tractusx.irs.component.enums.AspectType.AspectTypesConstants.BATCH;
import static org.eclipse.tractusx.irs.component.enums.AspectType.AspectTypesConstants.SINGLE_LEVEL_BOM_AS_BUILT;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport.TEST_BPN;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport;
import org.eclipse.tractusx.irs.util.singleleveltestdatagenerator.SemanticModelTemplate;
import org.eclipse.tractusx.irs.util.singleleveltestdatagenerator.SingleLevelTestDataGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import wiremock.com.fasterxml.jackson.databind.JsonNode;

@Slf4j
@WireMockTest(httpPort = 8085)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WireMockTestConfig.class)
@ContextConfiguration(initializers = IrsWireMockWithGeneratedDataIntegrationTest.MinioConfigInitializer.class)
@ActiveProfiles("integrationtest")
class IrsWireMockWithGeneratedDataIntegrationTest {
    public static final String SEMANTIC_HUB_URL = "http://semantic.hub/models";
    public static final String EDC_URL = "http://edc.test";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);
    @Autowired
    private IrsItemGraphQueryService irsService;
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

    private static Map<SemanticModelTemplate, Map<String, JsonNode>> additionalDataGeneration(
            final SemanticModelTemplate template, final Set<String> uuids) {
        Map<SemanticModelTemplate, Map<String, JsonNode>> additionallyGeneratedItems = new LinkedHashMap<>();
        template.getAdditionalTemplates().forEach(additionalTemplate -> {
            final Map<String, JsonNode> generatedItems = SingleLevelTestDataGenerator.generateDataForAdditionalTemplate(
                    additionalTemplate, uuids);
            additionallyGeneratedItems.put(additionalTemplate, generatedItems);
        });

        return additionallyGeneratedItems;
    }

    private static List<String> submodelRequests(final Map<SemanticModelTemplate, Map<String, JsonNode>> generatedData,
            final String globalAssetId, final String edcAssetId) {
        final List<String> submodelDescriptors = new ArrayList<>();
        generatedData.forEach((template, generatedItems) -> {
            final String submodelDescriptor = WiremockSupport.submodelRequestForPayload(edcAssetId,
                    template.getModelName(), template.getAspectName(), generatedItems.get(globalAssetId));

            submodelDescriptors.add(submodelDescriptor);
        });

        return submodelDescriptors;
    }

    void successfulNegotiation(final String edcAssetId) {
        final String negotiationId = WiremockSupport.randomUUID();
        final String transferProcessId = WiremockSupport.randomUUID();
        final String contractAgreementId = "%s:%s:%s".formatted(WiremockSupport.randomUUID(), edcAssetId,
                WiremockSupport.randomUUID());
        SubmodelFacadeWiremockSupport.prepareNegotiation(negotiationId, transferProcessId, contractAgreementId,
                edcAssetId);
        endpointDataReferenceStorage.put(contractAgreementId,
                WiremockSupport.createEndpointDataReference(contractAgreementId));
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames()
                    .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    @ParameterizedTest
    @ValueSource(longs = { 100,
                           300,
                           500,
                           700,
                           1000
    })
    void shouldCancelJobOnceRequestReceived(Long cancelDelay) {
        // Arrange
        WiremockSupport.successfulSemanticModelRequest();
        WiremockSupport.successfulSemanticHubRequests();
        WiremockSupport.successfulDiscovery();

        final Set<Pair<String, String>> additionalModels = Set.of(Pair.of(BATCH, "3.0.0"));
        final int maxDepth = 4;
        String globalAssetIdLevel1 = setupDynamicTestData("__files/integrationtesting/singleLevelModelTemplates.json",
                SINGLE_LEVEL_BOM_AS_BUILT, "3.0.0", additionalModels, 4, maxDepth, TEST_BPN);

        final RegisterJob request = WiremockSupport.jobRequest(globalAssetIdLevel1, TEST_BPN, maxDepth);

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();

        waitAndCancel(jobHandle, cancelDelay);

        Jobs jobBeforeWaiting = irsService.getJobForJobId(jobHandle.getId(), true);
        waitSeconds(6);
        Jobs jobAfterWaiting = irsService.getJobForJobId(jobHandle.getId(), true);

        // Assert
        assertThat(jobAfterWaiting.getJob().getLastModifiedOn()).isEqualTo(
                jobBeforeWaiting.getJob().getLastModifiedOn());
        assertThat(jobAfterWaiting.getJob().getSummary()).isEqualTo(jobBeforeWaiting.getJob().getSummary());
        assertThat(jobAfterWaiting.getBpns()).isEqualTo(jobBeforeWaiting.getBpns());
        assertThat(jobAfterWaiting.getSubmodels()).isEqualTo(jobBeforeWaiting.getSubmodels());
        assertThat(jobAfterWaiting.getRelationships()).isEqualTo(jobBeforeWaiting.getRelationships());
        assertThat(jobAfterWaiting.getShells()).isEqualTo(jobBeforeWaiting.getShells());
        assertThat(jobAfterWaiting.getTombstones()).isEmpty();
    }

    private void waitAndCancel(final JobHandle jobHandle, Long delay) {
        Awaitility.await()
                  .timeout(Duration.ofSeconds(35))
                  .pollDelay(Duration.ofMillis(delay))
                  .until(() -> irsService.cancelJobById(jobHandle.getId()).getState().equals(JobState.CANCELED));
    }

    private void waitSeconds(Integer seconds) {
        final LocalTime now = LocalTime.now();
        Awaitility.await().until(() -> LocalTime.now().isAfter(now.plusSeconds(seconds)));
    }

    void successfulRegistryAndDataRequestForPayloads(final String idShort, final String bpn,
            final Set<String> globalAssetIds, final Map<SemanticModelTemplate, Map<String, JsonNode>> generatedData) {
        globalAssetIds.forEach(globalAssetId -> {
            final String edcAssetId = WiremockSupport.randomUUIDwithPrefix();

            final List<String> submodelDescriptors = submodelRequests(generatedData, globalAssetId, edcAssetId);

            final String shellId = WiremockSupport.randomUUIDwithPrefix();
            final String registryEdcAssetId = "registry-asset";

            successfulNegotiation(registryEdcAssetId);

            stubFor(getLookupShells200(PUBLIC_LOOKUP_SHELLS_PATH, List.of(shellId)).withQueryParam("assetIds",
                    equalTo(encodedAssetIds(globalAssetId))));
            stubFor(getShellDescriptor200(PUBLIC_SHELL_DESCRIPTORS_PATH + WiremockSupport.encodedId(shellId), bpn,
                    submodelDescriptors, globalAssetId, shellId, idShort));

            successfulNegotiation(edcAssetId);
        });
    }

    String setupDynamicTestData(final String templatesFilePath, final String modelName, final String modelVersion,
            final Set<Pair<String, String>> additionalModelNamesAndVersions, final int numOfRelationships,
            final int maxDepth, final String bpn) {
        log.info(
                "Setting up dynamic test data with arguments: templatesFilePath = {}, modelName = {}, modelVersion = {},"
                        + " numOfRelationships = {}, maxDepth = {}, bpn = {}", templatesFilePath, modelName,
                modelVersion, numOfRelationships, maxDepth, bpn);

        final String templatesPath = Objects.requireNonNull(getClass().getClassLoader().getResource(templatesFilePath))
                                            .getFile();

        final Map<SemanticModelTemplate, Map<String, JsonNode>> generatedData = new HashMap<>();

        final Pair<SemanticModelTemplate, Map<String, JsonNode>> result = SingleLevelTestDataGenerator.generateDataForTemplate(
                templatesPath, modelName, modelVersion, additionalModelNamesAndVersions, numOfRelationships, maxDepth);

        final SemanticModelTemplate template = result.getFirst();
        final Map<String, JsonNode> generatedItems = result.getSecond();
        final Set<String> uuids = generatedItems.keySet();

        generatedData.put(template, generatedItems);

        generatedData.putAll(additionalDataGeneration(template, uuids));

        successfulRegistryAndDataRequestForPayloads("Component", TEST_BPN, uuids, generatedData);

        return uuids.iterator().next();
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
