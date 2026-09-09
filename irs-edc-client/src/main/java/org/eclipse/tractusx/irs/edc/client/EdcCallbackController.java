/********************************************************************************
 * Copyright (c) 2022,2024
 *       2026: Volkswagen AG
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

import java.util.Optional;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.edr.ContractAgreement;
import org.eclipse.tractusx.irs.edc.client.model.edr.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.edr.EndpointDataReferenceCallback;
import org.eclipse.tractusx.irs.edc.client.model.edr.NegotiationCallbackPayload;
import org.eclipse.tractusx.irs.edc.client.model.edr.NegotiationEndpointCallback;
import org.eclipse.tractusx.irs.edc.client.model.edr.Properties;
import org.eclipse.tractusx.irs.edc.client.model.edr.TransferProcessCallbackPayload;
import org.eclipse.tractusx.irs.edc.client.storage.ContractNegotiationIdStorage;
import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint used by the EDC ControlPlane to provide the endpoint data reference.
 */
@Slf4j
@RestController("irsEdcClientEdcCallbackController")
@Hidden
@RequiredArgsConstructor
@SuppressWarnings("PMD.CommentSize")
public class EdcCallbackController {

    private final EndpointDataReferenceStorage storage;
    private final ContractNegotiationIdStorage storageNegotiationId;

    private static EndpointDataReference mapToEndpointDataReference(final String endpointDataReference)
            throws EdcClientException {
        final EndpointDataReference dataReference;

        try {
            final EndpointDataReferenceCallback endpointDataReferenceCallback = StringMapper.mapFromString(
                    endpointDataReference, EndpointDataReferenceCallback.class);
            if (endpointDataReferenceCallback == null) {
                throw invalidEdrCallback("callback payload");
            }
            final TransferProcessCallbackPayload payload = Optional.ofNullable(endpointDataReferenceCallback.getPayload())
                                                                   .orElseThrow(() -> invalidEdrCallback("payload"));
            final DataAddress dataAddress = Optional.ofNullable(payload.dataAddress())
                                                    .orElseThrow(() -> invalidEdrCallback("dataAddress"));
            final Properties properties = Optional.ofNullable(dataAddress.properties())
                                                  .orElseThrow(() -> invalidEdrCallback("dataAddress.properties"));
            final String contractId = Optional.ofNullable(payload.contractId())
                                              .filter(StringUtils::isNotBlank)
                                              .or(() -> Optional.ofNullable(properties.agreementId())
                                                                .filter(StringUtils::isNotBlank))
                                              .orElseThrow(() -> invalidEdrCallback(
                                                      "payload.contractId and dataAddress.properties.agreement_id"));

            dataReference = EndpointDataReference.Builder.newInstance()
                                                         .contractId(contractId)
                                                         .id(properties.processId())
                                                         .authKey(HttpHeaders.AUTHORIZATION)
                                                         .authCode(properties.authorization())
                                                         .endpoint(properties.endpoint())
                                                         .build();
            return dataReference;
        } catch (JsonParseException e) {
            throw new EdcClientException("Invalid Endpoint Data Reference callback payload", e);
        }
    }

    private static EdcClientException invalidEdrCallback(final String missingField) {
        return new EdcClientException("Invalid Endpoint Data Reference callback: missing " + missingField);
    }

    private static NegotiationCallbackPayload mapToContractAgreementId(final String endpointNegotiationMapping)
            throws EdcClientException {
        try {
            final NegotiationEndpointCallback negotiationEndpointCallback = Optional.ofNullable(
                                                                                         StringMapper.mapFromString(
                                                                                                 endpointNegotiationMapping,
                                                                                                 NegotiationEndpointCallback.class))
                                                                                 .orElseThrow(
                                                                                         () -> invalidNegotiationCallback(
                                                                                                 "callback payload"));
            final NegotiationCallbackPayload payload = Optional.ofNullable(negotiationEndpointCallback.getPayload())
                                                               .orElseThrow(
                                                                       () -> invalidNegotiationCallback("payload"));
            Optional.ofNullable(payload.getContractNegotiationId())
                    .filter(StringUtils::isNotBlank)
                    .orElseThrow(() -> invalidNegotiationCallback("payload.contractNegotiationId"));
            final ContractAgreement contractAgreement = Optional.ofNullable(payload.getContractAgreement())
                                                                .orElseThrow(() -> invalidNegotiationCallback(
                                                                        "payload.contractAgreement"));
            Optional.ofNullable(contractAgreement.getContractAgreementId())
                    .filter(StringUtils::isNotBlank)
                    .orElseThrow(() -> invalidNegotiationCallback("payload.contractAgreement.id"));

            return payload;
        } catch (JsonParseException e) {
            throw new EdcClientException("Invalid Contract Negotiation callback payload", e);
        }
    }

    private static EdcClientException invalidNegotiationCallback(final String missingField) {
        return new EdcClientException("Invalid Contract Negotiation callback: missing " + missingField);
    }

    private static void logCallbackError(final String callbackType, final EdcClientException exception) {
        final Throwable cause = exception.getCause();
        final String causeType = cause == null ? "<none>" : cause.getClass().getName();
        log.error("Could not process {} callback: {} causeType={}", callbackType, exception.getMessage(), causeType);
    }

    @PostMapping("${irs-edc-client.callback.mapping}")
    public void receiveEdcCallback(final @RequestBody String endpointDataReferenceCallback) {
        final EndpointDataReference endpointDataReference;

        try {
            endpointDataReference = mapToEndpointDataReference(endpointDataReferenceCallback);

            log.debug("Received EndpointDataReference with ID {} and endpoint {}", endpointDataReference.getId(),
                    endpointDataReference.getEndpoint());

            final String contractAgreementId = endpointDataReference.getContractId();
            storeEdr(contractAgreementId, endpointDataReference);
        } catch (EdcClientException e) {
            logCallbackError("Endpoint Data Reference", e);
        }
    }

    @PostMapping("${irs-edc-client.callback.negotiation-mapping}")
    public void receiveNegotiationsCallback(final @RequestBody String endpointNegotiationCallback) {
        try {
            final NegotiationCallbackPayload payload = mapToContractAgreementId(endpointNegotiationCallback);
            final String contractAgreementId = payload.getContractAgreement().getContractAgreementId();
            log.debug("Received Negotiation Callback for negotiationId: '{}' and contractAgreementId: '{}'",
                    payload.getContractNegotiationId(), contractAgreementId);
            storeNegotiationId(payload.getContractNegotiationId(), contractAgreementId);
        } catch (EdcClientException e) {
            logCallbackError("Contract Negotiation", e);
        }

    }

    private void storeEdr(final String contractId, final EndpointDataReference dataReference) {
        storage.put(contractId, dataReference);
        log.info("Endpoint Data Reference received and cached for agreement: {}", Masker.mask(contractId));
    }

    private void storeNegotiationId(final String contractNegotiationId, final String contractAgreementId) {
        storageNegotiationId.put(contractNegotiationId, contractAgreementId);
        log.info("Mapped Contract NegotiationId {} to Contract AgreementId and cached for agreement: {}",
                contractNegotiationId, Masker.mask(contractAgreementId));
    }
}
