//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * Response for job status request
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class JobStatusResult {

    /**
     * Job identifier
     */
    private UUID jobId;

    /**
     * Current status for this job
     */
    private JobState status;

}
