package net.catenax.irs.component;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.dtos.IrsPartRelationship;

@ApiModel(description = "Information about request items tree job")
@Value
@Builder
@JsonDeserialize(builder = IrsPartRelationshipsWithInfos.IrsPartRelationshipsWithInfosBuilder.class)
public class IrsPartRelationshipsWithInfos {

   @Schema(description = "", implementation = Job.class)
   Jobs job;

   List<IrsPartRelationship> relationships;

   List<Shells> shells;


   @JsonPOJOBuilder(withPrefix = "with")
   public static class IrsPartRelationshipsWithInfosBuilder {}

}
