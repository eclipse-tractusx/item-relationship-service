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
package org.eclipse.tractusx.irs.edc.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint used by the EDC ControlPlane to provide the endpoint data reference.
 */
@Slf4j
@RestController("irsEdcClientEdcCallbackController")
@RequestMapping("${irs-edc-client.callback.mapping:internal/endpoint-data-reference}")
@Hidden
@RequiredArgsConstructor
public class EdcCallbackController {

    private final EndpointDataReferenceStorage storage;

    @PostMapping
    public void receiveEdcCallback(final @RequestBody EndpointDataReference dataReference) {
        log.debug("Received EndpointDataReference: {}", StringMapper.mapToString(dataReference));
        log.debug("Received EndpointDataReference with ID {} and endpoint {}", dataReference.getId(),
                dataReference.getEndpoint());
        final String authCode = dataReference.getAuthCode();
        if (authCode != null) {
            final var contractAgreementId = extractContractAgreementId(authCode);
            storage.put(contractAgreementId, dataReference);
            log.info("Endpoint Data Reference received and cached for agreement: {}", Masker.mask(contractAgreementId));
        } else {
            log.error("ContractAgreementId could not be extracted from Endpoint Data Reference {}",
                    StringMapper.mapToString(dataReference));
        }
    }

    private String extractContractAgreementId(final String token) {
        final var chunks = token.split("\\.");
        final var decoder = Base64.getUrlDecoder();
        final var payload = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);
        final var authCode = StringMapper.mapFromString(payload, EDRAuthCode.class);
        return authCode.getCid();
    }
}
