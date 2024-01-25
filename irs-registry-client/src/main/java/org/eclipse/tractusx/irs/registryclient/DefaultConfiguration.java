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
package org.eclipse.tractusx.irs.registryclient;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.edc.client.AsyncPollingService;
import org.eclipse.tractusx.irs.edc.client.ContractNegotiationService;
import org.eclipse.tractusx.irs.edc.client.EDCCatalogFacade;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClient;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClientImpl;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.registryclient.central.CentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClientImpl;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.decentral.DecentralDigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.decentral.EdcRetrieverException;
import org.eclipse.tractusx.irs.registryclient.decentral.EndpointDataForConnectorsService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClient;
import org.eclipse.tractusx.irs.registryclient.discovery.DiscoveryFinderClientImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * IRS configuration settings. Sets up the digital twin registry client.
 */
@Configuration
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.TooManyMethods"
})
public class DefaultConfiguration {

    public static final String DIGITAL_TWIN_REGISTRY_REST_TEMPLATE = "digitalTwinRegistryRestTemplate";
    public static final String EDC_REST_TEMPLATE = "edcRestTemplate";
    private static final String CONFIG_PREFIX = "digitalTwinRegistryClient";
    private static final String CONFIG_FIELD_TYPE = "type";
    private static final String CONFIG_VALUE_DECENTRAL = "decentral";
    private static final String CONFIG_VALUE_CENTRAL = "central";
    private static final int POOL_SIZE = 20;

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_CENTRAL)
    public CentralDigitalTwinRegistryService centralDigitalTwinRegistryService(final DigitalTwinRegistryClient client) {
        return new CentralDigitalTwinRegistryService(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_CENTRAL)
    public DigitalTwinRegistryClient digitalTwinRegistryClientImpl(
            @Qualifier(DIGITAL_TWIN_REGISTRY_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${digitalTwinRegistryClient.descriptorEndpoint:}") final String descriptorEndpoint,
            @Value("${digitalTwinRegistryClient.shellLookupEndpoint:}") final String shellLookupEndpoint) {
        return new DigitalTwinRegistryClientImpl(restTemplate, descriptorEndpoint, shellLookupEndpoint);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public DecentralDigitalTwinRegistryService decentralDigitalTwinRegistryService(
            final ConnectorEndpointsService connectorEndpointsService,
            final EndpointDataForConnectorsService endpointDataForConnectorsService,
            final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient) {
        return new DecentralDigitalTwinRegistryService(connectorEndpointsService, endpointDataForConnectorsService,
                decentralDigitalTwinRegistryClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public DiscoveryFinderClient discoveryFinderClient(
            @Qualifier(DIGITAL_TWIN_REGISTRY_REST_TEMPLATE) final RestTemplate dtrRestTemplate,
            @Value("${digitalTwinRegistryClient.discoveryFinderUrl:}") final String finderUrl) {
        return new DiscoveryFinderClientImpl(finderUrl, dtrRestTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public ConnectorEndpointsService connectorEndpointsService(final DiscoveryFinderClient discoveryFinderClient) {
        return new ConnectorEndpointsService(discoveryFinderClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public EndpointDataForConnectorsService endpointDataForConnectorsService(final EdcSubmodelFacade facade) {
        return new EndpointDataForConnectorsService((edcConnectorEndpoint, assetType, assetValue) -> {
            try {
                return facade.getEndpointReferenceForAsset(edcConnectorEndpoint, assetType, assetValue);
            } catch (EdcClientException e) {
                throw new EdcRetrieverException(e);
            }
        });
    }

    @Bean
    public EdcSubmodelFacade edcSubmodelFacade(final EdcSubmodelClient client) {
        return new EdcSubmodelFacade(client);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public EdcSubmodelClient edcSubmodelClient(final EdcConfiguration edcConfiguration,
            final ContractNegotiationService contractNegotiationService, final EdcDataPlaneClient edcDataPlaneClient,
            final EndpointDataReferenceStorage endpointDataReferenceStorage, final AsyncPollingService pollingService,
            final RetryRegistry retryRegistry, final EDCCatalogFacade catalogFacade,
            final EndpointDataReferenceCacheService endpointDataReferenceCacheService) {
        return new EdcSubmodelClientImpl(edcConfiguration, contractNegotiationService, edcDataPlaneClient,
                endpointDataReferenceStorage, pollingService, retryRegistry, catalogFacade,
                endpointDataReferenceCacheService);
    }

    @Bean
    @ConditionalOnProperty(prefix = CONFIG_PREFIX, name = CONFIG_FIELD_TYPE, havingValue = CONFIG_VALUE_DECENTRAL)
    public DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient(
            @Qualifier(EDC_REST_TEMPLATE) final RestTemplate edcRestTemplate,
            @Value("${digitalTwinRegistryClient.shellDescriptorTemplate:}") final String shellDescriptorTemplate,
            @Value("${digitalTwinRegistryClient.lookupShellsTemplate:}") final String lookupShellsTemplate) {
        return new DecentralDigitalTwinRegistryClient(edcRestTemplate, shellDescriptorTemplate, lookupShellsTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(POOL_SIZE);
    }

}
