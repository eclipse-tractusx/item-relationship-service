//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.TestData;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;

/**
 * Class to create Testdata from a JSON File
 */
@AllArgsConstructor
@Slf4j
public class TestdataCreator {

    private final ObjectMapper objectMapper;

    public List<TestData> getTestData(final File testdataFile) {
        try {
            final List<TestData> testData = Arrays.stream(objectMapper.readValue(testdataFile, TestData[].class))
                                                  .collect(Collectors.toList());
            return testData.stream().map(testData1 -> {
                if (testData1.getAssemblyPartRelationship().getChildParts() == null) {
                    return new TestData(testData1.getCatenaXId(),
                            List.of(new AssemblyPartRelationship(testData1.getCatenaXId(), Set.of())));
                } else {
                    return testData1;
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

    public AssemblyPartRelationship getDummyAssemblyPartRelationshipFromAASDescriptor(final List<TestData> testData,
            final AssetAdministrationShellDescriptor aasDescriptor) {
        final String catenaXId = aasDescriptor.getIdentification();
        return createDummyAssemblyPartRelationshipForId(testData, catenaXId);
    }

    public AssemblyPartRelationship createDummyAssemblyPartRelationshipForId(final List<TestData> testData,
            final String catenaXId) {

        final List<TestData> collect = testData.stream()
                                               .filter(testData1 -> testData1.getCatenaXId().equals(catenaXId))
                                               .collect(Collectors.toList());
        final Optional<TestData> returnValue = collect.stream().findFirst();
        if (returnValue.isPresent()) {
            return returnValue.get().getAssemblyPartRelationship();
        } else {
            return new AssemblyPartRelationship(catenaXId, Set.of());
        }
    }

//    public AssetAdministrationShellDescriptor createAASShellDescriptorForIdFromTestData(final String catenaXId,
//            final List<TestData> testData) {
//        final AssemblyPartRelationship assemblyPartRelationship = createDummyAssemblyPartRelationshipForId(testData,
//                catenaXId);
//        return createDummyAssetAdministrationShellDescriptorForId(catenaXId, List.of(assemblyPartRelationship));
//    }
//
//    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(final String catenaXId,
//            final List<AspectModel> endpointAspectModels) {
//
//        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
//        endpointAspectModels.forEach(aspectModel -> submodelDescriptors.add(
//                createAssemblyPartRelationshipSubmodelDescriptor(catenaXId + "/aas/assembly-part-relationship",
//                        catenaXId)));
//
//        return AssetAdministrationShellDescriptor.builder()
//                                                 .description(List.of(new LangString("en", "Test data")))
//                                                 .globalAssetId(new Reference(
//                                                         List.of("urn:twin:com.test#d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")))
//                                                 .idShort("testdata")
//                                                 .identification(catenaXId)
//                                                 .specificAssetIds(List.of(new IdentifierKeyValuePair(
//                                                                 "http://test.com/datamodel/common", "0000000251"),
//                                                         new IdentifierKeyValuePair("urn:VR:wt.part.WTPart#",
//                                                                 "25054146@nis11c130.epdm-d.edm.dsh.de")))
//                                                 .submodelDescriptors(submodelDescriptors)
//                                                 .build();
//    }
//
//    private SubmodelDescriptor createAssemblyPartRelationshipSubmodelDescriptor(final String endpointAddress,
//            final String catenaXId) {
//        return SubmodelDescriptor.builder()
//                                 .identification(catenaXId)
//                                 .idShort("idShort")
//                                 .semanticId(new Reference(
//                                         List.of("urn:bamm:com.catenax.assembly_part_relationtship:1.0.0")))
//                                 .endpoints(List.of(new Endpoint("https://OEM.connector",
//                                         new ProtocolInformation("edc://test.url/shells/" + endpointAddress,
//                                                 "AAS/SUBMODEL", "1.0RC02"))))
//                                 .build();
//    }
}
