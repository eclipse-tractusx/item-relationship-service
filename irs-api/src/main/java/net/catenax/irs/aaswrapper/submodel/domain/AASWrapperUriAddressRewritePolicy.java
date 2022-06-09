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

@Slf4j
class AASWrapperUriAddressRewritePolicy {

    public URI rewriteToAASWrapperUri(final String endpointAddress) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(AASWrapperUri.AAS_WRAPPER_HOST);

        final AASWrapperUri aasWrapperUri = new AASWrapperUri(endpointAddress);

        uriComponentsBuilder.path(aasWrapperUri.getPath());
        uriComponentsBuilder.query(aasWrapperUri.getQuery());
        uriComponentsBuilder.queryParam("provider-connector-url", aasWrapperUri.getProviderConnectUrl());

        final UriComponents uriComponents = uriComponentsBuilder.build();

        log.debug("Rewritten endpoint address from: {}, to: {}", endpointAddress, uriComponents.toUriString());

        return uriComponents.toUri();
    }

    @Getter
    static class AASWrapperUri {
        static final String AAS_WRAPPER_HOST = "http://aaswrapper:9191/api/service";

        final String providerConnectUrl;
        final String path;
        final String query;

        AASWrapperUri(final String endpointAddress) {
            final int indexOfUrn = findIndexOf(endpointAddress, "/urn:uuid:");
            final int indexOfQuestionMarkQuery = findIndexOf(endpointAddress, "?");

            if (indexOfUrn != -1 && indexOfQuestionMarkQuery != -1) {
                this.providerConnectUrl = endpointAddress.substring(0, indexOfUrn);
                this.path = endpointAddress.substring(indexOfUrn, indexOfQuestionMarkQuery);
                this.query = endpointAddress.substring(indexOfQuestionMarkQuery + 1);
            } else {
                throw new RuntimeException("Cannot rewrite endpoint address, malformed format: " + endpointAddress);
            }
        }

        private int findIndexOf(final String endpointAddress, final String str) {
            return endpointAddress.indexOf(str);
        }
    }

}
