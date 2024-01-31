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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.apicatalog.jsonld.document.JsonDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.edc.core.transform.TransformerContextImpl;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromAssetTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromCatalogTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromCriterionTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDataAddressTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDataServiceTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDatasetTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromDistributionTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.core.transform.transformer.from.JsonObjectFromQuerySpecTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToActionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToCatalogTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToConstraintTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToCriterionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDataServiceTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDatasetTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDistributionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToDutyTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToOperatorTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToPermissionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToPolicyTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToProhibitionTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonObjectToQuerySpecTransformer;
import org.eclipse.edc.core.transform.transformer.to.JsonValueToGenericTypeTransformer;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest;
import org.eclipse.tractusx.irs.edc.client.asset.transformer.JsonObjectFromAssetRequestTransformer;
import org.eclipse.tractusx.irs.edc.client.model.ContractOfferDescription;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
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
    private final JsonObjectFromAssetTransformer jsonObjectFromAssetTransformer;
    private final TitaniumJsonLd titaniumJsonLd;
    private final TransformerContextImpl transformerContext;
    private final JsonObjectFromAssetRequestTransformer jsonObjectFromAssetRequestTransformer;

    public EdcTransformer(@Qualifier("jsonLdObjectMapper") final ObjectMapper objectMapper,
            final TitaniumJsonLd titaniumJsonLd) {
        this.titaniumJsonLd = titaniumJsonLd;
        this.titaniumJsonLd.registerNamespace("type", "https://w3id.org/edc/v0.0.1/ns/dataAddress/type");
        this.titaniumJsonLd.registerNamespace("baseUrl", "https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl");
        this.titaniumJsonLd.registerNamespace("proxyMethod", "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod");
        this.titaniumJsonLd.registerNamespace("proxyBody", "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody");
        this.titaniumJsonLd.registerNamespace("method", "https://w3id.org/edc/v0.0.1/ns/dataAddress/method");
        this.titaniumJsonLd.registerNamespace("asset", "https://w3id.org/edc/v0.0.1/ns/asset");
        this.titaniumJsonLd.registerNamespace("dataAddress", "https://w3id.org/edc/v0.0.1/ns/dataAddress");
        this.titaniumJsonLd.registerNamespace("proxyPath", "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyPath");
        this.titaniumJsonLd.registerNamespace("proxyQueryParams", "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyQueryParams");
        final JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(Map.of());

        jsonObjectFromNegotiationInitiateDtoTransformer = new JsonObjectFromNegotiationInitiateDtoTransformer(
                jsonBuilderFactory);
        jsonObjectToCatalogTransformer = new JsonObjectToCatalogTransformer();
        jsonObjectFromTransferProcessRequestTransformer = new JsonObjectFromTransferProcessRequestTransformer(
                jsonBuilderFactory);
        jsonObjectFromContractOfferDescriptionTransformer = new JsonObjectFromContractOfferDescriptionTransformer(
                jsonBuilderFactory);
        jsonObjectFromCatalogRequestTransformer = new JsonObjectFromCatalogRequestTransformer(jsonBuilderFactory);
        jsonObjectFromAssetTransformer = new JsonObjectFromAssetTransformer(jsonBuilderFactory, objectMapper);
        jsonObjectFromAssetRequestTransformer = new JsonObjectFromAssetRequestTransformer(jsonBuilderFactory, objectMapper);

        final TypeTransformerRegistry typeTransformerRegistry = new TypeTransformerRegistryImpl();
        transformerContext = new TransformerContextImpl(typeTransformerRegistry);

        // JSON to Object
        typeTransformerRegistry.register(jsonObjectToCatalogTransformer);
        typeTransformerRegistry.register(new JsonValueToGenericTypeTransformer(objectMapper));
        typeTransformerRegistry.register(new JsonObjectToDataServiceTransformer());
        typeTransformerRegistry.register(new JsonObjectToConstraintTransformer());
        typeTransformerRegistry.register(new JsonObjectToDatasetTransformer());
        typeTransformerRegistry.register(new JsonObjectToPolicyTransformer());
        typeTransformerRegistry.register(new JsonObjectToPermissionTransformer());
        typeTransformerRegistry.register(new JsonObjectToActionTransformer());
        typeTransformerRegistry.register(new JsonObjectToDistributionTransformer());
        typeTransformerRegistry.register(new JsonObjectToProhibitionTransformer());
        typeTransformerRegistry.register(new JsonObjectToDutyTransformer());
        typeTransformerRegistry.register(new JsonObjectToAssetTransformer());
        typeTransformerRegistry.register(new JsonObjectToQuerySpecTransformer());
        typeTransformerRegistry.register(new JsonObjectToCriterionTransformer());
        typeTransformerRegistry.register(new JsonObjectToOperatorTransformer());
        // JSON from Object
        typeTransformerRegistry.register(jsonObjectFromNegotiationInitiateDtoTransformer);
        typeTransformerRegistry.register(jsonObjectFromCatalogRequestTransformer);
        typeTransformerRegistry.register(jsonObjectFromTransferProcessRequestTransformer);
        typeTransformerRegistry.register(jsonObjectFromContractOfferDescriptionTransformer);
        typeTransformerRegistry.register(new JsonObjectFromQuerySpecTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromCatalogTransformer(jsonBuilderFactory, objectMapper));
        typeTransformerRegistry.register(new JsonObjectFromDatasetTransformer(jsonBuilderFactory, objectMapper));
        typeTransformerRegistry.register(new JsonObjectFromPolicyTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromDistributionTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(new JsonObjectFromDataServiceTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(jsonObjectFromAssetTransformer);
        typeTransformerRegistry.register(new JsonObjectFromCriterionTransformer(jsonBuilderFactory, objectMapper));
        typeTransformerRegistry.register(new JsonObjectFromDataAddressTransformer(jsonBuilderFactory));
        typeTransformerRegistry.register(jsonObjectFromAssetRequestTransformer);
    }

    public Catalog transformCatalog(final String jsonString, final Charset charset) {
        final Result<JsonObject> expand;
        try (JsonReader reader = Json.createReader(new ByteArrayInputStream(jsonString.getBytes(charset)))) {

            expand = titaniumJsonLd.expand(
                    JsonDocument.of(reader.read()).getJsonContent().orElseThrow().asJsonObject());
        }
        return jsonObjectToCatalogTransformer.transform(expand.getContent(), transformerContext);
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

    public JsonObject transformAssetToJson(Asset asset) {
        final JsonObject transform = jsonObjectFromAssetTransformer.transform(asset, transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }

    public JsonObject transformAssetRequestToJson(AssetRequest assetRequest) {
        final JsonObject transform = jsonObjectFromAssetRequestTransformer.transform(assetRequest, transformerContext);
        return titaniumJsonLd.compact(transform).asOptional().orElseThrow();
    }
}
