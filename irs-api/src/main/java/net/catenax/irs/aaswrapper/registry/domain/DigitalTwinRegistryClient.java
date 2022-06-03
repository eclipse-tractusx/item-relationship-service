//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import static net.catenax.irs.configuration.RestTemplateConfig.OAUTH_REST_TEMPLATE;

import java.net.URI;

import net.catenax.irs.component.assemblypartrelationship.AssetAdministrationShellDescriptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
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

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
@Profile({"local", "test"})
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    private final AssetAdministrationShellTestdataCreator testdataCreator = new AssetAdministrationShellTestdataCreator();

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        if ("urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d".equals(aasIdentifier)) {
            throw new RestClientException("Dummy Exception");
        }
        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier);
    }
}


/**
 * Digital Twin Registry Rest Client Implementation
 */
@Service
@Profile({"!local && !test"})
class DigitalTwinRegistryClientImpl implements DigitalTwinRegistryClient {

    private final RestTemplate restTemplate;
    private final String aasProxyUrl;

    /* package */ DigitalTwinRegistryClientImpl(@Qualifier(OAUTH_REST_TEMPLATE) final RestTemplate restTemplate, @Value("${aasProxy.url:}") final String aasProxyUrl) {
        this.restTemplate = restTemplate;
        this.aasProxyUrl = aasProxyUrl;
    }

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        return restTemplate.getForObject(buildUri(aasIdentifier), AssetAdministrationShellDescriptor.class);
    }

    private URI buildUri(final String aasIdentifier) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(aasProxyUrl);
        uriBuilder.path("/registry/shell-descriptors/").path(aasIdentifier);

        return uriBuilder.build().toUri();
    }
}
