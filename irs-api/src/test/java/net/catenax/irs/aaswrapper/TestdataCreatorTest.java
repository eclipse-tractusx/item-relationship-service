package net.catenax.irs.aaswrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelDescriptor;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.assemblypartrelationship.ChildData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestdataCreatorTest {
    private TestdataCreator testdataCreator;
    private List<TestData> testData;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        testdataCreator = new TestdataCreator(objectMapper);

        final File file = new File("src/test/resources/AASShelDescriptorTestData.json");
        testData = testdataCreator.getTestData(file);
    }

    @Test
    void testGetTestData() {
        final File file = new File("src/test/resources/AASShelDescriptorTestData.json");
        List<TestData> testData = testdataCreator.getTestData(file);
        // number of entries in AASShelDescriptorTestData.json
        assertEquals(842, testData.size());
    }

    @Test
    void testCreateDummyAssemblyPartRelationshipForIdTest() {

        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = testdataCreator.createDummyAssemblyPartRelationshipForId(
                testData, "8a61c8db-561e-4db0-84ec-a693fc5ffdf6");

        assertEquals("8a61c8db-561e-4db0-84ec-a693fc5ffdf6", dummyAssemblyPartRelationshipForId.getCatenaXId());
    }

    @Test
    void testCreateDummyAssetAdministrationShellDescriptorForId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final AssemblyPartRelationship assemblyPartRelationship = testdataCreator.createDummyAssemblyPartRelationshipForId(
                testData, catenaXId);
        final AssetAdministrationShellDescriptor aasDescriptor = testdataCreator.createDummyAssetAdministrationShellDescriptorForId(
                catenaXId, List.of(assemblyPartRelationship));

        assertEquals(1, aasDescriptor.getSubmodelDescriptors().size());
        assertEquals(1, aasDescriptor.getSubmodelDescriptors().get(0).getEndpoints().size());

        final String endpointAddress = aasDescriptor.getSubmodelDescriptors()
                                                    .get(0)
                                                    .getEndpoints()
                                                    .get(0)
                                                    .getProtocolInformation()
                                                    .getEndpointAddress();
        assertEquals(
                "edc://test.url/shells/" + assemblyPartRelationship.getCatenaXId() + "/aas/assembly-part-relationship",
                endpointAddress);

        final AssemblyPartRelationship assemblyPartRelationshipFromAASDescriptor = testdataCreator.getDummyAssemblyPartRelationshipFromAASDescriptor(
                testData, aasDescriptor);

        assertEquals(assemblyPartRelationshipFromAASDescriptor, assemblyPartRelationship);
    }

    @Test
    void testGetChildPartsForCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final AssetAdministrationShellDescriptor aasDescriptor = testdataCreator.createAASShellDescriptorForIdFromTestData(
                catenaXId, testData);

        final Optional<SubmodelDescriptor> submodelDescriptor = aasDescriptor.getSubmodelDescriptors()
                                                                             .stream()
                                                                             .findFirst();
        assertThat(submodelDescriptor).isPresent();
        final List<Endpoint> endpoints = submodelDescriptor.get().getEndpoints();
        assertThat(endpoints).hasSize(1);
        final Optional<Endpoint> endpoint = endpoints.stream().findFirst();
        assertThat(endpoint).isPresent();
        final String endpointAddress = endpoint.get().getProtocolInformation().getEndpointAddress();
        assertThat(endpointAddress).isNotEmpty();

        final AssemblyPartRelationship assemblyPartRelationship = testdataCreator.getDummyAssemblyPartRelationshipFromAASDescriptor(
                testData, aasDescriptor);
        final Set<ChildData> childParts = assemblyPartRelationship.getChildParts();
        assertThat(childParts).hasSize(3);
        final List<String> childIDs = List.of("5ce49656-5156-4c8a-b93e-19422a49c0bc",
                "09b48bcc-8993-4379-a14d-a7740e1c61d4", "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
        childParts.forEach(childData -> assertThat(childIDs).containsAnyOf(childData.getChildCatenaXId()));
    }
}