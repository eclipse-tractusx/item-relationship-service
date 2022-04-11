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
 * @param <T> type of the DataRequest
 * @param <P> type of the TransferProcess
 */
public interface TransferProcessManager<T extends DataRequest, P extends TransferProcess> {
    TransferInitiateResponse initiateRequest(T dataRequest, Consumer<P> transferProcessCompleted);
}
