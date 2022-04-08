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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.connector.job.TransferProcess;

/**
 * Transfer Process for AAS Objects
 */
@Getter
@RequiredArgsConstructor
public class AASTransferProcess implements TransferProcess {

    @Getter(AccessLevel.NONE)
    private final String transferProcessId;

    private final List<String> idsToProcess = new ArrayList<>();

    public void addIdsToProcess(final List<String> childIds) {
        idsToProcess.addAll(childIds);
    }

    @Override
    public String getId() {
        return transferProcessId;
    }
}
