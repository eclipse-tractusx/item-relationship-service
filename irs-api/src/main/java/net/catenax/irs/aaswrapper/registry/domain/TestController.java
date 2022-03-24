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

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test purposes only controller
 * TODO: Remove this class before merging to main branch
 */
@Hidden
@ExcludeFromCodeCoverageGeneratedReport
@RestController
@RequiredArgsConstructor
@Slf4j
class TestController {

    /**
     * Digital Twin Registry Rest Client
     */
    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns all Asset Administration Shell Descriptors
     */
    @GetMapping("/registry/shell-descriptors/{aasIdentifier}")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(@PathVariable("aasIdentifier") final String aasIdentifier) {
        return digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aasIdentifier);
    }
}

