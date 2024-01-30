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
package org.eclipse.tractusx.irs.testing.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.List;

/**
 * WireMock configurations and requests used for testing the EDC Flow.
 */
public final class SubmodelFacadeWiremockConfig {
    public static final String PATH_CATALOG = "/catalog/request";
    public static final String PATH_NEGOTIATE = "/contractnegotiations";
    public static final String PATH_TRANSFER = "/transferprocesses";
    public static final String PATH_STATE = "/state";
    public static final String PATH_DATAPLANE_PUBLIC = "/api/public";
    public static final String DATAPLANE_HOST = "http://provider.dataplane";
    public static final String CONTEXT = """
            {
                    "dct": "https://purl.org/dc/terms/",
                    "tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                    "edc": "https://w3id.org/edc/v0.0.1/ns/",
                    "dcat": "https://www.w3.org/ns/dcat/",
                    "odrl": "http://www.w3.org/ns/odrl/2/",
                    "dspace": "https://w3id.org/dspace/v0.8/"
                }""";
    public static final String EDC_PROVIDER_DUMMY_URL = "https://edc.io/api/v1/dsp";
    public static final String IRS_INTERNAL_CALLBACK_URL = "https://irs.test/internal/endpoint-data-reference";
    public static final String EDC_PROVIDER_BPN = "BPNL00000003CRHK";
    public static final int STATUS_CODE_OK = 200;

    private SubmodelFacadeWiremockConfig() {
    }

    public static String prepareNegotiation() {
        final String contractAgreementId = "7681f966-36ea-4542-b5ea-0d0db81967de:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-31b614f5-ec14-4ed2-a509-e7b7780083e7:a6144a2e-c1b1-4ec6-96e1-a221da134e4f";
        prepareNegotiation("1bbaec6e-c316-4e1e-8258-c07a648cc43c", "1b21e963-0bc5-422a-b30d-fd3511861d88",
                contractAgreementId,
                "5a7ab616-989f-46ae-bdf2-32027b9f6ee6-31b614f5-ec14-4ed2-a509-e7b7780083e7");
        return contractAgreementId;
    }

    @SuppressWarnings("PMD.UseObjectForClearerAPI") // used only for testing
    public static void prepareNegotiation(final String negotiationId, final String transferProcessId,
            final String contractAgreementId, final String edcAssetId) {
        stubFor(post(urlPathEqualTo(PATH_CATALOG)).willReturn(WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                                                                            .withBody(getCatalogResponse(edcAssetId,
                                                                                    "USE", EDC_PROVIDER_BPN))));

        stubFor(post(urlPathEqualTo(PATH_NEGOTIATE)).willReturn(
                WireMockConfig.responseWithStatus(STATUS_CODE_OK).withBody(startNegotiationResponse(negotiationId))));

        final String negotiationState = "FINALIZED";
        stubFor(get(urlPathEqualTo(PATH_NEGOTIATE + "/" + negotiationId)).willReturn(
                WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                              .withBody(getNegotiationConfirmedResponse(negotiationId, negotiationState,
                                      contractAgreementId))));

        stubFor(get(urlPathEqualTo(PATH_NEGOTIATE + "/" + negotiationId + PATH_STATE)).willReturn(
                WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                              .withBody(getNegotiationStateResponse(negotiationState))));

        stubFor(post(urlPathEqualTo(PATH_TRANSFER)).willReturn(WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                                                                             .withBody(startTransferProcessResponse(
                                                                                     transferProcessId))

        ));
        final String transferProcessState = "COMPLETED";
        stubFor(get(urlPathEqualTo(PATH_TRANSFER + "/" + transferProcessId + PATH_STATE)).willReturn(
                WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                              .withBody(getTransferProcessStateResponse(transferProcessState))));
        stubFor(get(urlPathEqualTo(PATH_TRANSFER + "/" + transferProcessId)).willReturn(
                WireMockConfig.responseWithStatus(STATUS_CODE_OK)
                              .withBody(
                                      getTransferConfirmedResponse(transferProcessId, transferProcessState, edcAssetId,
                                              contractAgreementId))));
    }

    private static String startTransferProcessResponse(final String transferProcessId) {
        return startNegotiationResponse(transferProcessId);
    }

    private static String startNegotiationResponse(final String negotiationId) {
        return """
                {
                    "@type": "edc:IdResponseDto",
                    "@id": "%s",
                    "edc:createdAt": 1686830151573,
                    "@context": %s
                }
                """.formatted(negotiationId, CONTEXT);
    }

    private static String getNegotiationStateResponse(final String negotiationState) {
        return stateResponseTemplate("edc:NegotiationState", negotiationState);
    }

    private static String getTransferProcessStateResponse(final String transferProcessState) {
        return stateResponseTemplate("edc:TransferState", transferProcessState);
    }

    private static String stateResponseTemplate(final String responseType, final String negotiationState) {
        return """
                {
                    "@type": "%s",
                    "edc:state": "%s",
                    "@context": %s
                }
                """.formatted(responseType, negotiationState, CONTEXT);
    }

    private static String getNegotiationConfirmedResponse(final String negotiationId, final String negotiationState,
            final String contractAgreementId) {
        return """
                {
                    "@type": "edc:ContractNegotiationDto",
                    "@id": "%s",
                    "edc:type": "CONSUMER",
                    "edc:protocol": "dataspace-protocol-http",
                    "edc:state": "%s",
                    "edc:counterPartyAddress": "%s",
                    "edc:callbackAddresses": [],
                    "edc:contractAgreementId": "%s",
                    "@context": %s
                }
                """.formatted(negotiationId, negotiationState, EDC_PROVIDER_DUMMY_URL, contractAgreementId, CONTEXT);
    }

    private static String getTransferConfirmedResponse(final String transferProcessId, final String transferState,
            final String edcAssetId, final String contractAgreementId) {
        return """
                {
                    "@id": "%s",
                    "@type": "edc:TransferProcessDto",
                    "edc:state": "%s",
                    "edc:stateTimestamp": 1688024335567,
                    "edc:type": "CONSUMER",
                    "edc:callbackAddresses": [],
                    "edc:dataDestination": {
                        "edc:type": "HttpProxy"
                    },
                    "edc:dataRequest": {
                        "@type": "edc:DataRequestDto",
                        "@id": "%s",
                        "edc:assetId": "%s",
                        "edc:contractId": "%s",
                        "edc:connectorId": "%s"
                    },
                    "edc:receiverHttpEndpoint": "%s",
                    "@context": %s
                }
                """.formatted(transferProcessId, transferState, transferProcessId, edcAssetId, contractAgreementId,
                EDC_PROVIDER_BPN, IRS_INTERNAL_CALLBACK_URL, CONTEXT);
    }

    public static String getCatalogResponse(final String edcAssetId, final String permissionType,
            final String edcProviderBpn) {
        return """
                {
                   "@id": "78ff625c-0c05-4014-965c-bd3d0a6a0de0",
                   "@type": "dcat:Catalog",
                   "dcat:dataset": {
                     "@id": "58505404-4da1-427a-82aa-b79482bcd1f0",
                     "@type": "dcat:Dataset",
                     "odrl:hasPolicy": {
                       "@id": "7681f966-36ea-4542-b5ea-0d0db81967de:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-31b614f5-ec14-4ed2-a509-e7b7780083e7:66131c58-32af-4df0-825d-77f7df6017c1",
                       "@type": "odrl:Set",
                       "odrl:permission": {
                         "odrl:target": "%s",
                         "odrl:action": {
                           "odrl:type": "%s"
                         },
                         "odrl:constraint": %s
                       },
                       "odrl:prohibition": [],
                       "odrl:obligation": [],
                       "odrl:target": "%s"
                     },
                     "dcat:distribution": [
                       {
                         "@type": "dcat:Distribution",
                         "dct:format": {
                           "@id": "HttpProxy"
                         },
                         "dcat:accessService": "4ba1faa1-7f1a-4fb7-a41c-317f450e7443"
                       }
                     ],
                     "edc:description": "IRS EDC Test Asset",
                     "edc:id": "%s"
                   },
                   "dcat:service": {
                     "@id": "4ba1faa1-7f1a-4fb7-a41c-317f450e7443",
                     "@type": "dcat:DataService",
                     "dct:terms": "connector",
                     "dct:endpointUrl": "%s"
                   },
                   "edc:participantId": "%s",
                   "@context": %s
                 }
                """.formatted(edcAssetId, permissionType, createConstraints(), edcAssetId, edcAssetId,
                EDC_PROVIDER_DUMMY_URL, edcProviderBpn, CONTEXT);
    }

    private static String createConstraints() {
        final List<String> atomitConstraints = List.of(createAtomicConstraint("Membership", "active"),
                createAtomicConstraint("FrameworkAgreement.traceability", "active"));
        return """
                {
                  "odrl:and": [
                    %s
                  ]
                }""".formatted(String.join(",\n", atomitConstraints));
    }

    private static String createAtomicConstraint(final String leftOperand, final String rightOperand) {
        return """
                {
                  "odrl:leftOperand": "%s",
                  "odrl:operator": {
                    "@id": "odrl:eq"
                  },
                  "odrl:rightOperand": "%s"
                }""".formatted(leftOperand, rightOperand);
    }
}
