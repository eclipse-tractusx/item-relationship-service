//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package org.eclipse.dataspaceconnector.transfer.core.transfer;

import lombok.Builder;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;

import java.time.Clock;
import java.time.Duration;

import static java.time.Instant.now;
import static java.time.Instant.ofEpochMilli;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess.Type.CONSUMER;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates.IN_PROGRESS;

/**
 * Cancels long running transfer processes
 */
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
@Builder
class CancelLongRunningProcesses implements Runnable {
    /**
     * Monitor for logging
     */
    private final Monitor monitor;
    /**
     * Transfer process batch size
     */
    private final int batchSize;
    /**
     * TransferProcess timeout
     */
    private final Duration stateTimeout;

    /**
     * Clock (overriden for testing purposes)
     */
    @Builder.Default
    private Clock clock = Clock.systemUTC();

    /**
     * Transfer process store
     */
    private final TransferProcessStore transferProcessStore;

    /**
     * Runs a watchdog loop
     */
    @Override
    public void run() {
        monitor.debug("Watchdog triggered (timeout=" + stateTimeout.toMillis() + "ms)");
        final var transferProcesses = transferProcessStore.nextForState(IN_PROGRESS.code(), batchSize);

        transferProcesses.stream()
            .filter(p -> p.getType() == CONSUMER)
            .filter(p -> ofEpochMilli(p.getStateTimestamp()).isBefore(now(clock).minus(stateTimeout)))
            .forEach(p -> {
                p.transitionError("Timed out waiting for process to complete after > " + stateTimeout.toMillis() + "ms");
                /*
                 * IMPORTANT NOTE: Updating the process here might cause a race condition
                 * with the updates performed from the main loop in TransferProcessManagerImpl.
                 * See README for more details.
                 */
                transferProcessStore.update(p);
                monitor.info("Timeout for process " + p);
            });
    }
}
