package net.catenax.irs.component;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.component.enums.BomLifecycle;

/*** API type for ChildItem name/url entry. */
@Schema(description = "ChildItem ")
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = ChildItem.ChildItemBuilder.class)
public class ChildItem {

   @Schema(description = "Quantity component", implementation = Quantity.class)
   private Quantity quantity;

   @Schema(description = "Unique identifier of a single, unique (sub)component/part/batch,given by its globalAssetId/ digital twin id",
           implementation = BomLifecycle.class)
   private String lifecycleContext;

   @Schema(description = "Datetime of assembly", implementation = Instant.class)
   private Instant assembledOn;

   @Schema(description = "Last date and time part was modified", implementation = Instant.class)
   private Instant lastModifiedOn;

   @Schema(description = "CatenaX child Id", implementation = Instant.class)
   private String childCatenaXId;
}
