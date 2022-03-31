//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aaswrapper.registry.domain.model.LangString;
import net.catenax.irs.aaswrapper.registry.domain.model.Reference;

/**
 * SubmodelDescriptor
 */
@Data
@AllArgsConstructor
@Builder
public class SubmodelDescriptor {

    /**
     * identification
     */
    private String identification;
    /**
     * idShort
     */
    private String idShort;
    /**
     * semanticId
     */
    private Reference semanticId;

    /**
     * endpoints
     */
    private List<Endpoint> endpoints;
    /**
     * description
     */
    private List<LangString> description;
}
