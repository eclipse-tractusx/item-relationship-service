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

/**
 * Abstract Asset Administration Shell
 */
@Getter
public abstract class AbstractAasShell implements Shell {

    private final String idShort;
    private final String identification;
    private final NodeType nodeType;

    protected AbstractAasShell(final String idShort, final String identification, final NodeType nodeType) {
        this.idShort = idShort;
        this.identification = identification;
        this.nodeType = nodeType;
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean isNodeType(final NodeType nodeType) {
        return this.nodeType.equals(nodeType);
    }
}
