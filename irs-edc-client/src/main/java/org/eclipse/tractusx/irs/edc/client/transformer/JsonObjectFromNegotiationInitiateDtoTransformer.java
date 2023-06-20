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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.connector.api.management.contractnegotiation.model.NegotiationInitiateRequestDto;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonObjectFromNegotiationInitiateDtoTransformer
        extends AbstractJsonLdTransformer<NegotiationInitiateRequestDto, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromNegotiationInitiateDtoTransformer(JsonBuilderFactory jsonFactory) {
        super(NegotiationInitiateRequestDto.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull NegotiationInitiateRequestDto dto,
            @NotNull TransformerContext context) {
        JsonObjectBuilder builder = this.jsonFactory.createObjectBuilder();
        builder.add("@type", NegotiationInitiateRequestDto.TYPE)
               .add(NegotiationInitiateRequestDto.CONNECTOR_ADDRESS, dto.getConnectorAddress())
               .add(NegotiationInitiateRequestDto.CONNECTOR_ID, dto.getConnectorId())
               .add(NegotiationInitiateRequestDto.OFFER, context.transform(dto.getOffer(), JsonObject.class))
               .add(NegotiationInitiateRequestDto.PROTOCOL, dto.getProtocol());
        Optional.ofNullable(dto.getProviderId())
                .ifPresent(s -> builder.add(NegotiationInitiateRequestDto.PROVIDER_ID, dto.getProviderId()));
        Optional.ofNullable(dto.getCallbackAddresses())
                .ifPresent(s -> builder.add(NegotiationInitiateRequestDto.CALLBACK_ADDRESSES,
                        asArray(dto.getCallbackAddresses(), context)));
        Optional.ofNullable(dto.getConsumerId())
                .ifPresent(s -> builder.add(NegotiationInitiateRequestDto.CONSUMER_ID, dto.getConsumerId()));
        return builder.build();
    }

    private JsonArrayBuilder asArray(List<CallbackAddress> callbackAddresses, TransformerContext context) {
        JsonArrayBuilder bldr = Objects.requireNonNull(this.jsonFactory.createArrayBuilder());
        callbackAddresses.stream()
                         .map(callbackAddress -> context.transform(callbackAddress, JsonObject.class))
                         .forEach(bldr::add);
        return bldr;
    }
}
