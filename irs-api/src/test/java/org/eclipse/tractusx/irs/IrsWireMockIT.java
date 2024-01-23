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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.semanticshub.AspectModels;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WireMockTestConfig.class
                /*, properties = "spring.main.allow-bean-definition-overriding=true"*/)
@ContextConfiguration(initializers = IrsWireMockIT.MinioConfigInitializer.class)
//@Import({ WireMockTestConfig.class })
@ActiveProfiles("integrationtest")
class IrsWireMockIT {
    public static final String BPDM_TEST = "http://bpdm.test/legal-entities/{partnerId}?idType={idType}";
    public static final String DISCOVERY_TEST = "http://discovery.test";
    public static final String SEMANTIC_HUB_URL = "http://semantic.hub/models";
    public static final String SEMANTIC_HUB_SCHEMA_URL = "http://semantic.hub/models/{urn}/json-schema";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);
    public static final String EDC_URL = "http://edc.test:8081/management";
    @Autowired
    private IrsItemGraphQueryService irsService;

    @Autowired
    private SemanticHubService semanticHubService;

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
        givenThat(get(urlPathEqualTo("/models")).willReturn(aResponse().withStatus(200)
                                                                       .withHeader("Content-Type",
                                                                               "application/json;charset=UTF-8")
                                                                       .withBodyFile("all-models-page-IT.json")));
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("bpdm.bpnEndpoint", () -> BPDM_TEST);
        registry.add("digitalTwinRegistry.discoveryFinderUrl", () -> DISCOVERY_TEST);
        registry.add("semanticshub.url", () -> SEMANTIC_HUB_URL);
        registry.add("semanticshub.modelJsonSchemaEndpoint", () -> SEMANTIC_HUB_SCHEMA_URL);
        registry.add("irs-edc-client.controlplane.endpoint.data", () -> EDC_URL);
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
        final RegisterJob request = RegisterJob.builder()
                                               .key(PartChainIdentificationKey.builder()
                                                                              .bpn("BPNTEST")
                                                                              .globalAssetId("globalAssetId")
                                                                              .build())
                                               .depth(2)
                                               .aspects(List.of("SerialPart", "Batch", "SingleLevelBomAsBuilt"))
                                               .collectAspects(true)
                                               .lookupBPNs(true)
                                               .direction(Direction.DOWNWARD)
                                               .build();

        // Act
        final JobHandle jobHandle = irsService.registerItemJob(request);

        // Assert
        assertThat(jobHandle.getId()).isNotNull();
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
