//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.registry.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 */
@FeignClient(value = "digitalTwinRegistryClient",
      url = "${feign.client.config.digitalTwinRegistry.url}",
      configuration = DigitalTwinRegistryClientConfiguration.class)
interface DigitalTwinRegistryClient {

    /**
     *
     * @param aasIdentifier as
     * @return asf
     */
    @GetMapping(value = "/registry/shell-descriptors/{aasIdentifier}", consumes = APPLICATION_JSON_VALUE)
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(@PathVariable("aasIdentifier") String aasIdentifier);

}
