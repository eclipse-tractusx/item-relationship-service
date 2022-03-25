package net.catenax.irs.component;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "")
public class Shell {

   private String identification;

   private String idShort;

   private Map<String, String> specificAssetId;

}
