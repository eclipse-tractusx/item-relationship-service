# edc-transfer-process-watchdog

This extension starts a watchdog thread that cleans up long-running consumer transfer processes. Processes in state `IN_PROGRESS` are tracked and moved to state `ERROR` after a timeout expires. This prevents the consumer connector to poll for the results of providers indefinitely, when the provider fails to provide one.

The watchdog has no effect on provider processes.

## Configuration

The watchdog thread can be configured with the following properties:

* `edc.watchdog.interval.seconds`: polling interval in seconds (decimal values allowed) for running transfer processes with default of 1s.
* `edc.watchdog.timeout.seconds`: timeout in seconds (decimal values allowed) after which processes are cancelled with default of 60s.

## Known issues and limitations

### Race conditions

Watchdog process status updates to ERROR might cause a race condition with the updates performed from the main loop in `TransferProcessManagerImpl`. At the moment EDC offers no capabilities of preventing this in any way. Issue [#330](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector/issues/330) focuses on reworking `TransferProcessManagerImpl` to prevent such situations.

The consequences of this race condition are mild though. If a process happens to finish at the same time as its timeout the resulting state of the process (COMPLETED vs ERROR) will be determined by the thread that manages to update the process the last.

### Process batches

At the moment the watchdog monitors batches of 5 active transfer processes on each loop. This behaviour is similar to that of the main loop of `TransferProcessManager`, and can lead to some situations where some processes are never cancelled if the system is under load. Issue [#393](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector/issues/393) addresses this topic for the `TransferProcessManager`, a similar solution should be applied for the watchdog once it is resolved.