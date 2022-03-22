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

import java.util.List;

import lombok.Data;

/**
 *
 */
@Data
class AssetAdministrationShellDescriptor extends Descriptor {

    /**
     *
     */
    private AdministrativeInformation administration;
    /**
     *
     */
    private List<LangString> description;
    /**
     *
     */
    private Reference globalAssetId;
    /**
     *
     */
    private String idShort;
    /**
     *
     */
    private String identification;
    /**
     *
     */
    private List<IdentifierKeyValuePair> specificAssetIds;
    /**
     *
     */
    private List<SubmodelDescriptor> submodelDescriptors;

}
