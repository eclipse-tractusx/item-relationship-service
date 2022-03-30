package net.catenax.irs.aaswrapper.registry.domain;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.catenax.irs.aaswrapper.TestData;
import net.catenax.irs.aaswrapper.TestdataCreator;
import net.catenax.irs.aaswrapper.registry.domain.model.AssetAdministrationShellDescriptor;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Profile("local")
@Service
@ExcludeFromCodeCoverageGeneratedReport
public class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {

        final TestdataCreator testdataCreator = new TestdataCreator(
                new ObjectMapper().registerModule(new Jdk8Module()));
        final List<TestData> testData = testdataCreator.getTestData(
                new File("src/main/resources/Testdata/AASShelDescriptorTestData.json"));
        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = testdataCreator.createDummyAssemblyPartRelationshipForId(
                testData, aasIdentifier);

        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier,
                List.of(dummyAssemblyPartRelationshipForId));
    }
}
