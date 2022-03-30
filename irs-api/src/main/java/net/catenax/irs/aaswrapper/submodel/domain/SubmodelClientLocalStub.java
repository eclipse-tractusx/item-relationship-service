package net.catenax.irs.aaswrapper.submodel.domain;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.catenax.irs.aaswrapper.TestData;
import net.catenax.irs.aaswrapper.TestdataCreator;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.serialparttypization.ClassificationCharacteristic;
import net.catenax.irs.aspectmodels.serialparttypization.KeyValueList;
import net.catenax.irs.aspectmodels.serialparttypization.ManufacturingEntity;
import net.catenax.irs.aspectmodels.serialparttypization.PartTypeInformationEntity;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Profile("local")
@Service
@ExcludeFromCodeCoverageGeneratedReport
public class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public AspectModel getSubmodel(final String endpointPath, final AspectModelTypes aspectModel) {
        final TestdataCreator testdataCreator = new TestdataCreator(
                new ObjectMapper().registerModule(new Jdk8Module()));
        final List<TestData> testData = testdataCreator.getTestData(
                new File("src/main/resources/Testdata/AASShelDescriptorTestData.json"));

        switch (aspectModel) {
        case ASSEMBLY_PART_RELATIONSHIP: {
            // Extract the catenaXId from the endpointPath.
            // This is only applicable for the test data in "src/main/resources/Testdata/AASShelDescriptorTestData.json"
            String catenaXId = endpointPath.replaceFirst("edc://offer-trace-assembly-part-relationship/shells/", "");
            catenaXId = catenaXId.replaceFirst("/aas/assembly-part-relationship", "");

            return testdataCreator.createDummyAssemblyPartRelationshipForId(testData, catenaXId);
        }
        case SERIAL_PART_TYPIZATION: {
            return new SerialPartTypization("catenaXIdSerialPartTypization", Set.of(new KeyValueList("key", "value")),
                    new ManufacturingEntity(null, Optional.of("de")),
                    new PartTypeInformationEntity("manufacturerPartId", Optional.of("customerPartId"),
                            "nameAtManufacturer", Optional.of("nameAtCustomer"), ClassificationCharacteristic.PRODUCT));
        }
        default:
            return null;
        }
    }
}
