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
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.eclipse.tractusx.irs.configuration.RestTemplateConfig.OAUTH_REST_TEMPLATE;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import io.github.resilience4j.retry.annotation.Retry;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.configuration.local.CxTestDataContainer;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Digital Twin Registry Rest Client
 */
interface DigitalTwinRegistryClient {

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns a specific Asset Administration Shell Descriptor
     */
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(String aasIdentifier);

    /**
     * Returns a list of Asset Administration Shell ids based on Asset identifier key-value-pairs.
     * Only the Shell ids are returned when all provided key-value pairs match.
     *
     * @param assetIds The key-value-pair of an Asset identifier
     * @return urn uuid string list
     */
    List<String> getAllAssetAdministrationShellIdsByAssetLink(List<IdentifierKeyValuePair> assetIds);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
@Profile({ "local", "stubtest" })
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    private final AssetAdministrationShellTestdataCreator testdataCreator;

    /* package */ DigitalTwinRegistryClientLocalStub(final CxTestDataContainer cxTestDataContainer) {
        this.testdataCreator = new AssetAdministrationShellTestdataCreator(cxTestDataContainer);
    }

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        if ("urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d".equals(aasIdentifier)) {
            throw new RestClientException("Dummy Exception");
        }
        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier);
    }

    @Override
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final List<IdentifierKeyValuePair> assetIds) {
        return Collections.emptyList();
    }
}

/**
 * Digital Twin Registry Rest Client Implementation
 */
@Service
@Profile({ "!local && !stubtest" })
class DigitalTwinRegistryClientImpl implements DigitalTwinRegistryClient {

    private final RestTemplate restTemplate;
    private final String digitalTwinRegistryUrl;
    private final OutboundMeterRegistryService meterRegistryService;

    /* package */ DigitalTwinRegistryClientImpl(@Qualifier(OAUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${digitalTwinRegistry.url:}") final String digitalTwinRegistryUrl,
            final OutboundMeterRegistryService meterRegistryService) {
        this.restTemplate = restTemplate;
        this.digitalTwinRegistryUrl = digitalTwinRegistryUrl;
        this.meterRegistryService = meterRegistryService;
    }

    @Override
    @Retry(name = "registry")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(digitalTwinRegistryUrl);
        uriBuilder.path("/registry/shell-descriptors/").path(aasIdentifier);

        return execute(
                () -> restTemplate.getForObject(uriBuilder.build().toUri(), AssetAdministrationShellDescriptor.class));
    }

    @Override
    @Retry(name = "registry")
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final List<IdentifierKeyValuePair> assetIds) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(digitalTwinRegistryUrl);
        uriBuilder.path("/lookup/shells").queryParam("assetIds", new JsonUtil().asString(assetIds));

        return execute(() -> restTemplate.getForObject(uriBuilder.build().toUri(), List.class));
    }

    private <T> T execute(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                meterRegistryService.incrementRegistryTimeoutCounter();
            }
            throw e;
        }
    }

}
