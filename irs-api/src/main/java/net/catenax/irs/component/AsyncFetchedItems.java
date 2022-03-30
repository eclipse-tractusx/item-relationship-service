package net.catenax.irs.component;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;


@Value
@Builder
@JsonDeserialize(builder = AsyncFetchedItems.AsyncFetchedItemsBuilder.class)
public class AsyncFetchedItems {

   @Schema(description = "No of job with the globalAssetId on the queue", implementation = Integer.class)
   private Integer queue;

   @Schema(description = "Summary of running job with the globalAssetId", implementation = Integer.class)
   private Integer running;

   @Schema(description = "Summary of completed job with the globalAssetId", implementation = Integer.class)
   private Integer complete;

   @Schema(description = "Summary of failed job with the globalAssetId", implementation = Integer.class)
   private Integer failed;

   @Schema(description = "Summary of lost job with the globalAssetId", implementation = Integer.class)
   private Integer lost;

   @JsonPOJOBuilder(withPrefix = "with")
   public static class AsyncFetchedItemsBuilder {}
}
