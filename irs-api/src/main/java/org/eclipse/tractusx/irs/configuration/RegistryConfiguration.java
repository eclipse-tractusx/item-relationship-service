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
package org.eclipse.tractusx.irs.configuration;

import java.util.HashMap;

import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.central.CentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClientImpl;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.decentral.EdcRetrieverException;
import org.eclipse.tractusx.irs.registryclient.decentral.EndpointDataForConnectorsService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.eclipse.tractusx.irs.registryclient.discovery.LocalDataDiscovery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * IRS configuration settings. Sets up the digital twin registry client.
 */
@Configuration
public class RegistryConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "digitalTwinRegistry", name = "type", havingValue = "central")
    public CentralDigitalTwinRegistryService centralDigitalTwinRegistryService(final DigitalTwinRegistryClient client) {
        return new CentralDigitalTwinRegistryService(client);
    }

    @Bean
    @Profile({ "!local && !stubtest" })
    @ConditionalOnProperty(prefix = "digitalTwinRegistry", name = "type", havingValue = "central")
    public DigitalTwinRegistryClient digitalTwinRegistryClientImpl(
            @Qualifier(RestTemplateConfig.DTR_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${digitalTwinRegistry.descriptorEndpoint:}") final String descriptorEndpoint,
            @Value("${digitalTwinRegistry.shellLookupEndpoint:}") final String shellLookupEndpoint) {
        return new DigitalTwinRegistryClientImpl(restTemplate, descriptorEndpoint, shellLookupEndpoint);
    }

    @Bean
    @ConditionalOnProperty(prefix = "digitalTwinRegistry", name = "type", havingValue = "decentral")
    public DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService(
            @Qualifier(RestTemplateConfig.EDC_REST_TEMPLATE) final RestTemplate edcRestTemplate,
            final ConnectorEndpointsService connectorEndpointsService, final EdcSubmodelFacade facade) {
        return new DecentralDigitalTwinRegistryService(connectorEndpointsService,
                new EndpointDataForConnectorsService((edcConnectorEndpoint, assetType, assetValue) -> {
                    try {
                        return facade.getEndpointReferenceForAsset(edcConnectorEndpoint, assetType, assetValue);
                    } catch (EdcClientException e) {
                        throw new EdcRetrieverException(e);
                    }
                }), new DecentralDigitalTwinRegistryClient(edcRestTemplate));
    }

    @Bean
    @Profile({ "!local && !stubtest" })
    public ConnectorEndpointsService connectorEndpointsService(
            @Qualifier(RestTemplateConfig.DTR_REST_TEMPLATE) final RestTemplate dtrRestTemplate,
            @Value("${digitalTwinRegistry.discoveryFinderUrl:}") final String finderUrl) {
        return new ConnectorEndpointsService(new DiscoveryFinderClientImpl(finderUrl, dtrRestTemplate));
    }

    @Bean
    @Profile({ "local",
               "stubtest"
    })
    public LocalDataDiscovery discoveryFinderClient() {
        return new LocalDataDiscovery(new HashMap<>());
    }

    @Bean
    @Profile({ "local",
               "stubtest"
    })
    public ConnectorEndpointsService localDiscoveryConnector(final LocalDataDiscovery discoveryFinderClient) {
        return new ConnectorEndpointsService(discoveryFinderClient);
    }

}
