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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Digital Twin Registry Rest Client
 */
@Profile("prod")
@FeignClient(
        contextId = "digitalTwinRegistryClientContextId",
        value = "digitalTwinRegistryClient",
        url = "${feign.client.config.digitalTwinRegistry.url}",
        configuration = DigitalTwinRegistryClientConfiguration.class)
interface DigitalTwinRegistryClient {

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns a specific Asset Administration Shell Descriptor
     */
    @GetMapping(value = "/registry/shell-descriptors/{aasIdentifier}", consumes = APPLICATION_JSON_VALUE)
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(@PathVariable("aasIdentifier") String aasIdentifier);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        final AssetAdministrationShellTestdataCreator testdataCreator = new AssetAdministrationShellTestdataCreator();
        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier);
    }
}
