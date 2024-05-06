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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transformer to convert NegotiationRequest to JSON-LD.
 */
public class JsonObjectFromNegotiationInitiateDtoTransformer
        extends AbstractJsonLdTransformer<NegotiationRequest, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromNegotiationInitiateDtoTransformer(final JsonBuilderFactory jsonFactory) {
        super(NegotiationRequest.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull final NegotiationRequest dto,
            @NotNull final TransformerContext context) {
        final JsonObjectBuilder builder = this.jsonFactory.createObjectBuilder();
        builder.add(NegotiationRequest.NEGOTIATION_COUNTER_PARTY_ADDRESS, dto.getCounterPartyAddress())
               .add(NegotiationRequest.NEGOTIATION_COUNTER_PARTY_ID, dto.getCounterPartyId())
               .add(NegotiationRequest.NEGOTIATION_POLICY, context.transform(dto.getContractOffer(), JsonObject.class))
               .add(NegotiationRequest.NEGOTIATION_PROTOCOL, dto.getProtocol());
        Optional.ofNullable(dto.getCallbackAddresses())
                .ifPresent(s -> builder.add(NegotiationRequest.NEGOTIATION_CALLBACK_ADDRESSES,
                        asArray(dto.getCallbackAddresses(), context)));
        return builder.build();
    }

    private JsonArrayBuilder asArray(final List<CallbackAddress> callbackAddresses, final TransformerContext context) {
        final JsonArrayBuilder builder = Objects.requireNonNull(this.jsonFactory.createArrayBuilder());
        callbackAddresses.stream()
                         .map(callbackAddress -> context.transform(callbackAddress, JsonObject.class))
                         .forEach(builder::add);
        return builder;
    }
}
