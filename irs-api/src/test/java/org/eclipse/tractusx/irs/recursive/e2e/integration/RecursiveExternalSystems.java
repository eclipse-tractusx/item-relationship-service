/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;

/**
 * WireMock fixture for the external services used by recursive integration tests.
 * It keeps DTR, discovery, Semantic Hub, EDC control plane and EDC data plane stubs in one test server.
 */
public final class RecursiveExternalSystems implements AutoCloseable {

    private static final String JUPITER_EDC_CONTEXT = """
            {
              "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
              "dct": "http://purl.org/dc/terms/",
              "edc": "https://w3id.org/edc/v0.0.1/ns/",
              "odrl": "http://www.w3.org/ns/odrl/2/",
              "dcat": "http://www.w3.org/ns/dcat#",
              "dspace": "https://w3id.org/dspace/v0.8/"
            }""";

    private final WireMockServer server = new WireMockServer(0);
    private final Map<String, String> contractAgreementIds = new LinkedHashMap<>();

    public void start() {
        server.start();
        stubSemanticHub();
    }

    public String baseUrl() {
        return server.baseUrl();
    }

    public String edcConnectorEndpoint(final String bpnl) {
        return baseUrl() + "/edc/" + bpnl;
    }

    public String edcDspEndpoint(final String bpnl) {
        return edcConnectorEndpoint(bpnl) + "/api/v1/dsp";
    }

    public String descriptorEndpoint() {
        return baseUrl() + "/dtr/api/v3/shell-descriptors/{aasIdentifier}";
    }

    public String shellLookupEndpoint() {
        return baseUrl() + "/dtr/api/v3/lookup/shells?assetIds={assetIds}";
    }

    public String discoveryFinderUrl() {
        return baseUrl() + "/discoveryFinder";
    }

    public String semanticHubUrl() {
        return baseUrl() + "/semantics/models";
    }

    public String semanticHubSchemaUrl() {
        return baseUrl() + "/semantics/{urn}/schema";
    }

    public void stubDiscovery(final List<String> bpnls) {
        final Map<String, List<String>> connectorEndpointsByBpn = new LinkedHashMap<>();
        bpnls.forEach(bpnl -> connectorEndpointsByBpn.put(bpnl, List.of(edcConnectorEndpoint(bpnl))));

        stubDiscovery(connectorEndpointsByBpn);
    }

    public void stubDiscovery(final Map<String, List<String>> connectorEndpointsByBpn) {
        server.stubFor(post(urlPathEqualTo("/discoveryFinder"))
                .willReturn(okJson(json(discoveryFinderResponse()))));
        server.stubFor(post(urlPathEqualTo("/edcDiscovery"))
                .willReturn(okJson(json(edcDiscoveryResponse(connectorEndpointsByBpn)))));
    }

    public Map<String, String> stubShell(final Shell shell) {
        final String shellId = shell.globalAssetId();
        server.stubFor(get(urlPathEqualTo("/dtr/api/v3/lookup/shells"))
                .withQueryParam("assetIds", containing(shell.globalAssetId()))
                .willReturn(okJson(json(lookupShellsResponse(shellId)))));
        server.stubFor(get(urlPathEqualTo("/dtr/api/v3/shell-descriptors/" + base64(shellId)))
                .willReturn(okJson(json(shellResponse(shell)))));

        final Map<String, String> contractsByAsset = new LinkedHashMap<>();
        for (final Submodel submodel : shell.submodels()) {
            stubSubmodelPayload(submodel);
            contractsByAsset.put(submodel.edcAssetId(), stubEdcAsset(submodel.edcAssetId()));
        }
        return contractsByAsset;
    }

    public void stubShellLookupFailure(final String globalAssetId) {
        server.stubFor(get(urlPathEqualTo("/dtr/api/v3/lookup/shells"))
                .withQueryParam("assetIds", containing(globalAssetId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "digital twin lookup failed"
                                }
                                """)));
    }

    public String stubEdcAsset(final String edcAssetId) {
        if (contractAgreementIds.containsKey(edcAssetId)) {
            return contractAgreementIds.get(edcAssetId);
        }

        final String negotiationId = randomId();
        final String transferProcessId = randomId();
        final String contractAgreementId = randomId() + ":" + edcAssetId + ":" + randomId();
        contractAgreementIds.put(edcAssetId, contractAgreementId);

        server.stubFor(post(urlPathEqualTo("/catalog/request"))
                .atPriority(10)
                .withRequestBody(containing(edcAssetId))
                .willReturn(okJson(validCatalogResponse(edcAssetId, contractAgreementId))));
        server.stubFor(post(urlPathEqualTo("/contractnegotiations"))
                .withRequestBody(containing(edcAssetId))
                .willReturn(okJson(json(idResponse(negotiationId)))));
        server.stubFor(get(urlPathEqualTo("/contractnegotiations/" + negotiationId))
                .willReturn(okJson(json(negotiationResponse(negotiationId, contractAgreementId)))));
        server.stubFor(get(urlPathEqualTo("/contractnegotiations/" + negotiationId + "/state"))
                .willReturn(okJson(json(stateResponse("NegotiationState", "FINALIZED")))));
        server.stubFor(post(urlPathEqualTo("/transferprocesses"))
                .withRequestBody(containing(edcAssetId))
                .willReturn(okJson(json(idResponse(transferProcessId)))));
        server.stubFor(get(urlPathEqualTo("/transferprocesses/" + transferProcessId + "/state"))
                .willReturn(okJson(json(stateResponse("TransferState", "COMPLETED")))));
        server.stubFor(get(urlPathEqualTo("/transferprocesses/" + transferProcessId))
                .willReturn(okJson(json(transferResponse(transferProcessId, edcAssetId, contractAgreementId)))));

        return contractAgreementId;
    }

    public String stubEdcAssetByTypeAndVersion(final String edcAssetId, final String apiType,
            final String apiVersion) {
        final String contractAgreementId = stubEdcAsset(edcAssetId);
        server.stubFor(post(urlPathEqualTo("/catalog/request"))
                .atPriority(9)
                .withRequestBody(containing(apiType))
                .withRequestBody(containing(apiVersion))
                .willReturn(okJson(validCatalogResponse(edcAssetId, contractAgreementId))));
        return contractAgreementId;
    }

    public void rejectPolicyForAsset(final String edcAssetId) {
        final String offerId = contractAgreementIds.getOrDefault(edcAssetId, randomId() + ":" + edcAssetId);
        server.stubFor(post(urlPathEqualTo("/catalog/request"))
                .atPriority(1)
                .withRequestBody(containing(edcAssetId))
                .willReturn(okJson(rejectedPolicyCatalogResponse(edcAssetId, offerId))));
    }

    public void rejectCatalogRequestsForConnectorEndpoint(final String connectorEndpoint) {
        server.stubFor(post(urlPathEqualTo("/catalog/request"))
                .atPriority(1)
                .withRequestBody(containing(connectorEndpoint))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "connector endpoint not found"
                                }
                                """)));
    }

    public void verifyCatalogRequestedForConnectorEndpoint(final String connectorEndpoint) {
        server.verify(postRequestedFor(urlPathEqualTo("/catalog/request"))
                .withRequestBody(containing(connectorEndpoint)));
    }

    public EndpointDataReference endpointDataReference(final String contractAgreementId, final String endpoint) {
        return EndpointDataReference.Builder.newInstance()
                .contractId(contractAgreementId)
                .authKey("X-API-KEY")
                .id("recursive-integration-edr")
                .authCode(RecursiveIntegrationTestClient.ADMIN_API_KEY)
                .endpoint(endpoint)
                .properties(Map.of(JsonLdConfiguration.NAMESPACE_EDC_CID, contractAgreementId))
                .build();
    }

    @Override
    public void close() {
        server.stop();
    }

    private void stubSubmodelPayload(final Submodel submodel) {
        server.stubFor(get(urlPathEqualTo("/submodels/" + submodel.path() + "/$value"))
                .willReturn(okJson(json(submodel.payload()))));
    }

    private void stubSemanticHub() {
        server.stubFor(get(urlPathEqualTo("/semantics/models")).willReturn(okJson(json("""
                {
                  "items": [],
                  "totalItems": 0,
                  "currentPage": 0,
                  "totalPages": 0,
                  "itemCount": 0
                }
                """))));
        server.stubFor(get(urlPathMatching("/semantics/.*/schema")).willReturn(okJson(json("""
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "type": "object"
                }
                """))));
    }

    private String shellResponse(final Shell shell) {
        return """
                {
                  "description": [],
                  "displayName": [],
                  "globalAssetId": "%s",
                  "idShort": "%s",
                  "id": "%s",
                  "specificAssetIds": [
                    {
                      "name": "manufacturerId",
                      "value": "%s",
                      "externalSubjectId": {
                        "type": "ExternalReference",
                        "keys": [ { "type": "GlobalReference", "value": "%s" } ]
                      }
                    }
                  ],
                  "submodelDescriptors": [
                    %s
                  ]
                }
                """.formatted(shell.globalAssetId(), shell.idShort(), shell.globalAssetId(), shell.bpnl(), shell.bpnl(),
                String.join(",\n", shell.submodels()
                        .stream()
                        .map(submodel -> submodelDescriptor(shell.bpnl(), submodel))
                        .toList()));
    }

    private String submodelDescriptor(final String bpnl, final Submodel submodel) {
        final String href = baseUrl() + "/submodels/" + submodel.path();
        return """
                {
                  "endpoints": [
                    {
                      "interface": "SUBMODEL-3.0",
                      "protocolInformation": {
                        "href": "%s",
                        "endpointProtocol": "HTTP",
                        "endpointProtocolVersion": [ "1.1" ],
                        "subprotocol": "DSP",
                        "subprotocolBody": "%s",
                        "subprotocolBodyEncoding": "plain",
                        "securityAttributes": [
                          { "type": "NONE", "key": "NONE", "value": "NONE" }
                        ]
                      }
                    }
                  ],
                  "idShort": "%s",
                  "id": "%s",
                  "semanticId": {
                    "type": "ExternalReference",
                    "keys": [
                      { "type": "GlobalReference", "value": "%s" }
                    ]
                  },
                  "supplementalSemanticId": [],
                  "description": [],
                  "displayName": []
                }
                """.formatted(href, subprotocolBody(bpnl, submodel), submodel.idShort(), randomUrn(),
                submodel.semanticId());
    }

    private String subprotocolBody(final String bpnl, final Submodel submodel) {
        if (submodel.includeDspEndpoint()) {
            return "id=%s;dspEndpoint=%s".formatted(submodel.edcAssetId(), edcDspEndpoint(bpnl));
        }

        return "id=%s".formatted(submodel.edcAssetId());
    }

    private String lookupShellsResponse(final String shellId) {
        return """
                {
                  "paging_metadata": {},
                  "result": [ "%s" ]
                }
                """.formatted(shellId);
    }

    private String discoveryFinderResponse() {
        return """
                {
                  "endpoints": [
                    {
                      "type": "bpn",
                      "description": "Recursive integration EDC discovery",
                      "endpointAddress": "%s/edcDiscovery",
                      "documentation": "%s/swagger",
                      "resourceId": "%s"
                    }
                  ]
                }
                """.formatted(baseUrl(), baseUrl(), randomId());
    }

    private String edcDiscoveryResponse(final Map<String, List<String>> connectorEndpointsByBpn) {
        return """
                [
                  %s
                ]
                """.formatted(String.join(",\n", connectorEndpointsByBpn.entrySet()
                        .stream()
                        .map(entry -> edcDiscoveryEntry(entry.getKey(), entry.getValue()))
                        .toList()));
    }

    private String edcDiscoveryEntry(final String bpnl, final List<String> connectorEndpoints) {
        return """
                {
                  "bpn": "%s",
                  "connectorEndpoint": [ %s ]
                }
                """.formatted(bpnl, String.join(", ", connectorEndpoints.stream()
                        .map(this::quoted)
                        .toList()));
    }

    private String quoted(final String value) {
        return "\"" + value + "\"";
    }

    private String validCatalogResponse(final String edcAssetId, final String offerId) {
        return RecursiveJsonPreconditions.edcCatalog(catalogResponse(edcAssetId, offerId, "[]")).json();
    }

    private String rejectedPolicyCatalogResponse(final String edcAssetId, final String offerId) {
        return RecursiveJsonPreconditions.edcCatalog(catalogResponse(edcAssetId, offerId, """
                {
                  "odrl:leftOperand": "https://w3id.org/catenax/policy/UsagePurpose",
                  "odrl:operator": { "@id": "odrl:eq" },
                  "odrl:rightOperand": "cx.core.not-accepted:1"
                }""")).json();
    }

    private String catalogResponse(final String edcAssetId, final String offerId, final String policyConstraint) {
        final String rawJson = """
                {
                  "@id": "%s",
                  "@type": "dcat:Catalog",
                  "dcat:dataset": {
                    "@id": "%s",
                    "@type": "dcat:Dataset",
                    "odrl:hasPolicy": {
                      "@id": "%s",
                      "@type": "odrl:Offer",
                      "odrl:permission": {
                        "odrl:action": { "odrl:type": "use" },
                        "odrl:constraint": %s
                      },
                      "odrl:prohibition": [],
                      "odrl:obligation": []
                    },
                    "dcat:distribution": [
                      {
                        "@type": "dcat:Distribution",
                        "dct:format": { "@id": "HttpData-PULL" },
                        "dcat:accessService": {
                          "@id": "%s",
                          "@type": "dcat:DataService",
                          "dct:terms": "connector",
                          "dct:endpointUrl": "%s/api/v1/dsp"
                        }
                      }
                    ],
                    "id": "%s"
                  },
                  "dcat:service": {
                    "@id": "%s",
                    "@type": "dcat:DataService",
                    "dct:terms": "connector",
                    "dct:endpointUrl": "%s/api/v1/dsp"
                  },
                  "participantId": "BPNL00000000MOCK",
                  "dspace:participantId": "BPNL00000000MOCK",
                  "@context": %s
                }
                """;
        return rawJson.formatted(randomId(), edcAssetId, offerId, policyConstraint, randomId(), baseUrl(),
                edcAssetId, randomId(), baseUrl(), JUPITER_EDC_CONTEXT);
    }

    private String idResponse(final String id) {
        return """
                {
                  "@type": "IdResponse",
                  "@id": "%s",
                  "createdAt": 1686830151573,
                  "@context": %s
                }
                """.formatted(id, JUPITER_EDC_CONTEXT);
    }

    private String negotiationResponse(final String negotiationId, final String contractAgreementId) {
        return """
                {
                  "@type": "ContractNegotiation",
                  "@id": "%s",
                  "type": "CONSUMER",
                  "protocol": "dataspace-protocol-http",
                  "state": "FINALIZED",
                  "counterPartyAddress": "%s/api/v1/dsp",
                  "counterPartyId": "BPNL00000000MOCK",
                  "callbackAddresses": [],
                  "contractAgreementId": "%s",
                  "@context": %s
                }
                """.formatted(negotiationId, baseUrl(), contractAgreementId, JUPITER_EDC_CONTEXT);
    }

    private String transferResponse(final String transferProcessId, final String edcAssetId,
            final String contractAgreementId) {
        return """
                {
                  "@id": "%s",
                  "@type": "TransferProcess",
                  "state": "COMPLETED",
                  "stateTimestamp": 1688024335567,
                  "type": "CONSUMER",
                  "correlationId": "%s",
                  "assetId": "%s",
                  "contractId": "%s",
                  "transferType": "HttpData-PULL",
                  "dataDestination": {
                    "@type": "DataAddress",
                    "type": "HttpProxy"
                  },
                  "@context": %s
                }
                """.formatted(transferProcessId, transferProcessId, edcAssetId, contractAgreementId,
                JUPITER_EDC_CONTEXT);
    }

    private String stateResponse(final String type, final String state) {
        return """
                {
                  "@type": "%s",
                  "state": "%s",
                  "@context": %s
                }
                """.formatted(type, state, JUPITER_EDC_CONTEXT);
    }

    private String json(final String rawJson) {
        return RecursiveJsonPreconditions.json(rawJson).json();
    }

    private String base64(final String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String randomUrn() {
        return "urn:uuid:" + randomId();
    }

    private String randomId() {
        return UUID.randomUUID().toString();
    }

    public record Shell(String globalAssetId, String bpnl, String idShort, List<Submodel> submodels) {
    }

    public record Submodel(String idShort, String semanticId, String edcAssetId, String path, String payload,
                           boolean includeDspEndpoint) {

        public Submodel(final String idShort, final String semanticId, final String edcAssetId, final String path,
                final String payload) {
            this(idShort, semanticId, edcAssetId, path, payload, true);
        }

    }
}
