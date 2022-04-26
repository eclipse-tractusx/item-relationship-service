//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.catenax.irs.connector.job.TransferProcess;

/**
 * Transfer Process for AAS Objects
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AASTransferProcess implements TransferProcess {

    @SuppressWarnings("PMD.ShortVariable")
    private String id;

    private Integer depth;

    private final List<String> idsToProcess = new ArrayList<>();

    public void addIdsToProcess(final List<String> childIds) {
        idsToProcess.addAll(childIds);
    }

}
