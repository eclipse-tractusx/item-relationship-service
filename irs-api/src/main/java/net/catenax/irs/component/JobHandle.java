package net.catenax.irs.component;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;

@ApiModel(description = "The unique jobId handle of the just processed job.")
@Value
@Builder
@JsonDeserialize(builder = JobHandle.JobHandleBuilder.class)
public class JobHandle {

   private UUID jobId;

   @JsonPOJOBuilder(withPrefix = "with")
   public static class JobHandleBuilder {
   }
}
