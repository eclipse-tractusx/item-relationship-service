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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.DTR_REST_TEMPLATE;

import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.annotation.Retry;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Digital Twin Registry Rest Client
 */
@Service
public class DecentralDigitalTwinRegistryClient {

    private static final String PLACEHOLDER_AAS_IDENTIFIER = "aasIdentifier";
    private static final String PLACEHOLDER_ASSET_IDS = "assetIds";
    private static final String SHELL_DESCRIPTOR_TEMPLATE = "/registry/shell-descriptors/{aasIdentifier}";
    private static final String LOOKUP_SHELLS_TEMPLATE = "/lookup/shells?assetIds={assetIds}";

    private final RestTemplate restTemplate;

    public DecentralDigitalTwinRegistryClient(@Qualifier(DTR_REST_TEMPLATE) final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "registry")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String descriptorHostName, final String aasIdentifier) {
        final String descriptorEndpoint = descriptorHostName + SHELL_DESCRIPTOR_TEMPLATE;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(descriptorEndpoint);
        final Map<String, String> values = Map.of(PLACEHOLDER_AAS_IDENTIFIER, aasIdentifier);
        return restTemplate.getForObject(uriBuilder.build(values), AssetAdministrationShellDescriptor.class);
    }

    @Retry(name = "registry")
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final String shellLookupHostName, final List<IdentifierKeyValuePair> assetIds) {
        final String shellLookupEndpoint = shellLookupHostName + LOOKUP_SHELLS_TEMPLATE;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(shellLookupEndpoint);
        final var values = Map.of(PLACEHOLDER_ASSET_IDS, new JsonUtil().asString(assetIds));
        return restTemplate.getForObject(uriBuilder.build(values), List.class);
    }
}
