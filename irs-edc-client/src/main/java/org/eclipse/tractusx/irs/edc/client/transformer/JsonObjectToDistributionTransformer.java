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

import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_ACCESS_SERVICE_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCT_FORMAT_ATTRIBUTE;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts from a DCAT distribution as a {@link JsonObject} in JSON-LD expanded form to a {@link Distribution}.
 */
public class JsonObjectToDistributionTransformer extends AbstractJsonLdTransformer<JsonObject, Distribution> {

    public JsonObjectToDistributionTransformer() {
        super(JsonObject.class, Distribution.class);
    }

    @Override
    public @Nullable Distribution transform(final @NotNull JsonObject object,
            final @NotNull TransformerContext context) {
        final var builder = Distribution.Builder.newInstance();
        visitProperties(object, (key, value) -> transformProperties(key, value, builder, context));
        return builderResult(builder::build, context);
    }

    private void transformProperties(final String key, final JsonValue value, final Distribution.Builder builder,
            final TransformerContext context) {
        if (DCAT_ACCESS_SERVICE_ATTRIBUTE.equals(key)) {
            final var dataServiceBuilder = DataService.Builder.newInstance();
            transformString(value, dataServiceBuilder::id, context);
            builder.dataService(dataServiceBuilder.build());
        } else if (DCT_FORMAT_ATTRIBUTE.equals(key)) {
            transformString(value, builder::format, context);
        }
    }
}
