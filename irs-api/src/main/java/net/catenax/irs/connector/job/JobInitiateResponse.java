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

import java.util.Map;

import lombok.Builder;
import lombok.Value;

/**
 * Represents the result of a {@link JobOrchestrator#startJob(Map)} operation.
 */
@Value
@Builder(toBuilder = true)
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
