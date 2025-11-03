/********************************************************************************
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.model.DSPVersionParamsRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transformer to convert CatalogRequest to JSON-LD.
 */
public class JsonObjectFromDspVersionParamsRequestTransformer extends AbstractJsonLdTransformer<DSPVersionParamsRequest, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    private static final String TX_CONTEXT = "https://w3id.org/tractusx/v0.0.1/ns/";
    private static final String EDC_CONTEXT = "https://w3id.org/edc/v0.0.1/ns/";
    private static final String TX_BPNL = "tx:bpnl";
    private static final String EDC_COUNTERPARTY_ADDRESS = "edc:counterPartyAddress";

    public JsonObjectFromDspVersionParamsRequestTransformer(final JsonBuilderFactory jsonFactory) {
        super(DSPVersionParamsRequest.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull final DSPVersionParamsRequest dto,
            @NotNull final TransformerContext context) {
        final JsonObject contextBuilder = jsonFactory.createObjectBuilder()
                                        .add("tx", TX_CONTEXT)
                                        .add("edc", EDC_CONTEXT)
                                        .build();

        return jsonFactory.createObjectBuilder()
                   .add("@context", contextBuilder)
                   .add(TX_BPNL, dto.providerBpnl())
                   .add(EDC_COUNTERPARTY_ADDRESS, dto.providerEdcUrl())
                   .build();
    }
}
