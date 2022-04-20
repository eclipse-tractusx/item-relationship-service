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
import lombok.ToString;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;

/**
 * Asset Administration Shell Response Tombstone
 */
@Getter
@ToString
public class AasShellTombstone extends AbstractAasShell {
    private final ProcessingError processingError;

    public AasShellTombstone(final String idShort, final String identification, final ProcessingError processingError) {
        super(idShort, identification, NodeType.TOMBSTONE);
        this.processingError = processingError;
    }

    @Override
    public Shell getShell() {
        return this;
    }
}
