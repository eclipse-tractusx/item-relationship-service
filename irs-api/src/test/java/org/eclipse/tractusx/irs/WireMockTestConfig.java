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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.eclipse.tractusx.irs.bpdm.BpdmWireMockConfig.bpdmResponse;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.BPDM_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DISCOVERY_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DTR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.EDC_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.NO_ERROR_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.SEMHUB_REST_TEMPLATE;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockConfig.batchSchemaResponse200;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockConfig.singleLevelBomAsBuiltSchemaResponse200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.DISCOVERY_FINDER_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.EDC_DISCOVERY_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.TEST_BPN;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postDiscoveryFinder200;
import static org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockConfig.postEdcDiscovery200;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockConfig.DATAPLANE_PUBLIC_PATH;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.DATAPLANE_HOST;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_CATALOG;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_DATAPLANE_PUBLIC;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_NEGOTIATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_STATE;
import static org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockConfig.PATH_TRANSFER;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.restTemplateProxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class WireMockTestConfig {
    public static final int HTTP_PORT = 8085;
    private static final String PROXY_SERVER_HOST = "127.0.0.1";

    @Primary
    @Profile("integrationtest")
    @Bean(DTR_REST_TEMPLATE)
    RestTemplate dtrRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(EDC_REST_TEMPLATE)
    RestTemplate edcRestTemplate() {
        final RestTemplate edcRestTemplate = restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
        final List<HttpMessageConverter<?>> messageConverters = edcRestTemplate.getMessageConverters();
        for (final HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
                final ObjectMapper mappingJackson2HttpMessageConverterObjectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                PolicyRegistrationTypes.TYPES.forEach(
                        mappingJackson2HttpMessageConverterObjectMapper::registerSubtypes);
            }
        }
        return edcRestTemplate;
    }

    @Primary
    @Profile("integrationtest")
    @Bean(NO_ERROR_REST_TEMPLATE)
    RestTemplate noErrorRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(DISCOVERY_REST_TEMPLATE)
    RestTemplate discoveryRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(BPDM_REST_TEMPLATE)
    RestTemplate bpdmRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
    }

    @Primary
    @Profile("integrationtest")
    @Bean(SEMHUB_REST_TEMPLATE)
    @Qualifier(SEMHUB_REST_TEMPLATE)
    RestTemplate semanticHubRestTemplate() {
        return restTemplateProxy(PROXY_SERVER_HOST, HTTP_PORT);
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

    static void successfulSemanticModelRequest() {
        stubFor(get(urlPathEqualTo("/models")).willReturn(
                responseWithStatus(200).withBodyFile("semantichub/all-models-page-IT.json")));
    }

    static RegisterJob jobRequest(final String globalAssetId, final String bpn, final int depth) {
        return RegisterJob.builder()
                          .key(PartChainIdentificationKey.builder().bpn(bpn).globalAssetId(globalAssetId).build())
                          .depth(depth)
                          .aspects(List.of("Batch", "SingleLevelBomAsBuilt"))
                          .collectAspects(true)
                          .lookupBPNs(true)
                          .direction(Direction.DOWNWARD)
                          .build();
    }

    static void successfulDiscovery() {
        stubFor(postDiscoveryFinder200());
        stubFor(postEdcDiscovery200());
    }

    static String encodedId(final String shellId) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(shellId.getBytes(StandardCharsets.UTF_8));
    }

    static void verifyDiscoveryCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(DISCOVERY_FINDER_PATH)));
        verify(times, postRequestedFor(urlPathEqualTo(EDC_DISCOVERY_PATH)));
    }

    static void verifyNegotiationCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(PATH_NEGOTIATE)));
        verify(times, postRequestedFor(urlPathEqualTo(PATH_CATALOG)));
        verify(times * 2, getRequestedFor(urlPathMatching(PATH_NEGOTIATE + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(PATH_NEGOTIATE + "/.*" + PATH_STATE)));
        verify(times, postRequestedFor(urlPathEqualTo(PATH_TRANSFER)));
        verify(times * 2, getRequestedFor(urlPathMatching(PATH_TRANSFER + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(PATH_TRANSFER + "/.*" + PATH_STATE)));
    }

    static void successfulBpdmRequests() {
        stubFor(get(urlPathMatching("/legal-entities/.*")).willReturn(
                responseWithStatus(200).withBody(bpdmResponse(TEST_BPN, "Company Name"))));
    }

    static void successfulDataRequests(final String assetId, final String fileName) {
        stubFor(get(urlPathMatching(DATAPLANE_PUBLIC_PATH + "/" + assetId)).willReturn(
                responseWithStatus(200).withBodyFile(fileName)));
    }

    static void successfulSemanticHubRequests() {
        stubFor(batchSchemaResponse200());
        stubFor(singleLevelBomAsBuiltSchemaResponse200());
    }

    static String randomUUIDwithPrefix() {
        final String uuidPrefix = "urn:uuid:";
        return uuidPrefix + randomUUID();
    }

    static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
