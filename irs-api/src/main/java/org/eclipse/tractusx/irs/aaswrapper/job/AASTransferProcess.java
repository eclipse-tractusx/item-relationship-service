//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.tractusx.irs.connector.job.TransferProcess;

/**
 * Transfer Process for AAS Objects
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AASTransferProcess implements TransferProcess {

    private final List<String> idsToProcess = new ArrayList<>();
    @SuppressWarnings("PMD.ShortVariable")
    private String id;
    private Integer depth;

    public void addIdsToProcess(final List<String> childIds) {
        idsToProcess.addAll(childIds);
    }

}
