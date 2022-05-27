//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Response for job status request
 */
@Data
@Jacksonized
@Builder(toBuilder = true)
public class JobStatusResult {

    /**
     * Job identifier
     */
    private String jobId;

    /**
     * Current status for this job
     */
    private String status;

}
