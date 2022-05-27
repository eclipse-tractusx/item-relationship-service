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
