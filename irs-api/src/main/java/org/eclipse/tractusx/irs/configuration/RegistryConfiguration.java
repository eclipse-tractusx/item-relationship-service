/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.configuration;

import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.PreferredConnectorEndpointsCache;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.central.CentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClientImpl;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.decentral.EdcEndpointReferenceRetriever;
import org.eclipse.tractusx.irs.registryclient.decentral.EdcRetrieverException;
import org.eclipse.tractusx.irs.registryclient.decentral.EndpointDataForConnectorsService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
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
            final ConnectorEndpointsService connectorEndpointsService, final EdcSubmodelFacade facade,
            @Value("${digitalTwinRegistry.shellDescriptorTemplate:}") final String shellDescriptorTemplate,
            @Value("${digitalTwinRegistry.lookupShellsTemplate:}") final String lookupShellsTemplate,
            final EdcConfiguration edcConfiguration,
            final PreferredConnectorEndpointsCache preferredConnectorEndpointsCache) {

        final EdcEndpointReferenceRetriever endpointReferenceRetriever = (edcConnectorEndpoint, bpn) -> {
            try {
                return facade.getEndpointReferencesForRegistryAsset(edcConnectorEndpoint, bpn);
            } catch (EdcClientException e) {
                throw new EdcRetrieverException.Builder(e).withEdcUrl(edcConnectorEndpoint).withBpn(bpn).build();
            }
        };

        final DecentralDigitalTwinRegistryClient digitalTwinRegistryClient = new DecentralDigitalTwinRegistryClient(
                edcRestTemplate, shellDescriptorTemplate, lookupShellsTemplate);

        final EndpointDataForConnectorsService endpointDataForConnectorsService = new EndpointDataForConnectorsService(
                endpointReferenceRetriever, preferredConnectorEndpointsCache);

        return new DecentralDigitalTwinRegistryService(connectorEndpointsService, endpointDataForConnectorsService,
                digitalTwinRegistryClient, edcConfiguration, preferredConnectorEndpointsCache);
    }

    @Bean
    public ConnectorEndpointsService connectorEndpointsService(
            @Qualifier(RestTemplateConfig.DTR_REST_TEMPLATE) final RestTemplate dtrRestTemplate,
            @Value("${digitalTwinRegistry.discovery.discoveryFinderUrl:}") final String finderUrl,
            @Value("${digitalTwinRegistry.discovery.type:}") final String discoveryType) {
        return new ConnectorEndpointsService(discoveryFinderClient(dtrRestTemplate, finderUrl), discoveryType);
    }


    @Bean
    public DiscoveryFinderClient discoveryFinderClient(
            @Qualifier(RestTemplateConfig.DTR_REST_TEMPLATE) final RestTemplate dtrRestTemplate,
            @Value("${digitalTwinRegistry.discovery.discoveryFinderUrl:}") final String finderUrl) {
        return new DiscoveryFinderClientImpl(finderUrl, dtrRestTemplate);
    }

}
