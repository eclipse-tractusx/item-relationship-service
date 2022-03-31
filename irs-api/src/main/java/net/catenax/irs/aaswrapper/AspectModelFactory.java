//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;

/**
 * Factory Class for creation of AspectModels
 */
@AllArgsConstructor
public class AspectModelFactory {

    private final ObjectMapper objectMapper;

    public AspectModel createAspectModel(final String jsonString, final AspectModelTypes aspectModelType)
            throws JsonProcessingException {
        switch (aspectModelType) {
            case ASSEMBLY_PART_RELATIONSHIP:
                return objectMapper.readValue(jsonString, AssemblyPartRelationship.class);
            case SERIAL_PART_TYPIZATION:
                return objectMapper.readValue(jsonString, SerialPartTypization.class);
            default:
                return null;
        }
    }
}
