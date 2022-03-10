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


import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;


/**
 * Represents the state of a {@link MultiTransferJob}.
 * <p>
 * This class is inspired by the {@link TransferProcessStates} class.
 */
public enum JobState {
    UNSAVED,
    INITIAL,
    IN_PROGRESS,
    TRANSFERS_FINISHED,
    COMPLETED,
    ERROR;
}
