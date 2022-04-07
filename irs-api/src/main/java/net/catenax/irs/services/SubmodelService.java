//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.submodel.domain.AssemblyPartRelationship;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving the submodel json from a remote api
 */
@Slf4j
@Service
public class SubmodelService {
    private final SubmodelClient client;

    public SubmodelService(final SubmodelClient client) {
        this.client = client;
    }

    public AssemblyPartRelationship retrieveSubmodel(final String level, final String content, final String extent) {
        // To do
        final AssemblyPartRelationship assemblyPartRelationship = this.client.getSubmodel(level, content, extent);
        log.debug("assemblyPartRelationship {}", assemblyPartRelationship);

        return assemblyPartRelationship;
    }
}
