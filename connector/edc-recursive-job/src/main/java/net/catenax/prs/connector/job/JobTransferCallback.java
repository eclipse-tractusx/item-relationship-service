//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.job;


import lombok.RequiredArgsConstructor;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessListener;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

/**
 * {@TransferProcessObservable} callback invoked when a transfer has completed.
 * Used by the {@JobOrchestrator} to be notified of transfer completions.
 */
@RequiredArgsConstructor
class JobTransferCallback implements TransferProcessListener {

    /**
     * Job orchestrator.
     */
    private final JobOrchestrator jobOrchestrator;

    /**
     * Callback invoked by the EDC framework when a transfer has completed.
     *
     * @param process
     */
    @Override
    public void completed(final TransferProcess process) {
        jobOrchestrator.transferProcessCompleted(process);
    }
}
