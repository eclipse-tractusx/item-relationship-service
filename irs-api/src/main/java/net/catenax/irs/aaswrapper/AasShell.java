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

import java.util.List;
import java.util.Map;

import lombok.Getter;
import net.catenax.irs.component.Description;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.dto.SubmodelEndpoint;

/**
 * Asset Administration Shell Response Class
 */
@Getter
public class AasShell extends AbstractAasShell {
    private final List<Description> descriptions;
    private final GlobalAssetIdentification globalAssetIdentification;
    private final Map<String, String> specificAssetIds;
    private final List<SubmodelEndpoint> submodelEndpoints;

    public AasShell(final String idShort, final String identification, final NodeType nodeType,
            final List<Description> descriptions, final GlobalAssetIdentification globalAssetIdentification,
            final Map<String, String> specificAssetIds, final List<SubmodelEndpoint> submodelEndpoints) {
        super(idShort, identification, nodeType);
        this.descriptions = descriptions;
        this.globalAssetIdentification = globalAssetIdentification;
        this.specificAssetIds = specificAssetIds;
        this.submodelEndpoints = submodelEndpoints;
    }

    @Override
    public Shell getShell() {
        return this;
    }
}
