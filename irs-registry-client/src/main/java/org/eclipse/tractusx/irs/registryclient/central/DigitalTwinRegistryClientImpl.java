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
package org.eclipse.tractusx.irs.registryclient.central;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.annotation.Retry;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.registryclient.decentral.LookupShellsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Digital Twin Registry Rest Client Implementation
 */
public class DigitalTwinRegistryClientImpl implements DigitalTwinRegistryClient {

    private static final String PLACEHOLDER_AAS_IDENTIFIER = "aasIdentifier";
    private static final String PLACEHOLDER_ASSET_IDS = "assetIds";
    private final RestTemplate restTemplate;
    private final String descriptorEndpoint;
    private final String shellLookupEndpoint;

    public DigitalTwinRegistryClientImpl(final RestTemplate restTemplate,
            @Value("${digitalTwinRegistry.descriptorEndpoint:}") final String descriptorEndpoint,
            @Value("${digitalTwinRegistry.shellLookupEndpoint:}") final String shellLookupEndpoint) {
        this.restTemplate = restTemplate;
        this.descriptorEndpoint = descriptorEndpoint;
        this.shellLookupEndpoint = shellLookupEndpoint;

        ensureUrlContainsPlaceholders(descriptorEndpoint, "digitalTwinRegistry.descriptorEndpoint",
                PLACEHOLDER_AAS_IDENTIFIER);
    }

    private static void require(final String bpdmUrl, final String configPath, final String placeholder) {
        if (!bpdmUrl.contains(wrap(placeholder))) {
            throw new IllegalStateException(
                    "Configuration value for '" + configPath + "' must contain the URL placeholder '" + placeholder
                            + "'!");
        }
    }

    private static String wrap(final String placeholderIdType) {
        return "{" + placeholderIdType + "}";
    }

    private static String encodeWithBase64(final String aasIdentifier) {
        return Base64.getEncoder().encodeToString(aasIdentifier.getBytes(StandardCharsets.UTF_8));
    }

    private void ensureUrlContainsPlaceholders(final String bpdmUrl, final String configPath,
            final String... placeholders) {
        if (StringUtils.isNotBlank(bpdmUrl)) {
            for (final var placeholder : placeholders) {
                require(bpdmUrl, configPath, placeholder);
            }
        }
    }

    @Override
    @Retry(name = "registry")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(descriptorEndpoint);
        final Map<String, String> values = Map.of(PLACEHOLDER_AAS_IDENTIFIER, encodeWithBase64(aasIdentifier));
        return restTemplate.getForObject(uriBuilder.build(values), AssetAdministrationShellDescriptor.class);
    }

    @Override
    @Retry(name = "registry")
    public LookupShellsResponse getAllAssetAdministrationShellIdsByAssetLink(
            final List<IdentifierKeyValuePair> assetIds) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(shellLookupEndpoint);
        uriBuilder.uriVariables(Map.of(PLACEHOLDER_ASSET_IDS, StringMapper.mapToString(assetIds)));
        return restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET, null, LookupShellsResponse.class)
                           .getBody();
    }

}
