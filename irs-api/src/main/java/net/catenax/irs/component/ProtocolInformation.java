package net.catenax.irs.component;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ProtocolInformation {

   @Schema(description = "Uniform resource identifier of endpoint", example = "https://catena-x.net/vehicle/basedetails/", implementation = java.net.URI.class)
   private String endpointAddress;

   @Schema(description = "Protocol used to access the endpoint", example = "http or https", implementation = java.net.URI.class)
   private String endpointProtocol;

   @Schema(description = "Protocol version", example = "1.0", implementation = String.class)
   private String enpointProtocolVersion;
}
