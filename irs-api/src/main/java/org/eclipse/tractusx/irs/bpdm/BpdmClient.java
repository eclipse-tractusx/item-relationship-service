//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.bpdm;

import java.util.Collections;
import java.util.UUID;

import org.eclipse.tractusx.irs.configuration.RestTemplateConfig;
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

    /* package */ BpdmClientImpl(@Qualifier(RestTemplateConfig.OAUTH_REST_TEMPLATE) final RestTemplate restTemplate,
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
