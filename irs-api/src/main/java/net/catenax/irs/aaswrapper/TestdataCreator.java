package net.catenax.irs.aaswrapper;

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
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aaswrapper.registry.domain.model.IdentifierKeyValuePair;
import net.catenax.irs.aaswrapper.registry.domain.model.LangString;
import net.catenax.irs.aaswrapper.registry.domain.model.ProtocolInformation;
import net.catenax.irs.aaswrapper.registry.domain.model.Reference;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelDescriptor;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;

@AllArgsConstructor
public class TestdataCreator {

    private final ObjectMapper objectMapper;

    public List<TestData> getTestData(File testdataFile) {
        try {
            final List<TestData> testData = Arrays.stream(objectMapper.readValue(testdataFile, TestData[].class))
                                                  .collect(Collectors.toList());
            return testData.stream().map(testData1 -> {
                if (testData1.getAssemblyPartRelationship().getChildParts() == null) {
                    return new TestData(testData1.getCatenaXId(), new AssemblyPartRelationship[] {
                            new AssemblyPartRelationship(testData1.getCatenaXId(), Set.of())
                    });
                } else {
                    return testData1;
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public AssemblyPartRelationship getDummyAssemblyPartRelationshipFromAASDescriptor(List<TestData> testData,
            AssetAdministrationShellDescriptor aasDescriptor) {
        final String catenaXId = aasDescriptor.getIdentification();
        return createDummyAssemblyPartRelationshipForId(testData, catenaXId);
    }

    public AssemblyPartRelationship createDummyAssemblyPartRelationshipForId(List<TestData> testData,
            String catenaXId) {

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

    public AssetAdministrationShellDescriptor createAASShellDescriptorForIdFromTestData(final String catenaXId,
            List<TestData> testData) {
        final AssemblyPartRelationship assemblyPartRelationship = createDummyAssemblyPartRelationshipForId(testData,
                catenaXId);
        return createDummyAssetAdministrationShellDescriptorForId(catenaXId, List.of(assemblyPartRelationship));
    }

    public AssetAdministrationShellDescriptor createDummyAssetAdministrationShellDescriptorForId(final String catenaXId,
            List<AspectModel> endpointAspectModels) {

        final List<SubmodelDescriptor> submodelDescriptors = new ArrayList<>();
        endpointAspectModels.forEach(aspectModel -> submodelDescriptors.add(
                createAssemblyPartRelationshipSubmodelDescriptor(catenaXId + "/aas/assembly-part-relationship",
                        catenaXId)));

        return AssetAdministrationShellDescriptor.builder()
                                                 .description(List.of(new LangString("en", "Test data")))
                                                 .globalAssetId(new Reference(
                                                         List.of("urn:twin:com.test#d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")))
                                                 .idShort("testdata")
                                                 .identification(catenaXId)
                                                 .specificAssetIds(List.of(new IdentifierKeyValuePair(
                                                                 "http://test.com/datamodel/common", "0000000251"),
                                                         new IdentifierKeyValuePair("urn:VR:wt.part.WTPart#",
                                                                 "25054146@nis11c130.epdm-d.edm.dsh.de")))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    private SubmodelDescriptor createAssemblyPartRelationshipSubmodelDescriptor(final String endpointAddress,
            final String catenaXId) {
        return SubmodelDescriptor.builder()
                                 .identification(catenaXId)
                                 .idShort("idShort")
                                 .semanticId(new Reference(
                                         List.of("urn:bamm:com.catenax.assembly_part_relationtship:1.0.0")))
                                 .endpoints(List.of(new Endpoint("https://OEM.connector",
                                         new ProtocolInformation("edc://test.url/shells/" + endpointAddress,
                                                 "AAS/SUBMODEL", "1.0RC02"))))
                                 .build();
    }
}
