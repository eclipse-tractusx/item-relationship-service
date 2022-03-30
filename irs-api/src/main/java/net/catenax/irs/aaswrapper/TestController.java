//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.services.BoMQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test purposes only controller
 * TODO: Remove this class before merging to main branch
 */
//@Hidden
@ExcludeFromCodeCoverageGeneratedReport
@RestController
@RequiredArgsConstructor
@Slf4j
class TestController {

    /**
     * AAS Wrapper Rest Client
     */
    private final AASWrapperClient aasWrapperClient;

    /**
     * Item Tree Query Service
     */
    private final BoMQueryService boMQueryService;

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns all Asset Administration Shell Descriptors
     */
    @GetMapping("/registry/shell-descriptors/{aasIdentifier}")
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(
            @PathVariable("aasIdentifier") final String aasIdentifier) {
        return aasWrapperClient.getAssetAdministrationShellDescriptor(aasIdentifier);
    }

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns all Asset Administration Shell Descriptors
     */
    @GetMapping("/getSubmodel/{aasIdentifier}")
    public List<AssemblyPartRelationship> getSubmodelEndpoints(
            @PathVariable("aasIdentifier") final String aasIdentifier) {
        AspectModelTypes aspectModel = AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP;
        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasWrapperClient.getAssetAdministrationShellDescriptor(
                aasIdentifier);
        final List<String> endpointsForAspectModel = boMQueryService.getEndpointsForAspectModel(
                assetAdministrationShellDescriptor, aspectModel);
        final List<AssemblyPartRelationship> submodels = new ArrayList<>();
        endpointsForAspectModel.forEach(s -> submodels.add(boMQueryService.getSubmodel(s, aspectModel)));
        return submodels;
    }

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns all Asset Administration Shell Descriptors
     */
    @GetMapping("/getItemTreeTest/{aasIdentifier}")
    public List<AspectModel> getItemTreeTest(@PathVariable("aasIdentifier") final String aasIdentifier) {
        AspectModelTypes aspectModel = AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP;
        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasWrapperClient.getAssetAdministrationShellDescriptor(
                aasIdentifier);
        final List<String> endpointsForAspectModel = boMQueryService.getEndpointsForAspectModel(
                assetAdministrationShellDescriptor, aspectModel);
        final List<AspectModel> submodels = new ArrayList<>();
        endpointsForAspectModel.forEach(s -> submodels.add(aasWrapperClient.getSubmodel(s, aspectModel)));
        submodels.add(aasWrapperClient.getSubmodel("test", AspectModelTypes.SERIAL_PART_TYPIZATION));
        return submodels;
    }

}

