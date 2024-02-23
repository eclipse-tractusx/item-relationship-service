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
package org.eclipse.tractusx.irs.edc.client;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.createEdcTransformer;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.DATAPLANE_HOST;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport.PATH_DATAPLANE_PUBLIC;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.retry.RetryRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPoliciesProvider;
import org.eclipse.tractusx.irs.edc.client.policy.AcceptedPolicy;
import org.eclipse.tractusx.irs.edc.client.policy.Constraint;
import org.eclipse.tractusx.irs.edc.client.policy.ConstraintCheckerService;
import org.eclipse.tractusx.irs.edc.client.policy.Constraints;
import org.eclipse.tractusx.irs.edc.client.policy.Operator;
import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.eclipse.tractusx.irs.edc.client.policy.Permission;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@WireMockTest
class SubmodelFacadeWiremockTest {

    private static final String PROXY_SERVER_HOST = "127.0.0.1";
    private final static String CONNECTOR_ENDPOINT_URL = "https://connector.endpoint.com";
    private final static String SUBMODEL_DATAPLANE_PATH = "/api/public/shells/12345/submodels/5678/submodel";
    private final static String SUBMODEL_DATAPLANE_URL = "http://dataplane.test" + SUBMODEL_DATAPLANE_PATH;
    private final static String ASSET_ID = "12345";

    private EndpointDataReferenceStorage storage;

    private EdcSubmodelClient edcSubmodelClient;
    private AcceptedPoliciesProvider acceptedPoliciesProvider;

    @BeforeEach
    void configureSystemUnderTest(WireMockRuntimeInfo wireMockRuntimeInfo) {

        final RestTemplate restTemplate = restTemplateProxy(PROXY_SERVER_HOST, wireMockRuntimeInfo.getHttpPort());

        final EdcConfiguration config = new EdcConfiguration();
        config.getControlplane().getEndpoint().setData("http://controlplane.test");
        config.getControlplane().getEndpoint().setCatalog("/catalog/request");
        config.getControlplane().getEndpoint().setContractNegotiation("/contractnegotiations");
        config.getControlplane().getEndpoint().setTransferProcess("/transferprocesses");
        config.getControlplane().getEndpoint().setStateSuffix("/state");
        config.getControlplane().setRequestTtl(Duration.ofSeconds(5));
        config.getControlplane().setProviderSuffix("/api/v1/dsp");
        config.getSubmodel().setUrnPrefix("/urn");

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

        storage = new EndpointDataReferenceStorage(Duration.ofMinutes(1));
        final EndpointDataReferenceCacheService endpointDataReferenceCacheService = new EndpointDataReferenceCacheService(
                storage);

        acceptedPoliciesProvider = mock(AcceptedPoliciesProvider.class);
        when(acceptedPoliciesProvider.getAcceptedPolicies()).thenReturn(List.of(new AcceptedPolicy(policy("IRS Policy",
                List.of(new Permission(PolicyType.USE, new Constraints(
                        List.of(new Constraint("Membership", new Operator(OperatorType.EQ), "active"),
                                new Constraint("FrameworkAgreement.traceability", new Operator(OperatorType.EQ),
                                        "active")), new ArrayList<>())))), OffsetDateTime.now().plusYears(1))));
        final PolicyCheckerService policyCheckerService = new PolicyCheckerService(acceptedPoliciesProvider,
                new ConstraintCheckerService());
        final ContractNegotiationService contractNegotiationService = new ContractNegotiationService(controlPlaneClient,
                policyCheckerService, config);

        final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        this.edcSubmodelClient = new EdcSubmodelClientImpl(config, contractNegotiationService, dataPlaneClient,
                pollingService, retryRegistry, catalogFacade, endpointDataReferenceCacheService);
    }

    @Test
    void shouldReturnAssemblyPartRelationshipAsString()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(
                responseWithStatus(200).withBodyFile("singleLevelBomAsBuilt.json")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelPayload(CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL,
                ASSET_ID).get().getPayload();

        // Assert
        assertThat(submodel).contains("\"catenaXId\": \"urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978\"");
    }

    @Test
    void shouldReturnMaterialForRecyclingAsString()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(
                responseWithStatus(200).withBodyFile("materialForRecycling.json")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelPayload(CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL,
                ASSET_ID).get().getPayload();

        // Assert
        assertThat(submodel).contains("\"materialName\": \"Cooper\",");
    }

    @Test
    void shouldReturnObjectAsStringWhenResponseNotJSON()
            throws EdcClientException, ExecutionException, InterruptedException {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(responseWithStatus(200).withBody("test")));

        // Act
        final String submodel = edcSubmodelClient.getSubmodelPayload(CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL,
                ASSET_ID).get().getPayload();

        // Assert
        assertThat(submodel).isEqualTo("test");
    }

    @Test
    void shouldThrowExceptionWhenPoliciesAreNotAccepted() {
        // Arrange
        final List<Constraint> andConstraints = List.of(
                new Constraint("Membership", new Operator(OperatorType.EQ), "active"));
        final ArrayList<Constraint> orConstraints = new ArrayList<>();
        final Permission permission = new Permission(PolicyType.USE, new Constraints(andConstraints, orConstraints));
        final AcceptedPolicy acceptedPolicy = new AcceptedPolicy(policy("IRS Policy", List.of(permission)),
                OffsetDateTime.now().plusYears(1));

        when(acceptedPoliciesProvider.getAcceptedPolicies()).thenReturn(List.of(acceptedPolicy));

        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(responseWithStatus(200).withBody("test")));

        // Act & Assert
        final String errorMessage = "Consumption of asset '58505404-4da1-427a-82aa-b79482bcd1f0' is not permitted as the required catalog offer policies do not comply with defined IRS policies.";
        assertThatExceptionOfType(UsagePolicyException.class).isThrownBy(
                () -> edcSubmodelClient.getSubmodelPayload(CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL, ASSET_ID)
                                       .get()).withMessageEndingWith(errorMessage);
    }

    @Test
    void shouldThrowExceptionWhenResponse_400() {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(
                responseWithStatus(400).withBody("{ error: '400'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> edcSubmodelClient.getSubmodelPayload(
                CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL, ASSET_ID).get(5, TimeUnit.SECONDS);

        // Assert
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(throwingCallable)
                                                           .withCauseInstanceOf(RestClientException.class);
    }

    @Test
    void shouldThrowExceptionWhenResponse_500() {
        // Arrange
        prepareNegotiation();
        givenThat(get(urlPathEqualTo(SUBMODEL_DATAPLANE_PATH)).willReturn(
                responseWithStatus(500).withBody("{ error: '500'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> edcSubmodelClient.getSubmodelPayload(
                CONNECTOR_ENDPOINT_URL, SUBMODEL_DATAPLANE_URL, ASSET_ID).get(5, TimeUnit.SECONDS);

        // Assert
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(throwingCallable)
                                                           .withCauseInstanceOf(RestClientException.class);
    }

    private void prepareNegotiation() {
        final String contractAgreementId = SubmodelFacadeWiremockSupport.prepareNegotiation();
        final EndpointDataReference ref = createEndpointDataReference(contractAgreementId);
        storage.put(contractAgreementId, ref);
    }

    public static EndpointDataReference createEndpointDataReference(final String contractAgreementId) {
        final EDRAuthCode edrAuthCode = EDRAuthCode.builder()
                                                   .cid(contractAgreementId)
                                                   .dad("test")
                                                   .exp(9999999999L)
                                                   .build();
        final String b64EncodedAuthCode = Base64.getUrlEncoder()
                                                .encodeToString(StringMapper.mapToString(edrAuthCode)
                                                                            .getBytes(StandardCharsets.UTF_8));
        final String jwtToken = "eyJhbGciOiJSUzI1NiJ9." + b64EncodedAuthCode + ".test";
        return EndpointDataReference.Builder.newInstance()
                                            .authKey("testkey")
                                            .authCode(jwtToken)
                                            .properties(
                                                    Map.of(JsonLdConfiguration.NAMESPACE_EDC_CID, contractAgreementId))
                                            .endpoint(DATAPLANE_HOST + PATH_DATAPLANE_PUBLIC)
                                            .build();
    }

    private org.eclipse.tractusx.irs.edc.client.policy.Policy policy(String policyId, List<Permission> permissions) {
        return new org.eclipse.tractusx.irs.edc.client.policy.Policy(policyId, OffsetDateTime.now(),
                OffsetDateTime.now().plusYears(1), permissions);
    }
}
