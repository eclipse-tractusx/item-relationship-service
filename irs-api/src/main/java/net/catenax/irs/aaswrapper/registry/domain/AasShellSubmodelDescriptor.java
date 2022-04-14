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

import lombok.Getter;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.SubmodelType;

/**
 * Asset Administration Shell Submodel Descriptor
 */
@Getter
public class AasShellSubmodelDescriptor extends AbstractAasShell {
    private final SubmodelType submodelType;
    private final String submodelEndpointAddress;

    public AasShellSubmodelDescriptor(final String idShort, final String identification, final NodeType nodeType,
            final SubmodelType submodelType, final String submodelEndpointAddress) {
        super(idShort, identification, nodeType);
        this.submodelType = submodelType;
        this.submodelEndpointAddress = submodelEndpointAddress;
    }

    @Override
    public Shell getShell() {
        return null;
    }
}
