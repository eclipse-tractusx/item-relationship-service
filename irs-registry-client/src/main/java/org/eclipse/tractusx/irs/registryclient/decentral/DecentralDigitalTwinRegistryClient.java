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
package org.eclipse.tractusx.irs.registryclient.decentral;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.annotation.Retry;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.registryclient.util.SerializationHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Digital Twin Registry Rest Client
 */
public class DecentralDigitalTwinRegistryClient {

    private static final String PLACEHOLDER_AAS_IDENTIFIER = "aasIdentifier";
    private static final String PLACEHOLDER_ASSET_IDS = "assetIds";
    private static final String PLACEHOLDER_CURSOR = "cursor";
    private static final String PLACEHOLDER_LIMIT = "limit";

    private final RestTemplate edcRestTemplate;
    private final String shellDescriptorTemplate;
    private final String lookupShellsTemplate;

    private final SerializationHelper serializationHelper = new SerializationHelper();

    public DecentralDigitalTwinRegistryClient(final RestTemplate edcRestTemplate,
            @Value("${digitalTwinRegistry.shellDescriptorTemplate:}") final String shellDescriptorTemplate,
            @Value("${digitalTwinRegistry.lookupShellsTemplate:}") final String lookupShellsTemplate) {
        this.edcRestTemplate = edcRestTemplate;
        this.shellDescriptorTemplate = shellDescriptorTemplate;
        this.lookupShellsTemplate = lookupShellsTemplate;
    }

    @Retry(name = "registry")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(
            final EndpointDataReference endpointDataReference, final String aasIdentifier) {
        final String descriptorEndpoint = endpointDataReference.getEndpoint() + shellDescriptorTemplate;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(descriptorEndpoint);
        final Map<String, String> values = Map.of(PLACEHOLDER_AAS_IDENTIFIER, encodeWithBase64(aasIdentifier));
        return edcRestTemplate.exchange(uriBuilder.build(values), HttpMethod.GET,
                                      new HttpEntity<>(null, headers(endpointDataReference)), AssetAdministrationShellDescriptor.class)
                              .getBody();
    }

    @Retry(name = "registry")
    public LookupShellsResponse getAllAssetAdministrationShellIdsByAssetLink(
            final EndpointDataReference endpointDataReference, final IdentifierKeyValuePair assetIds) {
        final String shellLookupEndpoint = endpointDataReference.getEndpoint() + lookupShellsTemplate;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(shellLookupEndpoint);
        uriBuilder.uriVariables(Map.of(PLACEHOLDER_ASSET_IDS, encodeWithBase64(assetIds)));
        return edcRestTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET,
                new HttpEntity<>(null, headers(endpointDataReference)), LookupShellsResponse.class).getBody();
    }

    @Retry(name = "registry")
    public LookupShellsResponse getAllAssetAdministrationShellIdsByFilter(
            final EndpointDataReference endpointDataReference, final LookupShellsFilter lookupShellsFilter) {
        final String lookupShellsFilterTemplate = "/lookup/shells";
        final String shellLookupEndpoint = endpointDataReference.getEndpoint() + lookupShellsFilterTemplate;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(shellLookupEndpoint);

        // Add additional filters if they exist
        if (lookupShellsFilter != null) {
            if (lookupShellsFilter.getCursor() != null) {
                uriBuilder.queryParam(PLACEHOLDER_CURSOR, encodeWithBase64(lookupShellsFilter.getCursor()));
            }
            if (lookupShellsFilter.getLimit() != null) {
                uriBuilder.queryParam(PLACEHOLDER_LIMIT, lookupShellsFilter.getLimit().toString());
            }
            if (lookupShellsFilter.getIdentifierKeyValuePairs() != null) {
                lookupShellsFilter.getIdentifierKeyValuePairs().forEach(identifier -> {
                    final String jsonObject = String.format("{\"name\":\"%s\",\"value\":\"%s\"}", identifier.getName(),
                            identifier.getValue());

                    final String encodedValue = encodeWithBase64(jsonObject);
                    uriBuilder.queryParam(PLACEHOLDER_ASSET_IDS, encodedValue);
                });
            }
        }

        return edcRestTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET,
                new HttpEntity<>(null, headers(endpointDataReference)), LookupShellsResponse.class).getBody();
    }

    private String encodeWithBase64(final String aasIdentifier) {
        return Base64.getEncoder().encodeToString(aasIdentifier.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeWithBase64(final IdentifierKeyValuePair assetIds) {
        return Base64.getEncoder().encodeToString(serializationHelper.serialize(assetIds));
    }

    private HttpHeaders headers(final EndpointDataReference dataReference) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        final var authKey = dataReference.getAuthKey();
        if (authKey != null) {
            headers.add(authKey, dataReference.getAuthCode());
        }
        return headers;
    }
}
