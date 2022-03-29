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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.catenax.irs.aaswrapper.registry.domain.model.AdministrativeInformation;
import net.catenax.irs.aaswrapper.registry.domain.model.Descriptor;
import net.catenax.irs.aaswrapper.registry.domain.model.IdentifierKeyValuePair;
import net.catenax.irs.aaswrapper.registry.domain.model.LangString;
import net.catenax.irs.aaswrapper.registry.domain.model.Reference;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelDescriptor;

/**
 * AssetAdministrationShellDescriptor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAdministrationShellDescriptor extends Descriptor {

    /**
     * administration
     */
    private AdministrativeInformation administration;
    /**
     * description
     */
    private List<LangString> description;
    /**
     * globalAssetId
     */
    private Reference globalAssetId;
    /**
     * idShort
     */
    private String idShort;
    /**
     * identification
     */
    private String identification;
    /**
     * specificAssetIds
     */
    private List<IdentifierKeyValuePair> specificAssetIds;
    /**
     * submodelDescriptors
     */
    private List<SubmodelDescriptor> submodelDescriptors;

}
