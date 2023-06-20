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
import org.eclipse.edc.connector.api.management.transferprocess.model.TransferRequestDto;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonObjectFromTransferRequestDtoTransformer
        extends AbstractJsonLdTransformer<TransferRequestDto, JsonObject> {
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromTransferRequestDtoTransformer(JsonBuilderFactory jsonFactory) {
        super(TransferRequestDto.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull TransferRequestDto dto, @NotNull TransformerContext context) {
        JsonObjectBuilder builder = this.jsonFactory.createObjectBuilder();
        builder.add("@type", TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_TYPE)
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_ASSET_ID, dto.getAssetId())
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_CONNECTOR_ADDRESS, dto.getConnectorAddress())
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_CONNECTOR_ID, dto.getConnectorId())
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_CONTRACT_ID, dto.getContractId())
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_DATA_DESTINATION,
                       context.transform(dto.getDataDestination(), JsonObject.class))
               .add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_PROTOCOL, dto.getProtocol());

        Optional.of(dto.isManagedResources())
                .ifPresent(s -> builder.add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_MANAGED_RESOURCES,
                        dto.isManagedResources()));
        Optional.ofNullable(dto.getCallbackAddresses())
                .ifPresent(s -> builder.add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_CALLBACK_ADDRESSES,
                        asArray(dto.getCallbackAddresses(), context)));
        Optional.ofNullable(dto.getPrivateProperties())
                .ifPresent(s -> builder.add(TransferRequestDto.EDC_TRANSFER_REQUEST_DTO_PRIVATE_PROPERTIES,
                        context.transform(dto.getPrivateProperties(), JsonObject.class)));
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
