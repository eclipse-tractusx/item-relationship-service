package net.catenax.irs.component;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Schema(description="Provide descriptions to request parts ")
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = Description.DescriptionBuilder.class)
public class Description {

   @Schema(description = "language use to describe", example = "en")
   private String language;

   @Schema(description = "Description test", example = "The shell for a vehicle")
   private String text;
}
