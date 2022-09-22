/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.esr.supplyon;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OnSupplyAPI Rest Client
 */
interface SupplyOnClient {

    /**
     *
     * @param requestorBPN BPNL number of the requestor
     * @param supplierBPN BPNL number of the requestors - supplier.
     * @param certificateType Requested certificate type
     * @return ESR Certificate payload
     */
    EsrCertificate getESRCertificate(String requestorBPN, String supplierBPN, String certificateType);

}

/**
 * OnSupplyAPI Rest Client Implementation
 */
@Service
class SupplyOnClientClientImpl implements SupplyOnClient {

    private final RestTemplate restTemplate;
    private final String supplyOnUrl;
    private final String subscriptionKey;

    /* package */ SupplyOnClientClientImpl(final RestTemplate defaultRestTemplate,
            @Value("${supplyOn.url:}") final String supplyOnUrl,
            @Value("${supplyOn.subscriptionKey:}") final String subscriptionKey) {
        this.restTemplate = defaultRestTemplate;
        this.supplyOnUrl = supplyOnUrl;
        this.subscriptionKey = subscriptionKey;
    }

    @Override
    public EsrCertificate getESRCertificate(final String requestorBPN, final String supplierBPN,
            final String certificateType) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(supplyOnUrl);
        uriBuilder.pathSegment(requestorBPN).pathSegment(supplierBPN).path("/submodel/").path(certificateType.toLowerCase(Locale.ROOT));

        return restTemplate.exchange(
                uriBuilder.build().toUri(),
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders()),
                EsrCertificate.class).getBody();
    }

    private HttpHeaders buildHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);

        return headers;
    }

}
