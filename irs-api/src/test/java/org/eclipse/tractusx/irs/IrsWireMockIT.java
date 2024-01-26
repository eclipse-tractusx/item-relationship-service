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
package org.eclipse.tractusx.irs;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.WireMockTestConfig.createEndpointDataReference;
import static org.eclipse.tractusx.irs.bpdm.BpdmWireMockConfig.bpdmResponse;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockConfig.batchSchemaResponse200;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockConfig.singleLevelBomAsBuiltSchemaResponse200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.DISCOVERY_FINDER_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.DISCOVERY_FINDER_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.EDC_DISCOVERY_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.TEST_BPN;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postDiscoveryFinder200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postEdcDiscovery200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.DATAPLANE_PUBLIC_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.LOOKUP_SHELLS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.PUBLIC_LOOKUP_SHELLS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.PUBLIC_SHELL_DESCRIPTORS_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.SHELL_DESCRIPTORS_TEMPLATE;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getLookupShells200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.getShellDescriptor200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.lookupShellsResponse;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_CATALOG;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_NEGOTIATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_STATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_TRANSFER;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.commons.codec.binary.Base64;
import org.awaitility.Awaitility;
import org.eclipse.tractusx.irs.bpdm.BpdmWireMockConfig;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockConfig;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@ContextConfiguration(initializers = IrsWireMockIT.MinioConfigInitializer.class)
@ActiveProfiles("integrationtest")
class IrsWireMockIT {
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

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
        givenThat(get(urlPathEqualTo("/models")).willReturn(
                responseWithStatus(200).withBodyFile("semantichub/all-models-page-IT.json")));
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("bpdm.bpnEndpoint", () -> BpdmWireMockConfig.BPDM_TEST);
        registry.add("digitalTwinRegistry.discoveryFinderUrl", () -> DISCOVERY_FINDER_URL);
        registry.add("digitalTwinRegistry.shellDescriptorTemplate", () -> SHELL_DESCRIPTORS_TEMPLATE);
        registry.add("digitalTwinRegistry.lookupShellsTemplate", () -> LOOKUP_SHELLS_TEMPLATE);
        registry.add("digitalTwinRegistry.type", () -> "decentral");
        registry.add("semanticshub.url", () -> SEMANTIC_HUB_URL);
        registry.add("semanticshub.modelJsonSchemaEndpoint", () -> SemanticHubWireMockConfig.SEMANTIC_HUB_SCHEMA_URL);
        registry.add("irs-edc-client.controlplane.endpoint.data", () -> EDC_URL);
        registry.add("irs-edc-client.controlplane.endpoint.catalog", () -> PATH_CATALOG);
        registry.add("irs-edc-client.controlplane.endpoint.contract-negotiation", () -> PATH_NEGOTIATE);
        registry.add("irs-edc-client.controlplane.endpoint.transfer-process", () -> PATH_TRANSFER);
        registry.add("irs-edc-client.controlplane.endpoint.state-suffix", () -> PATH_STATE);
        registry.add("irs-edc-client.controlplane.api-key.header", () -> "X-Api-Key");
        registry.add("irs-edc-client.controlplane.api-key.secret", () -> "test");
        registry.add("resilience4j.retry.configs.default.waitDuration", () -> "1s");
    }

    @Test
    void shouldStartApplicationAndCollectSemanticModels() throws SchemaNotFoundException {
        // Act
        final AspectModels allAspectModels = semanticHubService.getAllAspectModels();

        // Assert
        assertThat(allAspectModels.models()).hasSize(78);
    }

    @Test
    void shouldStartJob() {
        // Arrange
        final String startId = "globalAssetId";

        successfulSemanticHubRequests();
        successfulDiscovery();
        successfulRegistryNegotiation();
        successfulDtrRequest(startId);
        successfulAssetNegotiation();

        successfulDataRequests();

        successfulBpdmRequests();

        final RegisterJob request = RegisterJob.builder()
                                               .key(PartChainIdentificationKey.builder()
                                                                              .bpn(TEST_BPN)
                                                                              .globalAssetId(startId)
                                                                              .build())
                                               .depth(1)
                                               .aspects(List.of("Batch", "SingleLevelBomAsBuilt"))
                                               .collectAspects(true)
                                               .lookupBPNs(true)
                                               .direction(Direction.DOWNWARD)
                                               .build();

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);
        assertThat(jobHandle.getId()).isNotNull();
        Awaitility.await()
                  .timeout(Duration.ofSeconds(35))
                  .pollInterval(Duration.ofSeconds(1))
                  .until(() -> irsService.getJobForJobId(jobHandle.getId(), false)
                                         .getJob()
                                         .getState()
                                         .equals(JobState.COMPLETED));

        Jobs jobForJobId = irsService.getJobForJobId(jobHandle.getId(), true);

        // Assert
        verifyDiscoveryCalls(1);
        verifyNegotiationCalls(3);

        assertThat(jobForJobId.getJob().getState()).isEqualTo(JobState.COMPLETED);
        assertThat(jobForJobId.getShells()).hasSize(2);
        assertThat(jobForJobId.getRelationships()).hasSize(1);
        assertThat(jobForJobId.getTombstones()).isEmpty();
    }

    private void successfulRegistryNegotiation() {
        final String negotiationId = "1bbaec6e-c316-4e1e-8258-c07a648cc43c";
        final String transferProcessId = "1b21e963-0bc5-422a-b30d-fd3511861d88";
        final String edcAssetId = "registry-asset";
        final String contractAgreementId = SubmodelFacadeWiremockConfig.prepareNegotiation(negotiationId,
                transferProcessId,
                "7681f966-36ea-4542-b5ea-0d0db81967de:registry-asset:a6144a2e-c1b1-4ec6-96e1-a221da134e4f", edcAssetId);
        endpointDataReferenceStorage.put(contractAgreementId, createEndpointDataReference(contractAgreementId));
    }

    private void successfulAssetNegotiation() {
        final String negotiationId = "1bbaec6e-c316-4e1e-8258-c07a648cc43c";
        final String transferProcessId = "1b21e963-0bc5-422a-b30d-fd3511861d88";
        final String edcAssetId = "urn:uuid:f8196d6a-1664-4531-bdee-f15dbb1daf26";
        final String contractAgreementId = SubmodelFacadeWiremockConfig.prepareNegotiation(negotiationId,
                transferProcessId,
                "7681f966-36ea-4542-b5ea-0d0db81967de:urn:uuid:f8196d6a-1664-4531-bdee-f15dbb1daf26:a6144a2e-c1b1-4ec6-96e1-a221da134e4f",
                edcAssetId);
        endpointDataReferenceStorage.put(contractAgreementId, createEndpointDataReference(contractAgreementId));
    }

    private static void successfulDiscovery() {
        stubFor(postDiscoveryFinder200());
        stubFor(postEdcDiscovery200());
    }

    private static String encodedId(final String secondDTR) {
        return Base64.encodeBase64String(secondDTR.getBytes(StandardCharsets.UTF_8));
    }

    private static void verifyDiscoveryCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(times, postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
    }

    private static void verifyNegotiationCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(PATH_NEGOTIATE)));
        verify(times, postRequestedFor(urlPathEqualTo(PATH_CATALOG)));
        verify(times * 2, getRequestedFor(urlPathMatching(PATH_NEGOTIATE + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(PATH_NEGOTIATE + "/.*" + PATH_STATE)));
        verify(times, postRequestedFor(urlPathEqualTo(PATH_TRANSFER)));
        verify(times * 2, getRequestedFor(urlPathMatching(PATH_TRANSFER + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(PATH_TRANSFER + "/.*" + PATH_STATE)));
    }

    private static void successfulBpdmRequests() {
        stubFor(get(urlPathMatching("/legal-entities/.*")).willReturn(
                responseWithStatus(200).withBody(bpdmResponse(TEST_BPN, "Company Name"))));
    }

    private static void successfulDataRequests() {
        givenThat(get(urlPathMatching(
                DATAPLANE_PUBLIC_PATH + "/urn:uuid:f53db6ef-7a58-4326-9169-0ae198b85dbf")).willReturn(
                responseWithStatus(200).withBodyFile("integrationtesting/batch-1.json")));
        givenThat(get(urlPathMatching(
                DATAPLANE_PUBLIC_PATH + "/urn:uuid:0e413809-966b-4107-aae5-aeb28bcdaadf")).willReturn(
                responseWithStatus(200).withBodyFile("integrationtesting/singleLevelBomAsBuilt-1.json")));
    }

    private static void successfulSemanticHubRequests() {
        stubFor(batchSchemaResponse200());
        stubFor(singleLevelBomAsBuiltSchemaResponse200());
    }

    private static void successfulDtrRequest(final String startId) {
        stubFor(getLookupShells200(PUBLIC_LOOKUP_SHELLS_PATH).withQueryParam("assetIds", containing(startId)));
        stubFor(getShellDescriptor200(
                PUBLIC_SHELL_DESCRIPTORS_PATH + encodedId("urn:uuid:21f7ebea-fa8a-410c-a656-bd9082e67dcf")));

        final String secondDTR = "urn:uuid:6d505432-8b31-4966-9514-4b753372683f";
        stubFor(get(urlPathEqualTo(PUBLIC_LOOKUP_SHELLS_PATH)).withQueryParam("assetIds",
                                                                      containing("urn:uuid:7e4541ea-bb0f-464c-8cb3-021abccbfaf5"))
                                                              .willReturn(responseWithStatus(200).withBody(
                                                                      lookupShellsResponse(List.of(secondDTR)))));

        stubFor(getShellDescriptor200(PUBLIC_SHELL_DESCRIPTORS_PATH + encodedId(secondDTR)));
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
