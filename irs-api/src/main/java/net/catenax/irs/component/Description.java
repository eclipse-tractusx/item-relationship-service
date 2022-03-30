package net.catenax.irs.component;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Schema(description="Provide descriptions to request parts ")
@Value
@Builder
@JsonDeserialize(builder = Description.DescriptionBuilder.class)
public class Description {

   @Schema(description = "language use to describe", example = "en", implementation = String.class)
   private String language;

   @Schema(description = "Description test", example = "The shell for a vehicle", implementation = String.class)
   private String text;

   @JsonPOJOBuilder(withPrefix = "with")
   public static class DescriptionBuilder {}
}
