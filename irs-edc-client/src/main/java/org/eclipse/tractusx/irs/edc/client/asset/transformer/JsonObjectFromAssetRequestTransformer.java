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

package org.eclipse.tractusx.irs.edc.client.asset.transformer;

import static org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest.ASSET_CREATION_ASSET;
import static org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest.ASSET_CREATION_DATA_ADDRESS;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.asset.model.AssetRequest;
import org.jetbrains.annotations.NotNull;

/**
 * Transformer to convert AssetRequest to JSON-LD.
 */

public class JsonObjectFromAssetRequestTransformer extends AbstractJsonLdTransformer<AssetRequest, JsonObject> {

    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromAssetRequestTransformer(final JsonBuilderFactory jsonFactory) {
        super(AssetRequest.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public JsonObject transform(@NotNull final AssetRequest assetRequest, @NotNull final TransformerContext context) {
        final JsonObjectBuilder builder = this.jsonFactory.createObjectBuilder();
        builder.add(ASSET_CREATION_ASSET,
                context.transform(assetRequest.asset(), JsonObject.class));
        builder.add(ASSET_CREATION_DATA_ADDRESS,
                context.transform(assetRequest.dataAddress(), JsonObject.class));
        return builder.build();
    }
}
