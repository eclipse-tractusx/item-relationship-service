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
package org.eclipse.tractusx.irs.semanticshub;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.configuration.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Semantics Hub Rest Client
 */
interface SemanticsHubClient {

    /**
     * Return Json Schema of requsted model by urn
     *
     * @param urn of the model
     * @return Json Schema
     */
    String getModelJsonSchema(String urn);

}

/**
 * Semantics Hub Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "test"
})
class SemanticsHubClientLocalStub implements SemanticsHubClient {

    @Override
    public String getModelJsonSchema(final String urn) {
        return "{" + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\"," + "  \"type\": \"integer\"" + "}";
    }
}

/**
 * Semantics Hub Rest Client Implementation
 */
@Service
@Profile({ "!local && !test" })
class SemanticsHubClientImpl implements SemanticsHubClient {

    private static final String PLACEHOLDER_URN = "urn";

    private final RestTemplate restTemplate;
    private final String semanticsHubUrl;

    /* package */ SemanticsHubClientImpl(
            @Qualifier(RestTemplateConfig.SEMHUB_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${semanticsHub.modelJsonSchemaEndpoint:}") final String semanticsHubUrl) {
        this.restTemplate = restTemplate;
        this.semanticsHubUrl = semanticsHubUrl;

        if (StringUtils.isNotBlank(semanticsHubUrl)) {
            requirePlaceholder(semanticsHubUrl);
        }
    }

    private static void requirePlaceholder(final String url) {
        if (!url.contains(wrap(SemanticsHubClientImpl.PLACEHOLDER_URN))) {
            throw new IllegalStateException(
                    "Configuration value for 'semanticsHub.modelJsonSchemaEndpoint' must contain the URL placeholder '"
                            + SemanticsHubClientImpl.PLACEHOLDER_URN + "'!");
        }
    }

    private static String wrap(final String placeholderIdType) {
        return "{" + placeholderIdType + "}";
    }

    @Override
    public String getModelJsonSchema(final String urn) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(semanticsHubUrl);
        final Map<String, String> values = Map.of(PLACEHOLDER_URN, urn);
        return restTemplate.getForObject(uriBuilder.build(values), String.class);
    }
}
