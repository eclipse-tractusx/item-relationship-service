package net.catenax.irs.component;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.component.AsyncFetchedItems;

@Value
@Builder
@JsonDeserialize(builder = Summary.SummaryBuilder.class)
public class Summary {

   @Schema(description = "Summary of the fetched jobs", implementation = AsyncFetchedItems.AsyncFetchedItemsBuilder.class)
   AsyncFetchedItems asyncFetchedItems;

   @JsonPOJOBuilder(withPrefix = "with")
   public static class SummaryBuilder {}

}
