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

/**
 * Factory Class for creation of AspectModels
 */
@AllArgsConstructor
public class AspectModelFactory {

    private final ObjectMapper objectMapper;

    public AspectModel createAspectModel(final String jsonString, final Class<? extends AspectModel> aspectModelClass)
            throws JsonProcessingException {
        return objectMapper.readValue(jsonString, aspectModelClass);
    }
}
