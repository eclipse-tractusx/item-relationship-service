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
package org.eclipse.tractusx.irs.bpdm;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.BPDM_REST_TEMPLATE;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
     *
     * @param idValue identifier value
     * @param idType  type of identifier
     * @return
     */
    BusinessPartnerResponse getBusinessPartner(String idValue, String idType);

}

/**
 * Business Partner Data Management Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "test"
})
class BpdmClientLocalStub implements BpdmClient {

    @Override
    public BusinessPartnerResponse getBusinessPartner(final String idValue, final String idType) {
        return BusinessPartnerResponse.builder()
                                      .bpn(idValue)
                                      .names(Collections.singletonList(NameResponse.of("OEM A")))
                                      .build();
    }
}

/**
 * Business Partner Data Management Rest Client Implementation
 */
@Service
@Profile({ "!local && !test" })
class BpdmClientImpl implements BpdmClient {

    private static final String PLACEHOLDER_BPID = "partnerId";
    private static final String PLACEHOLDER_ID_TYPE = "idType";

    private final RestTemplate restTemplate;
    private final String bpdmUrl;

    /* package */ BpdmClientImpl(@Qualifier(BPDM_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${bpdm.bpnEndpoint:}") final String bpdmUrl) {
        this.restTemplate = restTemplate;
        this.bpdmUrl = bpdmUrl;

        if (StringUtils.isNotBlank(bpdmUrl)) {
            ensureUrlContainsPlaceholders(bpdmUrl);
        }
    }

    private void ensureUrlContainsPlaceholders(final String bpdmUrl) {
        require(bpdmUrl, PLACEHOLDER_ID_TYPE);
        require(bpdmUrl, PLACEHOLDER_BPID);
    }

    private static void require(final String bpdmUrl, final String placeholder) {
        if (!bpdmUrl.contains(wrap(placeholder))) {
            throw new IllegalStateException(
                    "Configuration value for 'bpdm.bpnEndpoint' must contain the URL placeholder '" + placeholder
                            + "'!");
        }
    }

    private static String wrap(final String placeholderIdType) {
        return "{" + placeholderIdType + "}";
    }

    @Override
    public BusinessPartnerResponse getBusinessPartner(final String idValue, final String idType) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(bpdmUrl);
        final Map<String, String> values = Map.of(PLACEHOLDER_BPID, idValue, PLACEHOLDER_ID_TYPE, idType);

        return restTemplate.getForObject(uriBuilder.build(values), BusinessPartnerResponse.class);
    }
}
