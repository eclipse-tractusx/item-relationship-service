/********************************************************************************
 * Copyright (c) 2022 ZF Friedrichshafen AG
 * Copyright (c) 2022 ISTOS GmbH
 * Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022 BOSCH AG
 * Copyright (c) 2026 Volkswagen AG
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
import java.util.regex.Pattern;

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
public class EdcCallbackController {

    private static final int MAX_CALLBACK_VALUE_LENGTH = 255;
    private static final String INVALID_LOG_VALUE = "<invalid>";
    private static final String EDR_CALLBACK = "Endpoint Data Reference";
    private static final String NEGOTIATION_CALLBACK = "Contract Negotiation";
    private static final String MISSING = "missing";
    private static final Pattern SAFE_LOG_VALUE_PATTERN = Pattern.compile(
            "^[^\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]{1," + MAX_CALLBACK_VALUE_LENGTH + "}$");

    private final EndpointDataReferenceStorage storage;
    private final ContractNegotiationIdStorage storageNegotiationId;

    private static EndpointDataReference mapToEndpointDataReference(final String endpointDataReference)
            throws EdcClientException {
        final EndpointDataReference dataReference;

        try {
            final EndpointDataReferenceCallback endpointDataReferenceCallback = StringMapper.mapFromString(
                    endpointDataReference, EndpointDataReferenceCallback.class);
            if (endpointDataReferenceCallback == null) {
                throw invalidCallback(EDR_CALLBACK, MISSING, "callback payload");
            }
            final TransferProcessCallbackPayload payload = Optional.ofNullable(endpointDataReferenceCallback.getPayload())
                                                                   .orElseThrow(
                                                                           () -> invalidCallback(EDR_CALLBACK, MISSING,
                                                                                   "payload"));
            final DataAddress dataAddress = Optional.ofNullable(payload.dataAddress())
                                                    .orElseThrow(() -> invalidCallback(EDR_CALLBACK, MISSING,
                                                            "dataAddress"));
            final Properties properties = Optional.ofNullable(dataAddress.properties())
                                                  .orElseThrow(() -> invalidCallback(EDR_CALLBACK, MISSING,
                                                          "dataAddress.properties"));
            final String contractId = requireValidCallbackIdentifier(
                    Optional.ofNullable(payload.contractId())
                            .filter(StringUtils::isNotBlank)
                            .or(() -> Optional.ofNullable(properties.agreementId()).filter(StringUtils::isNotBlank))
                            .orElseThrow(() -> invalidCallback(EDR_CALLBACK, MISSING,
                                    "payload.contractId and dataAddress.properties.agreement_id")),
                    "payload.contractId or dataAddress.properties.agreement_id", EDR_CALLBACK);

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

    private static NegotiationCallbackPayload mapToContractAgreementId(final String endpointNegotiationMapping)
            throws EdcClientException {
        try {
            final NegotiationEndpointCallback negotiationEndpointCallback = Optional.ofNullable(
                                                                                         StringMapper.mapFromString(
                                                                                                 endpointNegotiationMapping,
                                                                                                 NegotiationEndpointCallback.class))
                                                                                 .orElseThrow(
                                                                                         () -> invalidCallback(
                                                                                                 NEGOTIATION_CALLBACK,
                                                                                                 MISSING,
                                                                                                 "callback payload"));
            final NegotiationCallbackPayload payload = Optional.ofNullable(negotiationEndpointCallback.getPayload())
                                                               .orElseThrow(
                                                                       () -> invalidCallback(NEGOTIATION_CALLBACK,
                                                                               MISSING, "payload"));
            requireValidCallbackIdentifier(payload.getContractNegotiationId(), "payload.contractNegotiationId",
                    NEGOTIATION_CALLBACK);
            final ContractAgreement contractAgreement = Optional.ofNullable(payload.getContractAgreement())
                                                                .orElseThrow(() -> invalidCallback(
                                                                        NEGOTIATION_CALLBACK, MISSING,
                                                                        "payload.contractAgreement"));
            requireValidCallbackIdentifier(contractAgreement.getContractAgreementId(), "payload.contractAgreement.id",
                    NEGOTIATION_CALLBACK);

            return payload;
        } catch (JsonParseException e) {
            throw new EdcClientException("Invalid Contract Negotiation callback payload", e);
        }
    }

    private static EdcClientException invalidCallback(final String callbackType, final String problem,
            final String field) {
        return new EdcClientException("Invalid " + callbackType + " callback: " + problem + " " + field);
    }

    private static String requireValidCallbackIdentifier(final String value, final String field,
            final String callbackType) throws EdcClientException {
        if (StringUtils.isBlank(value)) {
            throw invalidCallback(callbackType, MISSING, field);
        }
        if (!SAFE_LOG_VALUE_PATTERN.matcher(value).matches()) {
            throw invalidCallback(callbackType, "invalid", field);
        }
        return value;
    }

    private static String safeLogValue(final String value) {
        return value != null && SAFE_LOG_VALUE_PATTERN.matcher(value).matches() ? value : INVALID_LOG_VALUE;
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

            log.debug("Received EndpointDataReference with ID {} and endpoint {}",
                    safeLogValue(endpointDataReference.getId()), safeLogValue(endpointDataReference.getEndpoint()));

            final String contractAgreementId = endpointDataReference.getContractId();
            storeEdr(contractAgreementId, endpointDataReference);
        } catch (EdcClientException e) {
            logCallbackError(EDR_CALLBACK, e);
        }
    }

    @PostMapping("${irs-edc-client.callback.negotiation-mapping}")
    public void receiveNegotiationsCallback(final @RequestBody String endpointNegotiationCallback) {
        try {
            final NegotiationCallbackPayload payload = mapToContractAgreementId(endpointNegotiationCallback);
            final String contractAgreementId = payload.getContractAgreement().getContractAgreementId();
            log.debug("Received Negotiation Callback for negotiationId: '{}' and contractAgreementId: '{}'",
                    safeLogValue(payload.getContractNegotiationId()), safeLogValue(contractAgreementId));
            storeNegotiationId(payload.getContractNegotiationId(), contractAgreementId);
        } catch (EdcClientException e) {
            logCallbackError(NEGOTIATION_CALLBACK, e);
        }

    }

    private void storeEdr(final String contractId, final EndpointDataReference dataReference) {
        storage.put(contractId, dataReference);
        log.info("Endpoint Data Reference received and cached for agreement: {}",
                safeLogValue(Masker.mask(contractId)));
    }

    private void storeNegotiationId(final String contractNegotiationId, final String contractAgreementId) {
        storageNegotiationId.put(contractNegotiationId, contractAgreementId);
        log.info("Mapped Contract NegotiationId {} to Contract AgreementId and cached for agreement: {}",
                safeLogValue(contractNegotiationId), safeLogValue(Masker.mask(contractAgreementId)));
    }
}
