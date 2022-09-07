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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import java.net.URI;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Rewriting endpoint address returned by Registry service, to the one working with AASWrapper
 */
@Slf4j
class AASWrapperUriAddressRewritePolicy {

    private final String aasWrapperHost;
    private final UrlValidator urlValidator;

    /* package */ AASWrapperUriAddressRewritePolicy(final String aasWrapperHost) {
//    /* package */ AASWrapperUriAddressRewritePolicy(final String aasWrapperHost, final UrlValidator urlValidator) {
        this.aasWrapperHost = aasWrapperHost;
        this.urlValidator = new UrlValidator();
    }

    /**
     * Rewritten address to AASWrapper
     * @param endpointAddress returned by registry
     * @return rewritten address
     */
    public URI rewriteToAASWrapperUri(final String endpointAddress) {
        if (!urlValidator.isValid(endpointAddress)) {
            throw new IllegalArgumentException(String.format("Invalid endpoint url '%s'", endpointAddress));
        }

        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(aasWrapperHost);

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
