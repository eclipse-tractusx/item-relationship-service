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
package org.eclipse.tractusx.irs.edc.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_CID;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createEdcTransformer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.retry.RetryRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.ConstraintCheckerService;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class SubmodelFacadeWiremockTest {
    private final static String connectorEndpoint = "https://connector.endpoint.com";
    private final static String submodelDataplanePath = "/api/public/shells/12345/submodels/5678/submodel";
    private final static String assetId = "12345";
    private final EdcConfiguration config = new EdcConfiguration();
    private final EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(Duration.ofMinutes(1));
    private WireMockServer wireMockServer;
    private EdcSubmodelClient edcSubmodelClient;

    @BeforeEach
    void configureSystemUnderTest() {
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();
        configureFor(this.wireMockServer.port());

        config.getControlplane().getEndpoint().setData(buildApiMethodUrl());
        config.getControlplane().getEndpoint().setCatalog("/catalog/request");
        config.getControlplane().getEndpoint().setContractNegotiation("/contractnegotiations");
        config.getControlplane().getEndpoint().setTransferProcess("/transferprocesses");
        config.getControlplane().getEndpoint().setStateSuffix("/state");
        config.getControlplane().setRequestTtl(Duration.ofSeconds(5));
        config.getControlplane().setProviderSuffix("/api/v1/dsp");
        config.getSubmodel().setPath("/submodel");
        config.getSubmodel().setUrnPrefix("/urn");

        final RestTemplate restTemplate = new RestTemplateBuilder().build();
        final List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                final ObjectMapper mappingJackson2HttpMessageConverterObjectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(
                        mappingJackson2HttpMessageConverterObjectMapper::registerSubtypes);
            }
        }

        final AsyncPollingService pollingService = new AsyncPollingService(Clock.systemUTC(),
                Executors.newScheduledThreadPool(1));

        final EdcControlPlaneClient controlPlaneClient = new EdcControlPlaneClient(restTemplate, pollingService, config,
                createEdcTransformer());
        final EdcDataPlaneClient dataPlaneClient = new EdcDataPlaneClient(restTemplate);

        final EDCCatalogFacade catalogFacade = new EDCCatalogFacade(controlPlaneClient, config);

        final AcceptedPoliciesProvider acceptedPoliciesProvider = mock(AcceptedPoliciesProvider.class);
        when(acceptedPoliciesProvider.getAcceptedPolicies()).thenReturn(
                List.of(new AcceptedPolicy(policy("IRS Policy", List.of(
                        new Permission(PolicyType.USE, List.of(new Constraints(
                                List.of(new Constraint("Membership", OperatorType.EQ, List.of("active")),
                                        new Constraint("FrameworkAgreement.traceability", OperatorType.EQ, List.of("active"))),
                                new ArrayList<>()
                        )))
                        )), OffsetDateTime.now().plusYears(1))));
        final PolicyCheckerService policyCheckerService = new PolicyCheckerService(acceptedPoliciesProvider,
                new ConstraintCheckerService());
        final ContractNegotiationService contractNegotiationService = new ContractNegotiationService(controlPlaneClient,
                policyCheckerService, config);

        final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        this.edcSubmodelClient = new EdcSubmodelClientImpl(config, contractNegotiationService, dataPlaneClient, storage,
                pollingService, retryRegistry, catalogFacade);
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipAsString()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(submodelDataplanePath)).willReturn(aResponse().withStatus(200)
                                                                               .withHeader("Content-Type",
                                                                                    "application/json;charset=UTF-8")
                                                                               .withBodyFile(
                                                                                    "singleLevelBomAsBuilt.json")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelRawPayload(connectorEndpoint, buildWiremockDataplaneUrl(), assetId)
                                                 .get();

        // Assert
        assertThat(submodel).contains("\"catenaXId\": \"urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978\"");
    }

    private void prepareNegotiation() {
        final var contentType = "application/json;charset=UTF-8";
        final var pathCatalog = "/catalog/request";
        final var pathNegotiate = "/contractnegotiations";
        final var pathTransfer = "/transferprocesses";
        givenThat(post(urlPathEqualTo(pathCatalog)).willReturn(aResponse().withStatus(200)
                                                                          .withHeader("Content-Type", contentType)
                                                                          .withBodyFile("edc/responseCatalog.json")));

        givenThat(post(urlPathEqualTo(pathNegotiate)).willReturn(aResponse().withStatus(200)
                                                                            .withHeader("Content-Type", contentType)
                                                                            .withBodyFile(
                                                                                    "edc/responseStartNegotiation.json")));

        final var negotiationId = "1bbaec6e-c316-4e1e-8258-c07a648cc43c";
        givenThat(get(urlPathEqualTo(pathNegotiate + "/" + negotiationId)).willReturn(aResponse().withStatus(200)
                                                                                                 .withHeader(
                                                                                                         "Content-Type",
                                                                                                         contentType)
                                                                                                 .withBodyFile(
                                                                                                         "edc/responseGetNegotiationConfirmed.json")));
        givenThat(get(urlPathEqualTo(pathNegotiate + "/" + negotiationId + "/state")).willReturn(
                aResponse().withStatus(200)
                           .withHeader("Content-Type", contentType)
                           .withBodyFile("edc/responseGetNegotiationState.json")));

        givenThat(post(urlPathEqualTo(pathTransfer)).willReturn(aResponse().withStatus(200)
                                                                           .withHeader("Content-Type", contentType)
                                                                           .withBodyFile(
                                                                                   "edc/responseStartTransferprocess.json")));
        final var transferProcessId = "1b21e963-0bc5-422a-b30d-fd3511861d88";
        givenThat(get(urlPathEqualTo(pathTransfer + "/" + transferProcessId + "/state")).willReturn(
                aResponse().withStatus(200)
                           .withHeader("Content-Type", contentType)
                           .withBodyFile("edc/responseGetTransferState.json")));
        givenThat(get(urlPathEqualTo(pathTransfer + "/" + transferProcessId)).willReturn(aResponse().withStatus(200)
                                                                                                    .withHeader(
                                                                                                            "Content-Type",
                                                                                                            contentType)
                                                                                                    .withBodyFile(
                                                                                                            "edc/responseGetTransferConfirmed.json")));

        final var contractAgreementId = "7681f966-36ea-4542-b5ea-0d0db81967de:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-31b614f5-ec14-4ed2-a509-e7b7780083e7:a6144a2e-c1b1-4ec6-96e1-a221da134e4f";
        final EndpointDataReference ref = EndpointDataReference.Builder.newInstance()
                                                                       .authKey("testkey")
                                                                       .authCode("testcode")
                                                                       .properties(Map.of(NAMESPACE_EDC_CID,
                                                                               contractAgreementId))
                                                                       .endpoint("http://provider.dataplane/api/public")
                                                                       .build();
        storage.put(contractAgreementId, ref);
    }

    @Test
    void shouldReturnMaterialForRecyclingAsString()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(submodelDataplanePath)).willReturn(aResponse().withStatus(200)
                                                                               .withHeader("Content-Type",
                                                                                    "application/json;charset=UTF-8")
                                                                               .withBodyFile(
                                                                                    "materialForRecycling.json")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelRawPayload(connectorEndpoint, buildWiremockDataplaneUrl(), assetId)
                                                 .get();

        // Assert
        assertThat(submodel).contains("\"materialName\": \"Cooper\",");
    }

    @Test
    void shouldReturnObjectAsStringWhenResponseNotJSON()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(submodelDataplanePath)).willReturn(aResponse().withStatus(200)
                                                                               .withHeader("Content-Type",
                                                                                    "application/json;charset=UTF-8")
                                                                               .withBody("test")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelRawPayload(connectorEndpoint, buildWiremockDataplaneUrl(), assetId)
                                                 .get();

        // Assert
        assertThat(submodel).isEqualTo("test");
    }

    @Test
    void shouldThrowExceptionWhenResponse_400() {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(submodelDataplanePath)).willReturn(aResponse().withStatus(400)
                                                                               .withHeader("Content-Type",
                                                                                    "application/json;charset=UTF-8")
                                                                               .withBody("{ error: '400'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> edcSubmodelClient.getSubmodelRawPayload(
                connectorEndpoint, buildWiremockDataplaneUrl(), assetId).get(5, TimeUnit.SECONDS);

        // Assert
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(throwingCallable)
                                                           .withCauseInstanceOf(RestClientException.class);
    }

    @Test
    void shouldThrowExceptionWhenResponse_500() {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(submodelDataplanePath)).willReturn(aResponse().withStatus(500)
                                                                               .withHeader("Content-Type",
                                                                                    "application/json;charset=UTF-8")
                                                                               .withBody("{ error: '500'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> edcSubmodelClient.getSubmodelRawPayload(
                connectorEndpoint, buildWiremockDataplaneUrl(), assetId).get(5, TimeUnit.SECONDS);

        // Assert
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(throwingCallable)
                                                           .withCauseInstanceOf(RestClientException.class);
    }

    private String buildWiremockDataplaneUrl() {
        return buildApiMethodUrl() + submodelDataplanePath;
    }
    private String buildApiMethodUrl() {
        return String.format("http://localhost:%d", this.wireMockServer.port());
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(String policyId, List<Permission> permissions) {
        return new org.eclipse.tractusx.irs.edc.client.policy.Policy(
                policyId,
                OffsetDateTime.now().plusYears(1),
                OffsetDateTime.now().plusYears(1),
                permissions
        );
    }
}
