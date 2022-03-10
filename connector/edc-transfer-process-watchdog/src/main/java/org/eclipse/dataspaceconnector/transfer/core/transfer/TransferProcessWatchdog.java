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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TransferProcess watchdog thread that cancels long running transfer processes after a timeout
 */
// Monitor doesn't offer guard statements
// Class instantiated using lombok
@SuppressWarnings({"PMD.MissingStaticMethodInNonInstantiatableClass", "PMD.DoNotUseThreads", "PMD.GuardLogStatement"})
public final class TransferProcessWatchdog {
    /**
     * Monitor for logging
     */
    private final Monitor monitor;
    /**
     * Transfer process batch size
     */
    private final int batchSize;
    /**
     * Watchdog pollin interval
     */
    private final Duration interval;
    /**
     * Transfer process timeout
     */
    private final Duration stateTimeout;
    /**
     * Executor service
     */
    private ScheduledExecutorService executor;

    /**
     * @param monitor The monitor
     * @param batchSize Transfer process batch size for each iteration
     * @param interval Watchdog polling interval in seconds
     * @param stateTimeout Process timeout in seconds
     */
    @Builder
    private TransferProcessWatchdog(final Monitor monitor, final int batchSize, final double interval, final double stateTimeout) {
        this.monitor = monitor;
        this.batchSize = batchSize;
        this.interval = Duration.of(secondsToMillis(interval), ChronoUnit.MILLIS);
        this.stateTimeout = Duration.of(secondsToMillis(stateTimeout), ChronoUnit.MILLIS);
    }

    /**
     * Starts the watchdog thread
     * @param processStore Process store
     */
    public void start(final TransferProcessStore processStore) {
        final var action = CancelLongRunningProcesses.builder()
                .monitor(monitor)
                .stateTimeout(stateTimeout)
                .batchSize(batchSize)
                .transferProcessStore(processStore)
                .build();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(action, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        monitor.info("TransferProcessWatchdog started (timeout=" + stateTimeout.toMillis() + "ms)");
    }

    /**
     * Stop the watchdog thread
     */
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private long secondsToMillis(final double seconds) {
        return (long) (seconds * 1000);
    }
}
