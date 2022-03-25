package net.catenax.irs.component;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.dtos.IrsPartRelationship;

@ApiModel(description = "Information about request items tree job")
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = IrsPartRelationshipsWithInfos.IrsPartRelationshipsWithInfosBuilder.class)
public class IrsPartRelationshipsWithInfos {

   @Schema(description = "", implementation = Job.class)
   Job job;

   List<IrsPartRelationship> relationships;

   List<Shells> shells;

}
