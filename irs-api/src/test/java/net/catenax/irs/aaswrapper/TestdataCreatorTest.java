package net.catenax.irs.aaswrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestdataCreatorTest {
    private TestdataCreator testdataCreator;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        testdataCreator = new TestdataCreator(objectMapper);
    }

    @Test
    void testMethod() {
        final File file = new File("src/test/resources/AASShelDescriptorTestData.json");
        final List<TestData> testData = testdataCreator.getTestData(file);
        // number of entries in AASShelDescriptorTestData.json
        assertEquals(842, testData.size());
    }

    @Test
    void createDummyAssemblyPartRelationshipForIdTest() {
        final File file = new File("src/test/resources/AASShelDescriptorTestData.json");
        final List<TestData> testData = testdataCreator.getTestData(file);

        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = testdataCreator.createDummyAssemblyPartRelationshipForId(
                testData, "8a61c8db-561e-4db0-84ec-a693fc5ffdf6");

        assertEquals("8a61c8db-561e-4db0-84ec-a693fc5ffdf6", dummyAssemblyPartRelationshipForId.getCatenaXId());
    }

    @Test
    void createDummyAssetAdministrationShellDescriptorForId() {
        final File file = new File("src/test/resources/AASShelDescriptorTestData.json");
        final List<TestData> testData = testdataCreator.getTestData(file);
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
        assertEquals("edc://offer-trace-assembly-part-relationship/shells/"
                        + assemblyPartRelationship.getCatenaXId() + "/aas/assembly-part-relationship",
                endpointAddress);

        final AssemblyPartRelationship assemblyPartRelationshipFromAASDescriptor = testdataCreator.getDummyAssemblyPartRelationshipFromAASDescriptor(
                testData, aasDescriptor);

        assertEquals(assemblyPartRelationshipFromAASDescriptor, assemblyPartRelationship);
        System.out.println(aasDescriptor);
    }
}