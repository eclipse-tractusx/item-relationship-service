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

import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATASET_TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DISTRIBUTION_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_POLICY_ATTRIBUTE;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts from a DCAT dataset as a {@link JsonObject} in JSON-LD expanded form to a {@link Dataset}.
 */
public class JsonObjectToDatasetTransformer extends AbstractJsonLdTransformer<JsonObject, Dataset> {

    public JsonObjectToDatasetTransformer() {
        super(JsonObject.class, Dataset.class);
    }

    @Override
    public @Nullable Dataset transform(final @NotNull JsonObject object, final @NotNull TransformerContext context) {
        final var builder = Dataset.Builder.newInstance();

        builder.id(nodeId(object));
        visitProperties(object, (key, value) -> transformProperties(key, value, builder, context));

        return builderResult(builder::build, context);
    }

    private void transformProperties(final String key, final JsonValue value, final Dataset.Builder builder,
            final TransformerContext context) {
        switch (key) {
            case ODRL_POLICY_ATTRIBUTE -> transformPolicies(value, builder, context);
            case DCAT_DISTRIBUTION_ATTRIBUTE ->
                    transformArrayOrObject(value, Distribution.class, builder::distribution, context);
            default -> builder.property(key, transformGenericProperty(value, context));
        }
    }

    private void transformPolicies(final JsonValue value, final Dataset.Builder builder,
            final TransformerContext context) {
        if (value instanceof JsonObject object) {
            final var offerId = nodeId(object);
            final var policy = context.transform(object, Policy.class);
            builder.offer(offerId, policy);
        } else if (value instanceof JsonArray array) {
            array.forEach(entry -> transformPolicies(entry, builder, context));
        } else {
            context.problem()
                   .unexpectedType()
                   .type(DCAT_DATASET_TYPE)
                   .property(ODRL_POLICY_ATTRIBUTE)
                   .actual(value == null ? "null" : value.getValueType().toString())
                   .expected(JsonValue.ValueType.OBJECT)
                   .expected(JsonValue.ValueType.ARRAY)
                   .report();
        }
    }
}
