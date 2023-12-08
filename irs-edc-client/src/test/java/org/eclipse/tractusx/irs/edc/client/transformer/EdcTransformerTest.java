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
package org.eclipse.tractusx.irs.edc.client.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.objectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.ContractOfferDescription;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EdcTransformerTest {

    private TitaniumJsonLd jsonLd;
    private EdcTransformer edcTransformer;

    private static @NotNull String getCatalogAsString() {
        return """
                {
                    "@id": "78ff625c-0c05-4014-965c-bd3d0a6a0de0",
                    "@type": "dcat:Catalog",
                    "dcat:dataset": {
                        "@id": "58505404-4da1-427a-82aa-b79482bcd1f0",
                        "@type": "dcat:Dataset",
                        "odrl:hasPolicy": {
                            "@id": "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c1",
                            "@type": "odrl:Set",
                            "odrl:permission": {
                                "odrl:target": "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a",
                                "odrl:action": {
                                    "odrl:type": "USE"
                                },
                                "odrl:constraint": {
                                    "odrl:or": {
                                        "odrl:leftOperand": "idsc:PURPOSE",
                                        "odrl:operator": {
                                            "@id": "odrl:eq"
                                        },
                                        "odrl:rightOperand": "ID 3.0 Trace"
                                    }
                                }
                            },
                            "odrl:prohibition": [],
                            "odrl:obligation": [],
                            "odrl:target": "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a"
                        },
                        "dcat:distribution": [
                            {
                                "@type": "dcat:Distribution",
                                "dct:format": {
                                    "@id": "HttpProxy"
                                },
                                "dcat:accessService": "4ba1faa1-7f1a-4fb7-a41c-317f450e7443"
                            },
                            {
                                "@type": "dcat:Distribution",
                                "dct:format": {
                                    "@id": "AmazonS3"
                                },
                                "dcat:accessService": "4ba1faa1-7f1a-4fb7-a41c-317f450e7443"
                            }
                        ],
                        "edc:description": "IRS EDC Demo Asset",
                        "edc:id": "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a"
                    },
                    "dcat:service": {
                        "@id": "4ba1faa1-7f1a-4fb7-a41c-317f450e7443",
                        "@type": "dcat:DataService",
                        "dct:terms": "connector",
                        "dct:endpointUrl": "https://irs-test-controlplane-provider.dev.demo.catena-x.net/api/v1/dsp"
                    },
                    "edc:participantId": "BPNL00000003CRHK",
                    "@context": {
                        "dct": "https://purl.org/dc/terms/",
                        "tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                        "edc": "https://w3id.org/edc/v0.0.1/ns/",
                        "dcat": "https://www.w3.org/ns/dcat/",
                        "odrl": "http://www.w3.org/ns/odrl/2/",
                        "dspace": "https://w3id.org/dspace/v0.8/"
                    }
                }
                """;
    }

    private static Dataset createDataset() {
        final String edcNamespace = "https://w3id.org/edc/v0.0.1/ns/";
        final String target = "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a";
        final String distributionId = "4ba1faa1-7f1a-4fb7-a41c-317f450e7443";
        final String datasetId = "58505404-4da1-427a-82aa-b79482bcd1f0";
        final String offerId = "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c1";

        final HashMap<String, Object> properties = new HashMap<>();
        properties.put(edcNamespace + "description", "IRS EDC Demo Asset");
        properties.put(edcNamespace + "id", target);
        final Policy policy = createPolicy(target);
        final Distribution httpProxy = Distribution.Builder.newInstance()
                                                           .format("HttpProxy")
                                                           .dataService(DataService.Builder.newInstance()
                                                                                           .id(distributionId)
                                                                                           .build())
                                                           .build();
        final Distribution amazonS3 = Distribution.Builder.newInstance()
                                                          .format("AmazonS3")
                                                          .dataService(DataService.Builder.newInstance()
                                                                                          .id(distributionId)
                                                                                          .build())
                                                          .build();
        final List<Distribution> distributions = List.of(httpProxy, amazonS3);
        return Dataset.Builder.newInstance()
                              .id(datasetId)
                              .offer(offerId, policy)
                              .distributions(distributions)
                              .properties(properties)
                              .build();
    }

    private static NegotiationRequest createNegotiation(final String consumerBPN, final String providerBPN,
            final String providerConnector, final String protocol, final String offerId, final String assetId) {
        final Policy policy = createPolicy(assetId);

        final ContractOfferDescription contractOfferDescription = ContractOfferDescription.builder()
                                                                                          .offerId(offerId)
                                                                                          .assetId(assetId)
                                                                                          .policy(policy)
                                                                                          .build();
        return NegotiationRequest.builder()
                                 .connectorId(consumerBPN)
                                 .connectorId(providerBPN)
                                 .connectorAddress(providerConnector)
                                 .protocol(protocol)
                                 .offer(contractOfferDescription)
                                 .build();
    }

    private static Policy createPolicy(final String assetId) {
        final Action action = Action.Builder.newInstance().type("USE").build();
        final AtomicConstraint atomicConstraint = AtomicConstraint.Builder.newInstance()
                                                                          .leftExpression(
                                                                                  new LiteralExpression("idsc:PURPOSE"))
                                                                          .operator(Operator.EQ)
                                                                          .rightExpression(
                                                                                  new LiteralExpression("ID 3.0 Trace"))
                                                                          .build();
        final OrConstraint orConstraint = OrConstraint.Builder.newInstance().constraint(atomicConstraint).build();
        final Permission permission = Permission.Builder.newInstance()
                                                        .action(action)
                                                        .constraint(orConstraint)
                                                        .target(assetId)
                                                        .build();
        return Policy.Builder.newInstance().permission(permission).target(assetId).build();
    }

    @BeforeEach
    void setUp() {
        jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");
        jsonLd.registerNamespace("dct", "https://purl.org/dc/terms/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("dcat", "https://www.w3.org/ns/dcat/");
        jsonLd.registerNamespace("dspace", "https://w3id.org/dspace/v0.8/");

        ObjectMapper objectMapper = objectMapper();
        edcTransformer = new EdcTransformer(objectMapper, jsonLd);
    }

    @Test
    void shouldDeserializeJsonLdResponseToCatalog() throws JsonProcessingException {
        // Arrange
        final String key = "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c1";
        final String catalogAsString = getCatalogAsString();
        final DataService expectedDataService = DataService.Builder.newInstance()
                                                                   .id("4ba1faa1-7f1a-4fb7-a41c-317f450e7443")
                                                                   .terms("connector")
                                                                   .endpointUrl(
                                                                           "https://irs-test-controlplane-provider.dev.demo.catena-x.net/api/v1/dsp")
                                                                   .build();
        // Act
        final Catalog actualCatalog = edcTransformer.transformCatalog(catalogAsString, StandardCharsets.UTF_8);

        // Assert
        assertThat(actualCatalog).isNotNull();
        assertThat(actualCatalog.getId()).isEqualTo("78ff625c-0c05-4014-965c-bd3d0a6a0de0");
        assertThat(actualCatalog.getProperties()).containsEntry("https://w3id.org/edc/v0.0.1/ns/participantId",
                "BPNL00000003CRHK");

        final Dataset expectedDataset = createDataset();
        final Distribution expectedDistribution = expectedDataset.getDistributions().get(0);
        final Policy expectedPolicy = expectedDataset.getOffers().get(key);
        final String expectedPermission = objectMapper().writeValueAsString(expectedPolicy.getPermissions().get(0));

        final Dataset actualDataset = actualCatalog.getDatasets().get(0);
        final Distribution actualDistribution = actualDataset.getDistributions().get(0);
        final Policy actualPolicy = actualDataset.getOffers().get(key);
        final String actualPermission = objectMapper().writeValueAsString(actualPolicy.getPermissions().get(0));

        assertThat(actualDataset.getId()).isEqualTo(expectedDataset.getId());
        assertThat(actualDataset.getProperties()).isEqualTo(expectedDataset.getProperties());
        assertThat(actualDistribution.getFormat()).isEqualTo(expectedDistribution.getFormat());
        assertThat(actualDistribution.getDataService().getId()).isEqualTo(
                expectedDistribution.getDataService().getId());
        assertThat(actualPolicy.getTarget()).isEqualTo(expectedPolicy.getTarget());
        assertThat(actualPermission).isEqualTo(expectedPermission);
        assertThat(actualCatalog.getDataServices().get(0)).isEqualTo(expectedDataService);
        assertThat(actualCatalog.getContractOffers()).isNull();
    }

    @Test
    void shouldSerializeContractOfferDescriptionToJsonObject() {
        final String offerId = "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c";
        final String assetId = "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a";
        final ContractOfferDescription contractOfferDescription = ContractOfferDescription.builder()
                                                                                          .offerId(offerId)
                                                                                          .policy(createPolicy(assetId))
                                                                                          .assetId(assetId)
                                                                                          .build();
        final JsonObject contractOfferDescriptionToJson = edcTransformer.transformContractOfferDescriptionToJson(
                contractOfferDescription);
        final Optional<JsonObject> optional = jsonLd.compact(contractOfferDescriptionToJson).asOptional();
        assertThat(optional).isPresent();
        assertThat(optional.get()).isNotEmpty();
        assertThat(optional.get()).contains(entry("@type", Json.createValue("edc:ContractOfferDescription")));
        assertThat(optional.get()).contains(entry("edc:offerId", Json.createValue(offerId)));
        assertThat(optional.get()).contains(entry("edc:assetId", Json.createValue(assetId)));
    }

    @Test
    void shouldSerializeNegotiationInitiateDtoToJsonObject() {
        final String consumerBPN = "BPNL00000003CRHK";
        final String providerBPN = "BPNL00000003CRHK";
        final String providerConnector = "https://provider.edc/api/v1/dsp";
        final String protocol = "dataspace-protocol-http";
        final String offerId = "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c";
        final String assetId = "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a";
        final NegotiationRequest negotiationInitiateRequestDto = createNegotiation(consumerBPN, providerBPN,
                providerConnector, protocol, offerId, assetId);

        final JsonObject negotiationJson = edcTransformer.transformNegotiationRequestToJson(
                negotiationInitiateRequestDto);

        assertThat(negotiationJson).isNotEmpty()
                                   .contains(entry("edc:connectorAddress", Json.createValue(providerConnector)))
                                   .contains(entry("edc:connectorId", Json.createValue(consumerBPN)))
                                   .contains(entry("edc:protocol", Json.createValue(protocol)));
    }

    @Test
    void shouldSerializeTransferRequestDtoToJsonObject() {
        final String providerConnector = "https://provider.edc/api/v1/dsp";
        final String protocol = "dataspace-protocol-http";
        final String contractId = "7681f966-36ea-4542-b5ea-0d0db81967de:35c78eca-db53-442c-9e01-467fc22c9434-55840861-5d7f-444b-972a-6e8b78552d8a:66131c58-32af-4df0-825d-77f7df6017c";
        final String assetId = "urn:uuid:35c78eca-db53-442c-9e01-467fc22c9434-urn:uuid:55840861-5d7f-444b-972a-6e8b78552d8a";
        final String connectorId = "BPNL00000003CRHK";
        final DataAddress dataAddress = DataAddress.Builder.newInstance().type("HttpProxy").build();
        final TransferProcessRequest transferRequestDto = TransferProcessRequest.builder()
                                                                                .assetId(assetId)
                                                                                .connectorId(connectorId)
                                                                                .connectorAddress(providerConnector)
                                                                                .protocol(protocol)
                                                                                .contractId(contractId)
                                                                                .managedResources(false)
                                                                                .dataDestination(dataAddress)
                                                                                .privateProperties(
                                                                                        Map.of("receiverHttpEndpoint",
                                                                                                "https://backend.app/endpoint-data-reference"))
                                                                                .build();
        final JsonObject jsonObject = edcTransformer.transformTransferProcessRequestToJson(transferRequestDto);
        final Optional<JsonObject> optional = jsonLd.compact(jsonObject).asOptional();
        assertThat(optional).isPresent();
        assertThat(optional.get().getJsonObject("edc:privateProperties")).isNotEmpty();
        assertThat(optional.get().getJsonObject("edc:privateProperties")).containsKey("edc:receiverHttpEndpoint");
        assertThat(
                optional.get().getJsonObject("edc:privateProperties").getString("edc:receiverHttpEndpoint")).isEqualTo(
                "https://backend.app/endpoint-data-reference");
    }

    @Test
    void shouldSerializeCatalogRequestToJsonObject() {
        final String providerConnector = "https://provider.edc/api/v1/dsp";
        final String protocol = "dataspace-protocol-http";
        final CatalogRequest catalogRequest = CatalogRequest.Builder.newInstance()
                                                                    .counterPartyAddress(providerConnector)
                                                                    .protocol(protocol)
                                                                    .build();
        final JsonObject requestJson = edcTransformer.transformCatalogRequestToJson(catalogRequest);
        final Optional<JsonObject> optional = jsonLd.compact(requestJson).asOptional();
        assertThat(optional).isPresent();
        assertThat(optional.get()).isNotEmpty();
        assertThat(optional.get()).contains(entry("@type", Json.createValue("edc:CatalogRequest")));
    }
}