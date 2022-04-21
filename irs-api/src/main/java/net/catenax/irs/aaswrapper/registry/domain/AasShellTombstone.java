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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.dto.ProcessingError;

/**
 * Asset Administration Shell Response Tombstone
 */
@Getter
@ToString
@JsonDeserialize(builder = AasShellTombstone.AasShellTombstoneBuilder.class)
public class AasShellTombstone extends AbstractAasShell implements Tombstone {
    private final ProcessingError processingError;

    @Builder(setterPrefix = "with")
    public AasShellTombstone(final String idShort, final String identification, final ProcessingError processingError) {
        super(idShort, identification, NodeType.TOMBSTONE);
        this.processingError = processingError;
    }
    @Builder(setterPrefix = "with")
    public AasShellTombstone(final String idShort, final String identification, final ProcessingError processingError, final NodeType nodeType) {
        super(idShort, identification, nodeType);
        this.processingError = processingError;
    }

}
