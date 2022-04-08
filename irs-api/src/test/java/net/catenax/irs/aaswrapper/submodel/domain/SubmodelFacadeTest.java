/*
 * Copyright (c) 2022. Copyright Holder (Catena-X Consortium)
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * See the LICENSE file(s) distributed with this work for
 * additional information regarding license terms.
 *
 */

package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmodelFacadeTest {

    private SubmodelFacade submodelFacade;

    @BeforeEach
    void setUp() {
        final SubmodelClientLocalStub submodelClient = new SubmodelClientLocalStub();
        submodelFacade = new SubmodelFacade(submodelClient);
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithChildDataWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final AssemblyPartRelationshipDTO submodel = submodelFacade.getSubmodel(catenaXId);
        assertThat(submodel.getCatenaXId()).isEqualTo(catenaXId);
        final Set<ChildDataDTO> childParts = submodel.getChildParts();
        assertThat(childParts).hasSize(3);
        final List<String> childIds = childParts.stream()
                                                .map(ChildDataDTO::getChildCatenaXId)
                                                .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("09b48bcc-8993-4379-a14d-a7740e1c61d4",
                "5ce49656-5156-4c8a-b93e-19422a49c0bc", "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
    }
}