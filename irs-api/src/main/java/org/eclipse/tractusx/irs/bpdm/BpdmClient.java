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
package org.eclipse.tractusx.irs.bpdm;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.OAUTH_REST_TEMPLATE;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Business Partner Data Management Rest Client
 */
interface BpdmClient {

    /**
     * Find a business partner by the specified identifier.
     * @param idValue identifier value
     * @param idType type of identifier
     * @return
     */
    BusinessPartnerResponse getBusinessPartner(String idValue, String idType);

}

/**
 * Business Partner Data Management Rest Client Stub used in local environment
 */
@Service
@Profile({ "local", "test" })
class BpdmClientLocalStub implements BpdmClient {

    @Override
    public BusinessPartnerResponse getBusinessPartner(final String idValue, final String idType) {
        return BusinessPartnerResponse.builder()
                                      .bpn(idValue)
                                      .names(Collections.singletonList(NameResponse.of(UUID.randomUUID(), "OEM A")))
                                      .build();
    }
}

/**
 * Business Partner Data Management Rest Client Implementation
 */
@Service
@Profile({ "!local && !test" })
class BpdmClientImpl implements BpdmClient {

    private final RestTemplate restTemplate;
    private final String bpdmUrl;

    /* package */ BpdmClientImpl(@Qualifier(OAUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${bpdm.url:}") final String bpdmUrl) {
        this.restTemplate = restTemplate;
        this.bpdmUrl = bpdmUrl;
    }

    @Override
    public BusinessPartnerResponse getBusinessPartner(final String idValue, final String idType) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(bpdmUrl);
        uriBuilder.path("/api/catena/business-partner/").path(idValue).queryParam("idType", idType);

        return restTemplate.getForObject(uriBuilder.build().toUri(), BusinessPartnerResponse.class);
    }
}
