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
package org.eclipse.tractusx.irs.edc.client.transformer;

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.JSON_LD_OBJECT_MAPPER;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.apicatalog.jsonld.document.JsonDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.edc.api.transformer.JsonObjectFromCallbackAddressTransformer;
import org.eclipse.edc.api.transformer.JsonObjectToCallbackAddressTransformer;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.connector.api.management.contractnegotiation.transform.JsonObjectToContractRequestTransformer;
import org.eclipse.edc.connector.core.base.agent.NoOpParticipantIdMapper;
import org.eclipse.edc.core.transform.TransformerContextImpl;
import org.eclipse.edc.core.transform.transformer.dcat.from.JsonObjectFromCatalogTransformer;
import org.eclipse.edc.core.transform.transformer.dcat.from.JsonObjectFromDataServiceTransformer;
import org.eclipse.edc.core.transform.transformer.dcat.from.JsonObjectFromDatasetTransformer;
import org.eclipse.edc.core.transform.transformer.dcat.from.JsonObjectFromDistributionTransformer;
import org.eclipse.edc.core.transform.transformer.edc.from.JsonObjectFromAssetTransformer;
import org.eclipse.edc.core.transform.transformer.edc.from.JsonObjectFromCriterionTransformer;
import org.eclipse.edc.core.transform.transformer.edc.from.JsonObjectFromDataAddressTransformer;
import org.eclipse.edc.core.transform.transformer.edc.from.JsonObjectFromQuerySpecTransformer;
import org.eclipse.edc.core.transform.transformer.edc.to.JsonObjectToActionTransformer;
import org.eclipse.edc.core.transform.transformer.edc.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.core.transform.transformer.edc.to.JsonObjectToCriterionTransformer;
import org.eclipse.edc.core.transform.transformer.edc.to.JsonObjectToQuerySpecTransformer;
import org.eclipse.edc.core.transform.transformer.edc.to.JsonValueToGenericTypeTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToConstraintTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToDutyTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToOperatorTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToPermissionTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToPolicyTransformer;
import org.eclipse.edc.core.transform.transformer.odrl.to.JsonObjectToProhibitionTransformer;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.protocol.dsp.negotiation.transform.from.JsonObjectFromContractNegotiationTransformer;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.tractusx.irs.edc.client.model.ContractOfferDescription;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Transformer to convert between EDC models and JSON-LD.
 */
@Component("irsEdcClientEdcTransformer")
@SuppressWarnings("PMD.ExcessiveImports")
public class EdcTransformer {
    private final JsonObjectToCatalogTransformer jsonObjectToCatalogTransformer;
    private final JsonObjectFromNegotiationInitiateDtoTransformer jsonObjectFromNegotiationInitiateDtoTransformer;
    private final JsonObjectFromTransferProcessRequestTransformer jsonObjectFromTransferProcessRequestTransformer;
    private final JsonObjectFromContractOfferDescriptionTransformer jsonObjectFromContractOfferDescriptionTransformer;
    private final JsonObjectFromCatalogRequestTransformer jsonObjectFromCatalogRequestTransformer;
    private final TitaniumJsonLd titaniumJsonLd;
    private final TransformerContextImpl transformerContext;
    private final JsonObjectFromAssetTransformer jsonObjectFromAssetTransformer;
    private final JsonObjectToIrsPolicyTransformer jsonObjectToIrsPolicyTransformer;

    public EdcTransformer(@Qualifier(JSON_LD_OBJECT_MAPPER) final ObjectMapper objectMapper,
            final TitaniumJsonLd titaniumJsonLd, final TypeTransformerRegistry typeTransformerRegistry) {
        this.titaniumJsonLd = titaniumJsonLd;
        transformerContext = new TransformerContextImpl(typeTransformerRegistry);
        final NoOpParticipantIdMapper participantIdMapper = new NoOpParticipantIdMapper();
        final JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(Map.of());

        jsonObjectToCatalogTransformer = new JsonObjectToCatalogTransformer();
        jsonObjectToIrsPolicyTransformer = new JsonObjectToIrsPolicyTransformer(objectMapper);

        jsonObjectFromNegotiationInitiateDtoTransformer = new JsonObjectFromNegotiationInitiateDtoTransformer(
                jsonBuilderFactory);
        jsonObjectFromTransferProcessRequestTransformer = new JsonObjectFromTransferProcessRequestTransformer(
                jsonBuilderFactory);
        jsonObjectFromContractOfferDescriptionTransformer = new JsonObjectFromContractOfferDescriptionTransformer(
                jsonBuilderFactory);
        jsonObjectFromCatalogRequestTransformer = new JsonObjectFromCatalogRequestTransformer(jsonBuilderFactory);
        jsonObjectFromAssetTransformer = new JsonObjectFromAssetTransformer(jsonBuilderFactory, objectMapper);

        // JSON to Object
        typeTransformerRegistry.register(jsonObjectToCatalogTransformer);
        typeTransformerRegistry.register(new JsonObjectToPolicyTransformer(participantIdMapper));
        typeTransformerRegistry.register(new JsonValueToGenericTypeTransformer(objectMapper));
        typeTransformerRegistry.register(new JsonObjectToDataServiceTransformer());
        typeTransformerRegistry.register(new JsonObjectToConstraintTransformer());
        typeTransformerRegistry.register(new JsonObjectToDatasetTransformer());
        typeTransformerRegistry.register(new JsonObjectToContractRequestTransformer());
        typeTransformerRegistry.register(new JsonObjectToPermissionTransformer());
        typeTransformerRegistry.register(new JsonObjectToActionTransformer());
        typeTransformerRegistry.register(new JsonObjectToDistributionTransformer());
        typeTransformerRegistry.register(new JsonObjectToProhibitionTransformer());
        typeTransformerRegistry.register(new JsonObjectToDutyTransformer());
        typeTransformerRegistry.register(new JsonObjectToAssetTransformer());
        typeTransformerRegistry.register(new JsonObjectToQuerySpecTransformer());
        typeTransformerRegistry.register(new JsonObjectToCriterionTransformer());
        typeTransformerRegistry.register(new JsonObjectToOperatorTransformer());
        typeTransformerRegistry.register(new JsonObjectToCallbackAddressTransformer());
        typeTransformerRegistry.register(jsonObjectToIrsPolicyTransformer);

        // JSON from Object
        typeTransformerRegistry.register(jsonObjectFromNegotiationInitiateDtoTransformer);
        typeTransformerRegistry.register(jsonObjectFromCatalogRequestTransformer);
        typeTransformerRegistry.register(jsonObjectFromTransferProcessRequestTransformer);
        typeTransformerRegistry.register(jsonObjectFromContractOfferDescriptionTransformer);
        typeTransformerRegistry.register(jsonObjectFromAssetTransformer);
        typeTransformerRegistry.register(new JsonObjectFromContractNegotiationTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromQuerySpecTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(
                new JsonObjectFromCatalogTransformer(jsonBuilderFactory, objectMapper, participantIdMapper));
        typeTransformerRegistry.register(new JsonObjectFromDatasetTransformer(jsonBuilderFactory, objectMapper));
        typeTransformerRegistry.register(new JsonObjectFromPolicyTransformer(jsonBuilderFactory, participantIdMapper));
        typeTransformerRegistry.register(new JsonObjectFromDistributionTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromDataServiceTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromCriterionTransformer(jsonBuilderFactory, objectMapper));
        typeTransformerRegistry.register(new JsonObjectFromDataAddressTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromCallbackAddressTransformer(jsonBuilderFactory));
    }

    public Catalog transformCatalog(final String jsonString, final Charset charset) {
        final Result<JsonObject> expand;
        expand = expandJsonLd(jsonString, charset);
        return jsonObjectToCatalogTransformer.transform(expand.getContent(), transformerContext);
    }

    public Result<JsonObject> expandJsonLd(final String jsonString, final Charset charset) {
        final Result<JsonObject> expand;
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(jsonString.getBytes(charset)))) {

            expand = titaniumJsonLd.expand(
                    JsonDocument.of(reader.read()).getJsonContent().orElseThrow().asJsonObject());
        }
        return expand;
    }

    public JsonObject transformNegotiationRequestToJson(final NegotiationRequest negotiationRequest) {
        final JsonObject transform = jsonObjectFromNegotiationInitiateDtoTransformer.transform(negotiationRequest,
                transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public JsonObject transformTransferProcessRequestToJson(final TransferProcessRequest transferProcessRequest) {
        final JsonObject transform = jsonObjectFromTransferProcessRequestTransformer.transform(transferProcessRequest,
                transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public JsonObject transformContractOfferDescriptionToJson(final ContractOfferDescription contractOfferDescription) {

        final JsonObject transform = jsonObjectFromContractOfferDescriptionTransformer.transform(
                contractOfferDescription, transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public JsonObject transformCatalogRequestToJson(final CatalogRequest catalogRequest) {
        final JsonObject transform = jsonObjectFromCatalogRequestTransformer.transform(catalogRequest,
                transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public JsonObject transformAssetToJson(final Asset assetRequest) {
        final JsonObject transform = jsonObjectFromAssetTransformer.transform(assetRequest, transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public org.eclipse.tractusx.irs.edc.client.policy.@Nullable Policy transformToIrsPolicy(final JsonObject body) {
        return jsonObjectToIrsPolicyTransformer.transform(body, transformerContext);
    }
}
