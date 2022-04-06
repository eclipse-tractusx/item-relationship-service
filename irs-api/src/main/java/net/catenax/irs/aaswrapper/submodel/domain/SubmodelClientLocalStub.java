//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import net.catenax.irs.aaswrapper.TestData;
import net.catenax.irs.aaswrapper.TestdataCreator;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
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
public class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public AspectModel getSubmodel(final String endpointPath, final Class<? extends AspectModel> aspectModelClass) {
        final TestdataCreator testdataCreator = new TestdataCreator(
                new ObjectMapper().registerModule(new Jdk8Module()));
        final List<TestData> testData = testdataCreator.getTestData(
                new File("src/main/resources/Testdata/AASShelDescriptorTestData.json"));

        if (aspectModelClass.equals(AssemblyPartRelationship.class)) {
            return getAssemblyPartRelationship(endpointPath, testdataCreator, testData);
        } else if (aspectModelClass.equals(SerialPartTypization.class)) {
            return new SerialPartTypization("catenaXIdSerialPartTypization", Set.of(new KeyValueList("key", "value")),
                    new ManufacturingEntity(null, Optional.of("de")),
                    new PartTypeInformationEntity("manufacturerPartId", Optional.of("customerPartId"),
                            "nameAtManufacturer", Optional.of("nameAtCustomer"), ClassificationCharacteristic.PRODUCT));
        }
        return null;
    }

    private net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship getAssemblyPartRelationship(
            final String endpointPath, final TestdataCreator testdataCreator, final List<TestData> testData) {
        // Extract the catenaXId from the endpointPath.
        // only applicable for the test data in "src/main/resources/Testdata/AASShelDescriptorTestData.json"
        String catenaXId = endpointPath.replaceFirst("edc://offer-trace-assembly-part-relationship/shells/", "");
        catenaXId = catenaXId.replaceFirst("/aas/assembly-part-relationship", "");

        return testdataCreator.createDummyAssemblyPartRelationshipForId(testData, catenaXId);
    }
}
