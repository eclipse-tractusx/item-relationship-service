//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.service;

import lombok.Builder;
import lombok.Value;
import net.catenax.prs.connector.job.JobState;

/**
 * Response of a transfer process status check
 */
@Value
@Builder
public class StatusResponse {
    /**
     * Transfer process status
     */
    private final JobState status;
    /**
     * SAS Token in case the process is COMPLETED
     */
    private final String sasToken;
}
