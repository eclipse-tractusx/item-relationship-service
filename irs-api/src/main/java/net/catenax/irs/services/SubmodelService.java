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
import net.catenax.irs.aaswrapper.submodel.domain.ClientBuilder;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving the submodel json from a remote api
 */
@Slf4j
@Service
public class SubmodelService {

    private final ClientBuilder clientBuilder;

    public SubmodelService(final ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public AssemblyPartRelationship retrieveSubmodel(final String uri) {
        AssemblyPartRelationship assemblyPartRelationship = null;

        final SubmodelClient client = this.clientBuilder.createClient(SubmodelClient.class, uri);

        if (client != null) {
            // To do
            assemblyPartRelationship = client.getSubmodel("", "", "");
            log.debug("assemblyPartRelationship {}", assemblyPartRelationship.toString());
        }

        return assemblyPartRelationship;
    }
}
