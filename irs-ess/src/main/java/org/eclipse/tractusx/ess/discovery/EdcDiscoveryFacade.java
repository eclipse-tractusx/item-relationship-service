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
package org.eclipse.tractusx.ess.discovery;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for Discovery service domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EdcDiscoveryFacade {

    private final EdcDiscoveryClient edcDiscoveryClient;

    /**
     * Returns EDC base url or empty
     *
     * @param bpn number
     * @return address
     */
    public Optional<String> getEdcBaseUrl(final String bpn) {
        log.info("Requesting EDC URLs for BPN '{}'", bpn);
        final EdcAddressResponse[] edcBaseUrl = edcDiscoveryClient.getEdcBaseUrl(bpn);
        final List<EdcAddressResponse> edcAddressResponse;
        if (edcBaseUrl == null) {
            return Optional.empty();
        } else {
            edcAddressResponse = List.of(edcBaseUrl);
        }

        final List<String> endpoints = edcAddressResponse.stream()
                                                         .flatMap(response -> response.getConnectorEndpoint().stream())
                                                         .toList();

        return endpoints.stream()
                        .filter(StringUtils::isNotBlank)
                        .findFirst();
    }

}
