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


import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.util.stream.Stream;

/**
 * Interface for extensions to provide the logic to build jobs with
 * custom logic to run multiple transfers.
 */
public interface RecursiveJobHandler {
    /**
     * Start the recursive process by creating any number of transfers.
     *
     * @param job job definition.
     * @return a stream of {@DataRequest}. One data transfer will be initiated for each item.
     */
    Stream<DataRequest> initiate(MultiTransferJob job);

    /**
     * Continue the recursive process by creating any number of transfers from
     * the result of a completed transfer.
     *
     * @param job             job definition.
     * @param transferProcess completed transfer.
     * @return a stream of {@DataRequest}. One data transfer will be initiated for each item.
     */
    Stream<DataRequest> recurse(MultiTransferJob job, TransferProcess transferProcess);

    /**
     * Called when all transfers in the job have completed.
     *
     * @param job job definition.
     */
    void complete(MultiTransferJob job);
}
