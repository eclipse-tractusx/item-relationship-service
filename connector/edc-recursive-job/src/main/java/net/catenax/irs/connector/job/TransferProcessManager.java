//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import java.util.function.Consumer;

/**
 * Manages the processes to retrieve data by executing them asynchronously.
 *
 * @param <T> type of the DataRequest
 * @param <P> type of the TransferProcess
 */
public interface TransferProcessManager<T extends DataRequest, P extends TransferProcess> {

    /**
     * Starts a data request asynchronously.
     *
     * @param dataRequest the data request instruction
     * @param transferProcessStarted callback which is executed as soon as a request is being started
     * @param transferProcessCompleted callback which is executed after the request is finished
     *
     * @return the initialization response, indicating the acceptance status of the transfer
     */
    TransferInitiateResponse initiateRequest(T dataRequest, Consumer<String> transferProcessStarted,
            Consumer<P> transferProcessCompleted);
}
