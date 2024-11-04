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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.eclipse.tractusx.irs.SemanticModelNames.BATCH_3_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0;
import static org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport.semanticHubWillReturnAllModels;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.DATAPLANE_PUBLIC_URL;
import static org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport.submodelDescriptor;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.enums.BatchStrategy;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.registryclient.util.SerializationHelper;
import org.eclipse.tractusx.irs.semanticshub.SemanticHubWireMockSupport;
import org.eclipse.tractusx.irs.testing.wiremock.DiscoveryServiceWiremockSupport;
import org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport;
import org.eclipse.tractusx.irs.testing.wiremock.SubmodelFacadeWiremockSupport;

public class WiremockSupport {

    public static final String SUBMODEL_SUFFIX = "/\\$value";
    public static final String CALLBACK_URL = "http://localhost/callback?id={id}&state={state}";
    public static final String CALLBACK_BATCH_URL = "http://localhost/callback?batchId={batchId}&batchState={batchState}";
    public static final String CALLBACK_PATH = "/callback";

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
                                            .contractId(contractAgreementId)
                                            .authKey("Authorization")
                                            .id("test")
                                            .authCode(jwtToken)
                                            .properties(
                                                    Map.of(JsonLdConfiguration.NAMESPACE_EDC_CID, contractAgreementId))
                                            .endpoint(SubmodelFacadeWiremockSupport.DATAPLANE_HOST
                                                    + SubmodelFacadeWiremockSupport.PATH_DATAPLANE_PUBLIC)
                                            .build();
    }

    static void successfulSemanticModelRequest() {
        semanticHubWillReturnAllModels("semantichub/all-models-page-IT.json");
    }

    static RegisterJob jobRequest(final String globalAssetId, final String bpn, final int depth) {
        return RegisterJob.builder()
                          .key(PartChainIdentificationKey.builder().bpn(bpn).globalAssetId(globalAssetId).build())
                          .depth(depth)
                          .aspects(List.of(BATCH_3_0_0, SINGLE_LEVEL_BOM_AS_BUILT_3_0_0))
                          .collectAspects(true)
                          .direction(Direction.DOWNWARD)
                          .build();
    }

    static RegisterJob jobRequest(final String globalAssetId, final String bpn, final int depth,
            final String callbackUrl) {
        return RegisterJob.builder()
                          .key(PartChainIdentificationKey.builder().bpn(bpn).globalAssetId(globalAssetId).build())
                          .depth(depth)
                          .aspects(List.of(BATCH_3_0_0, SINGLE_LEVEL_BOM_AS_BUILT_3_0_0))
                          .collectAspects(true)
                          .direction(Direction.DOWNWARD)
                          .callbackUrl(callbackUrl)
                          .build();
    }

    static RegisterBatchOrder batchOrderRequest(Set<PartChainIdentificationKey> keys, final int depth,
            final String callbackUrl) {
        return RegisterBatchOrder.builder()
                                 .keys(keys)
                                 .depth(depth)
                                 .callbackUrl(callbackUrl)
                                 .batchStrategy(BatchStrategy.PRESERVE_BATCH_ORDER)
                                 .direction(Direction.DOWNWARD)
                                 .collectAspects(true)
                                 .batchSize(1)
                                 .timeout(100)
                                 .aspects(List.of(BATCH_3_0_0, SINGLE_LEVEL_BOM_AS_BUILT_3_0_0))
                                 .build();
    }

    static RegisterBpnInvestigationBatchOrder bpnInvestigationBatchOrderRequest(Set<PartChainIdentificationKey> keys, final String callbackUrl) {
        return RegisterBpnInvestigationBatchOrder.builder()
                                                 .keys(keys)
                                                 .incidentBPNSs(List.of())
                                                 .callbackUrl(callbackUrl)
                                                 .batchStrategy(BatchStrategy.PRESERVE_BATCH_ORDER)
                                                 .batchSize(1)
                                                 .timeout(100)
                                                 .build();
    }

    static void successfulDiscovery() {
        stubFor(DiscoveryServiceWiremockSupport.postDiscoveryFinder200());
        stubFor(DiscoveryServiceWiremockSupport.postEdcDiscovery200());
    }

    static void successfulDiscovery(final List<String> edcUrls) {
        stubFor(DiscoveryServiceWiremockSupport.postDiscoveryFinder200());
        stubFor(DiscoveryServiceWiremockSupport.postEdcDiscovery200(edcUrls));
    }

    static void failedEdcDiscovery() {
        stubFor(DiscoveryServiceWiremockSupport.postDiscoveryFinder200());
        stubFor(DiscoveryServiceWiremockSupport.postEdcDiscovery200Empty());
    }

    static String encodedId(final String shellId) {
        return encodeBase64String(shellId.getBytes(StandardCharsets.UTF_8));
    }

    static String encodedAssetIds(final String assetIds) {
        final IdentifierKeyValuePair globalAssetId = IdentifierKeyValuePair.builder()
                                                                           .name("globalAssetId")
                                                                           .value(assetIds)
                                                                           .build();
        return Base64.getEncoder().encodeToString(new SerializationHelper().serialize(globalAssetId));
    }

    static void verifyDiscoveryCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(DiscoveryServiceWiremockSupport.DISCOVERY_FINDER_PATH)));
        verify(times, postRequestedFor(urlPathEqualTo(DiscoveryServiceWiremockSupport.EDC_DISCOVERY_PATH)));
    }

    static void verifyNegotiationCalls(final int times) {
        verify(times, postRequestedFor(urlPathEqualTo(SubmodelFacadeWiremockSupport.PATH_NEGOTIATE)));
        verify(times, postRequestedFor(urlPathEqualTo(SubmodelFacadeWiremockSupport.PATH_CATALOG)));
        verify(times * 2, getRequestedFor(urlPathMatching(SubmodelFacadeWiremockSupport.PATH_NEGOTIATE + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(
                SubmodelFacadeWiremockSupport.PATH_NEGOTIATE + "/.*" + SubmodelFacadeWiremockSupport.PATH_STATE)));
        verify(times, postRequestedFor(urlPathEqualTo(SubmodelFacadeWiremockSupport.PATH_TRANSFER)));
        verify(times * 2, getRequestedFor(urlPathMatching(SubmodelFacadeWiremockSupport.PATH_TRANSFER + "/.*")));
        verify(times, getRequestedFor(urlPathMatching(
                SubmodelFacadeWiremockSupport.PATH_TRANSFER + "/.*" + SubmodelFacadeWiremockSupport.PATH_STATE)));
    }

    static void successfulDataRequests(final String assetId, final String fileName) {
        stubFor(get(
                urlPathMatching(DtrWiremockSupport.DATAPLANE_PUBLIC_PATH + "/" + assetId + SUBMODEL_SUFFIX)).willReturn(
                responseWithStatus(200).withBodyFile(fileName)));
    }

    static void successfulCallbackRequest() {
        stubFor(get(urlPathEqualTo(CALLBACK_PATH)).withQueryParam("id", matching(".*"))
                                                  .withQueryParam("state", matching(".*"))
                                                  .willReturn(responseWithStatus(200)));
    }

    static void verifyCallbackCall(final String jobId, final JobState state, final int times) {
        verify(times, getRequestedFor(urlPathEqualTo(CALLBACK_PATH)).withQueryParam("id", equalTo(jobId))
                                                                    .withQueryParam("state", equalTo(state.toString())));
    }

    static void verifyBatchCallbackCall(final String jobId, final JobState state, final int times) {
        verify(times, getRequestedFor(urlPathEqualTo(CALLBACK_PATH)).withQueryParam("batchId", equalTo(jobId))
                                                                    .withQueryParam("batchState", equalTo(state.toString())));
    }

    static void successfulSemanticHubRequests() {
        SemanticHubWireMockSupport.semanticHubWillReturnBatchSchema();
        SemanticHubWireMockSupport.semanticHubWillReturnSingleLevelBomAsBuiltSchema();
    }

    static String randomUUIDwithPrefix() {
        final String uuidPrefix = "urn:uuid:";
        return uuidPrefix + randomUUID();
    }

    static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    static String submodelRequest(final String edcAssetId, final String idShort, final String semanticId,
            final String sbomFileName) {
        final String submodelDescriptorId = randomUUIDwithPrefix();
        final String submodel = submodelDescriptor(DATAPLANE_PUBLIC_URL, edcAssetId,
                DiscoveryServiceWiremockSupport.CONTROLPLANE_PUBLIC_URL, idShort, submodelDescriptorId, semanticId);
        successfulDataRequests(submodelDescriptorId, sbomFileName);
        return submodel;
    }
}