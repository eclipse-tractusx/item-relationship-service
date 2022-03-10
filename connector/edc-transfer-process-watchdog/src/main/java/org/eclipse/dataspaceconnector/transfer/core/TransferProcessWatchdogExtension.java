//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package org.eclipse.dataspaceconnector.transfer.core;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.transfer.core.transfer.TransferProcessWatchdog;

import java.util.Set;

/**
 * TransferProcessWatchdog extension
 */
public class TransferProcessWatchdogExtension implements ServiceExtension {


    /**
     * TransferProcess batch size.
     * This amount of processes will be monitored on each iteration by the watchdog.
     */
    private static final int BATCH_SIZE = 5;

    /**
     * Monitor for logging
     */
    private Monitor monitor;
    /**
     * Extension context
     */
    private ServiceExtensionContext context;
    /**
     * TransferProcessWatchdog monitoring long running processes
     */
    private TransferProcessWatchdog watchdog;

    @Override
    public Set<String> requires() {
        return Set.of("dataspaceconnector:transfer-process-manager");
    }

    @Override
    public void initialize(final ServiceExtensionContext context) {
        monitor = context.getMonitor();
        this.context = context;

        watchdog = TransferProcessWatchdog.builder()
                        .monitor(monitor)
                        .interval(Double.parseDouble(context.getSetting("edc.watchdog.interval.seconds", "1")))
                        .stateTimeout(Double.parseDouble(context.getSetting("edc.watchdog.timeout.seconds", "60")))
                        .batchSize(BATCH_SIZE)
                        .build();

        monitor.info("Initialized Transfer Process Watchdog extension");
    }

    @Override
    public void start() {
        final var transferProcessStore = context.getService(TransferProcessStore.class);
        watchdog.start(transferProcessStore);
        monitor.info("Started Transfer Process Watchdog extension");
    }

    @Override
    public void shutdown() {
        if (watchdog != null) {
            watchdog.stop();
        }
        monitor.info("Shutdown Transfer Process Watchdog extension");
    }

}
