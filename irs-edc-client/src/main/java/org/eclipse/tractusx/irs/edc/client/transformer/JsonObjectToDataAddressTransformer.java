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
package org.eclipse.tractusx.irs.edc.client.transformer;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.model.edr.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.edr.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts from a DCAT DataAddress as a {@link JsonObject} in JSON-LD expanded form to a {@link DataAddress}.
 */
public class JsonObjectToDataAddressTransformer extends AbstractJsonLdTransformer<JsonObject, DataAddress> {

    protected JsonObjectToDataAddressTransformer() {
        super(JsonObject.class, DataAddress.class);
    }

    @Override
    public @Nullable DataAddress transform(@NotNull final JsonObject jsonObject,
            @NotNull final TransformerContext transformerContext) {

        if (jsonObject == null) {
            transformerContext.reportProblem("Input JSON object is null.");
            return null;
        }

        try {
            final String endpointType = getStringFromNestedValue(jsonObject, "https://w3id.org/edc/v0.0.1/ns/endpointType");
            final String refreshEndpoint = getStringFromNestedValue(jsonObject,
                    "https://w3id.org/tractusx/auth/refreshEndpoint");
            final String audience = getStringFromNestedValue(jsonObject, "https://w3id.org/tractusx/auth/audience");
            final String type = getStringFromNestedValue(jsonObject, "https://w3id.org/edc/v0.0.1/ns/type");
            final String endpoint = getStringFromNestedValue(jsonObject, "https://w3id.org/edc/v0.0.1/ns/endpoint");
            final String refreshToken = getStringFromNestedValue(jsonObject, "https://w3id.org/tractusx/auth/refreshToken");
            final String expiresIn = getStringFromNestedValue(jsonObject, "https://w3id.org/tractusx/auth/expiresIn");
            final String authorization = getStringFromNestedValue(jsonObject, "https://w3id.org/edc/v0.0.1/ns/authorization");
            final String refreshAudience = getStringFromNestedValue(jsonObject,
                    "https://w3id.org/tractusx/auth/refreshAudience");

            final Properties properties = new Properties(null, null, null, endpointType, refreshEndpoint, audience, null,
                    null, type, endpoint, refreshToken, expiresIn, authorization, refreshAudience);

            return DataAddress.builder().properties(properties).build();
        } catch (JsonParseException e) {
            transformerContext.reportProblem("Error parsing JSON: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            transformerContext.reportProblem("Invalid argument: " + e.getMessage());
            return null;
        }
    }

    private String getStringFromNestedValue(final JsonObject jsonObject, final String key) {
        if (jsonObject.containsKey(key) && jsonObject.getJsonArray(key) != null) {
            final JsonArray valueArray = jsonObject.getJsonArray(key);
            if (!valueArray.isEmpty() && valueArray.getJsonObject(0).containsKey("@value")) {
                return valueArray.getJsonObject(0).getString("@value");
            }
        }
        return null;
    }
}
