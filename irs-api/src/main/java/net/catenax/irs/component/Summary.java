package net.catenax.irs.component;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.component.AsyncFetchedItems;

@Value
@Builder
public class Summary {

   @Schema(description = "")
   AsyncFetchedItems asyncFetchedItems;

}
