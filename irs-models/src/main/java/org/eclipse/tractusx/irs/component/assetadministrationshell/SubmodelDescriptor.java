//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * SubmodelDescriptor
 */
@Data
@Builder
@Jacksonized
public class SubmodelDescriptor {

    /**
     * administration
     */
    private AdministrativeInformation administration;
    /**
     * description
     */
    private List<LangString> description;
    /**
     * idShort
     */
    private String idShort;
    /**
     * identification
     */
    private String identification;
    /**
     * semanticId
     */
    private Reference semanticId;
    /**
     * endpoint
     */
    private List<Endpoint> endpoints;

    /**
     * @return The first value of the semanticId or null, if none is present.
     */
    @JsonIgnore
    public String getAspectType() {
        return this.getSemanticId().getValue().stream().findFirst().orElse(null);
    }

}
