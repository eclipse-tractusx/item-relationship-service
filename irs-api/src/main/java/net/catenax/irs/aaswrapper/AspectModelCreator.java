package net.catenax.irs.aaswrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;

@AllArgsConstructor
public class AspectModelCreator {

    private final ObjectMapper objectMapper;

    public AspectModel createAspectModel(String jsonString, AspectModelTypes aspectModelType)
            throws JsonProcessingException {
        switch (aspectModelType) {
        case ASSEMBLY_PART_RELATIONSHIP: {
            return objectMapper.readValue(jsonString, AssemblyPartRelationship.class);
        }
        case SERIAL_PART_TYPIZATION:
            return objectMapper.readValue(jsonString, SerialPartTypization.class);
        default:
            return null;
        }
    }
}
