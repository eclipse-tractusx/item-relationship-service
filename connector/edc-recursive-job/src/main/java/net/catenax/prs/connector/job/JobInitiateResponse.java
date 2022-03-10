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


import lombok.Builder;
import lombok.Value;
import org.eclipse.dataspaceconnector.spi.transfer.TransferInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;

import java.util.Map;

/**
 * Represents the result of a {@link JobOrchestrator#startJob(Map)} operation.
 * <p>
 * This class is modeled after the {@link TransferInitiateResponse} class.
 */
@Value
@Builder
public class JobInitiateResponse {
    /**
     * Job identifier.
     */
    private final String jobId;

    /**
     * Optional error message.
     */
    private final String error;

    /**
     * Response status.
     */
    private final ResponseStatus status;
}
