package net.catenax.irs.component;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EndPoint {

   @Schema(description = "Communication interface type", example = "Http", implementation = String.class)
   private String interfaceType;

   @Schema(description = "Informaiton to the interface used", implementation = ProtocolInformation.class)
   private ProtocolInformation protocolInformation;

}
