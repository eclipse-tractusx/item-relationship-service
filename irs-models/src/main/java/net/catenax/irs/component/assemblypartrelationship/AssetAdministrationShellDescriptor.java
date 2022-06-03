//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.assemblypartrelationship;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * AssetAdministrationShellDescriptor
 */
@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AssetAdministrationShellDescriptor {

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
