package net.catenax.irs.component;

import io.swagger.v3.oas.annotations.media.Schema;

public class EndPoint {

   @Schema(description = "")
   private String interfaceType;

   @Schema(description = "Informaiton to the interface used", implementation = ProtocolInformation.class)
   private ProtocolInformation protocolInformation;
}
