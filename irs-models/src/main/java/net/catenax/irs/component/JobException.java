package net.catenax.irs.component;

import java.time.Instant;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Exception container for job
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = JobException.JobExceptionBuilder.class)
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
@ExcludeFromCodeCoverageGeneratedReport
public class JobException {

   @Min(value = 0)
   @Max(value = 100)
   @Schema(description = "Name of the exception occurred", implementation = String.class)
   private String exceptionName;

   @Min(value = 0)
   @Max(value = 400)
   @Schema(description = "Detail information for the error occurred", implementation = String.class)
   private String errorDetail;

   @Schema(description = "Datetime when error occurred", implementation = Instant.class)
   private Instant exceptionDate;

   /**
    * Builder class
    */
   @JsonPOJOBuilder(withPrefix = "with")
   public static class JobExceptionBuilder {
   }

}
