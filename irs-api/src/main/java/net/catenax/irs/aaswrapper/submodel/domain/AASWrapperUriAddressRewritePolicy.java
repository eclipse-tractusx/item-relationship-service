//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.net.URI;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Rewriting endpoint address returned by Registry service, to the one working with AASWrapper
 */
@Slf4j
class AASWrapperUriAddressRewritePolicy {

    /**
     * Rewritten address to AASWrapper
     * @param endpointAddress returned by registry
     * @return rewritten address
     */
    public URI rewriteToAASWrapperUri(final String endpointAddress) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(AASWrapperUri.AAS_WRAPPER_HOST);

        final AASWrapperUri aasWrapperUri = new AASWrapperUri(endpointAddress);

        uriComponentsBuilder.path(aasWrapperUri.getPath());
        uriComponentsBuilder.query(aasWrapperUri.getQuery());
        uriComponentsBuilder.queryParam("provider-connector-url", aasWrapperUri.getProviderConnectorUrl());

        final UriComponents uriComponents = uriComponentsBuilder.build(true);

        log.debug("Rewritten endpoint address from: {}, to: {}", endpointAddress, uriComponents.toUriString());

        return uriComponents.toUri();
    }

    /**
     * AASWrapper uri container
     */
    @Getter
    /* package */ static class AASWrapperUri {
        /* package */ static final String AAS_WRAPPER_HOST = "http://aaswrapper:9191/api/service";

        private final String providerConnectorUrl;
        private final String path;
        private final String query;

        /* package */ AASWrapperUri(final String endpointAddress) {
            final int indexOfUrn = findIndexOf(endpointAddress, "/urn");
            final int indexOfQuestionMarkQuery = findIndexOf(endpointAddress, "?");

            if (indexOfUrn == -1 || indexOfQuestionMarkQuery == -1) {
                throw new IllegalArgumentException("Cannot rewrite endpoint address, malformed format: " + endpointAddress);
            }

            this.providerConnectorUrl = endpointAddress.substring(0, indexOfUrn);
            this.path = endpointAddress.substring(indexOfUrn, indexOfQuestionMarkQuery);
            this.query = endpointAddress.substring(indexOfQuestionMarkQuery + 1);
        }

        private int findIndexOf(final String endpointAddress, final String str) {
            return endpointAddress.indexOf(str);
        }
    }

}
