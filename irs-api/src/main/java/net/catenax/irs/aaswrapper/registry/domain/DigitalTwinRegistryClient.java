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

/**
 * Digital Twin Registry Rest Client
 */
//@Profile("!local")
//@FeignClient(
//      contextId = "digitalTwinRegistryClientContextId",
//      value = "digitalTwinRegistryClient",
//      url = "${feign.client.config.digitalTwinRegistry.url}",
//      configuration = DigitalTwinRegistryClientConfiguration.class)
public interface DigitalTwinRegistryClient {

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns a specific Asset Administration Shell Descriptor
     */
    //    @GetMapping(value = "/registry/shell-descriptors/{aasIdentifier}", consumes = APPLICATION_JSON_VALUE)
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(String aasIdentifier);

}
