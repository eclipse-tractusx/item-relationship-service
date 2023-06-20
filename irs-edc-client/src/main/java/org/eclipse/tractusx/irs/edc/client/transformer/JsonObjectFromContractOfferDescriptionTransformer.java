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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client.transformer;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.connector.api.management.contractnegotiation.model.ContractOfferDescription;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonObjectFromContractOfferDescriptionTransformer
        extends AbstractJsonLdTransformer<ContractOfferDescription, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromContractOfferDescriptionTransformer(JsonBuilderFactory jsonFactory) {
        super(ContractOfferDescription.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull ContractOfferDescription dto, @NotNull TransformerContext context) {
        JsonObjectBuilder builder = this.jsonFactory.createObjectBuilder();
        builder.add("@type", ContractOfferDescription.TYPE)
               .add(ContractOfferDescription.OFFER_ID, dto.getOfferId())
               .add(ContractOfferDescription.ASSET_ID, dto.getAssetId())
               .add("validity", dto.getValidity())
               .add(ContractOfferDescription.POLICY, context.transform(dto.getPolicy(), JsonObject.class));

        return builder.build();
    }
}
