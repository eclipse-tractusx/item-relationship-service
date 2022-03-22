package net.catenax.irs.response;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Value;
import net.catenax.irs.response.enums.JobStatus;

@Value
@Builder
public class Job {

   /**
    * Job identifier.
    */
   @NotNull
   private final String jobId;

   /**
    * Job Status
    */
   private JobStatus jobStatus;

   /**
    *  Timestamp when the job was created
    */
   private Instant createdOn;

   /**
    * Last time job was modified
    */
   private Instant lastModifiedOn;

   /**
    * Mark the time the was completed
    */
   private Instant jobFinished;

   /**
    * Url of request that resulted to this job
    */
   private String requestUrl;

   /**
    * Http method, only GET is supported
    */
   private String action;

   /**
    * Owner of the job
    */
   private String owner;

}
