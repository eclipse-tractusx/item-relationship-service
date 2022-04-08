package net.catenax.irs.component;

import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * contains a collection of JobHandle
 */
@ApiModel(description = "Collection of job handle")
@Value
@Builder
@JsonDeserialize(builder = JobHandleCollection.JobHandleCollectionBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class JobHandleCollection {

   private Collection<JobHandle> jobHandleCollection;

   /**
    * Builder class
    */
   @JsonPOJOBuilder(withPrefix = "with")
   public static class JobHandleCollectioneBuilder {
   }
}
