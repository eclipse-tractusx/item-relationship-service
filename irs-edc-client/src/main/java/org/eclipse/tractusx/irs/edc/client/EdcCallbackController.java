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
package org.eclipse.tractusx.irs.edc.client;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.edr.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.edr.EndpointDataReferenceCallback;
import org.eclipse.tractusx.irs.edc.client.model.edr.Properties;
import org.eclipse.tractusx.irs.edc.client.model.edr.TransferProcessCallbackPayload;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint used by the EDC ControlPlane to provide the endpoint data reference.
 */
@Slf4j
@RestController("irsEdcClientEdcCallbackController")
@RequestMapping("${irs-edc-client.callback.mapping}")
@Hidden
@RequiredArgsConstructor
public class EdcCallbackController {

    private final EndpointDataReferenceStorage storage;

    @PostMapping
    public void receiveEdcCallback(final @RequestBody String endpointDataReferenceCallback) {
        final EndpointDataReference endpointDataReference;

        try {
            endpointDataReference = mapToEndpointDataReference(endpointDataReferenceCallback);

            log.debug("Received EndpointDataReference: {}", StringMapper.mapToString(endpointDataReference));
            log.debug("Received EndpointDataReference with ID {} and endpoint {}", endpointDataReference.getId(),
                    endpointDataReference.getEndpoint());

            final String contractId = endpointDataReference.getContractId();
            storeEdr(contractId, endpointDataReference);
        } catch (EdcClientException e) {
            log.error("Could not deserialize Endpoint Data Reference {}", endpointDataReferenceCallback);
        }
    }

    private static EndpointDataReference mapToEndpointDataReference(final String endpointDataReference)
            throws EdcClientException {
        final EndpointDataReference dataReference;

        try {
            final EndpointDataReferenceCallback endpointDataReferenceCallback = StringMapper.mapFromString(
                    endpointDataReference, EndpointDataReferenceCallback.class);
            final TransferProcessCallbackPayload payload = Optional.ofNullable(
                    endpointDataReferenceCallback.getPayload()).orElseThrow();
            final DataAddress dataAddress = Optional.ofNullable(payload.dataAddress()).orElseThrow();
            final Properties properties = Optional.ofNullable(dataAddress.properties()).orElseThrow();

            dataReference = EndpointDataReference.Builder.newInstance()
                                                         .contractId(properties.agreementId())
                                                         .id(properties.processId())
                                                         .authKey(HttpHeaders.AUTHORIZATION)
                                                         .authCode(properties.authorization())
                                                         .endpoint(properties.endpoint())
                                                         .build();
            return dataReference;
        } catch (JsonParseException | NoSuchElementException e) {
            throw new EdcClientException(e);
        }
    }

    private void storeEdr(final String contractId, final EndpointDataReference dataReference) {
        storage.put(contractId, dataReference);
        log.info("Endpoint Data Reference received and cached for agreement: {}", Masker.mask(contractId));
    }
}
