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

import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATASET_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATA_SERVICE_ATTRIBUTE;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts from a DCAT catalog as a {@link JsonObject} in JSON-LD expanded form to a {@link Catalog}.
 */
public class JsonObjectToCatalogTransformer extends AbstractJsonLdTransformer<JsonObject, Catalog> {

    public JsonObjectToCatalogTransformer() {
        super(JsonObject.class, Catalog.class);
    }

    @Override
    public @Nullable Catalog transform(final @NotNull JsonObject object, final @NotNull TransformerContext context) {
        final var builder = Catalog.Builder.newInstance();

        builder.id(nodeId(object));
        visitProperties(object, (key, value) -> transformProperties(key, value, builder, context));

        return builderResult(builder::build, context);
    }

    private void transformProperties(final String key, final JsonValue value, final Catalog.Builder builder,
            final TransformerContext context) {
        if (DCAT_DATASET_ATTRIBUTE.equals(key)) {
            transformArrayOrObject(value, Dataset.class, builder::dataset, context);
        } else if (DCAT_DATA_SERVICE_ATTRIBUTE.equals(key)) {
            transformArrayOrObject(value, DataService.class, builder::dataService, context);
        } else {
            builder.property(key, transformGenericProperty(value, context));
        }
    }
}
