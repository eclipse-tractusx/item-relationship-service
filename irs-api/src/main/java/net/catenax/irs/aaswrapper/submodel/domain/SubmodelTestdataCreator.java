//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to create Submodel Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
class SubmodelTestdataCreator {
    private final List<AssemblyPartRelationship> testData;

    SubmodelTestdataCreator() {
        testData = new ArrayList<>();
        testData.add(getDummyAssemblyPartRelationshipWithChildren("8a61c8db-561e-4db0-84ec-a693fc5ffdf6",
                List.of("09b48bcc-8993-4379-a14d-a7740e1c61d4", "5ce49656-5156-4c8a-b93e-19422a49c0bc",
                        "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d")));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("09b48bcc-8993-4379-a14d-a7740e1c61d4",
                List.of("c35ee875-5443-4a2d-bc14-fdacd64b9446", "8c1407e8-a911-4236-ac0a-03c48e6cbf19",
                        "ea724f73-cb93-4b7b-b92f-d97280ff888b")));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("c35ee875-5443-4a2d-bc14-fdacd64b9446", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("5ce49656-5156-4c8a-b93e-19422a49c0bc", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("8c1407e8-a911-4236-ac0a-03c48e6cbf19", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("ea724f73-cb93-4b7b-b92f-d97280ff888b", List.of()));
    }

    public AssemblyPartRelationship getDummyAssemblyPartRelationshipWithChildren(final String catenaXId,
            final List<String> childIds) {
        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship();
        assemblyPartRelationship.setCatenaXId(catenaXId);
        final Set<ChildData> childData = new HashSet<>();
        childIds.forEach(childId -> {
            final ChildData child = new ChildData();
            child.setChildCatenaXId(childId);
            child.setLifecycleContext(LifecycleContextCharacteristic.ASBUILT);
            childData.add(child);
        });
        assemblyPartRelationship.setChildParts(childData);

        return assemblyPartRelationship;
    }

    public AssemblyPartRelationship createDummyAssemblyPartRelationshipForId(final String catenaXId) {
        final List<AssemblyPartRelationship> collect = testData.stream()
                                                               .filter(assemblyPartRelationship -> assemblyPartRelationship.getCatenaXId().equals(catenaXId))
                                                               .collect(Collectors.toList());
        final AssemblyPartRelationship other = new AssemblyPartRelationship();
        other.setCatenaXId(catenaXId);
        other.setChildParts(Set.of());

        return collect.stream().findFirst().orElse(other);
    }
}
