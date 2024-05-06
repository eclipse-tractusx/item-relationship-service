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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transformer to convert JSON-LD to Policy.
 */
public class JsonObjectToIrsPolicyTransformer extends AbstractJsonLdTransformer<JsonObject, Policy> {

    private final ObjectMapper objectMapper;

    protected JsonObjectToIrsPolicyTransformer(final ObjectMapper objectMapper) {
        super(JsonObject.class, Policy.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public @Nullable Policy transform(@NotNull final JsonObject jsonObject,
            @NotNull final TransformerContext transformerContext) {
        final Policy.PolicyBuilder builder = Policy.builder();
        builder.policyId(getId(jsonObject));

        this.visitProperties(jsonObject, key -> v -> {
            try {
                final Object result = objectMapper.readerFor(Policy.class).readValue(v.asJsonObject().toString());
                builder.permissions(((Policy) result).getPermissions());
            } catch (JsonProcessingException e) {
                throw new JsonParseException(e);
            }
        });

        return builder.build();
    }

    private String getId(final JsonObject jsonObject) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(jsonObject.toString());
            return jsonNode.path("@id").asText();
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }
}
