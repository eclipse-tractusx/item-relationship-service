package net.catenax.irs.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.aaswrapper.AASWrapperClientLocalStub;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryClientLocalStub;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClientLocalStub;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.assemblypartrelationship.ChildData;
import org.junit.jupiter.api.Test;

class ItemTreeQueryServiceTest {

    @Test
    void getItemTree() {
        final ItemTreeQueryService itemTreeQueryService = new ItemTreeQueryService(new AASWrapperClientLocalStub(new DigitalTwinRegistryClientLocalStub(), new SubmodelClientLocalStub()));
        final String test = itemTreeQueryService.getItemTree("test");
        assertThat(test).isEqualTo("Job started.");
        final List<AspectModel> aspectModels = itemTreeQueryService.getAspectModels();
        assertThat(aspectModels.get(0)).isNotNull();
        assertThat(aspectModels.get(0)).isInstanceOf(AssemblyPartRelationship.class);
        AssemblyPartRelationship assemblyPartRelationship = (AssemblyPartRelationship) aspectModels.get(0);
        assertThat(assemblyPartRelationship.getCatenaXId()).isEqualTo("catenaXIdAssemblyPartRelationship");
    }

    @Test
    void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        AssemblyPartRelationship test = null;
        try {
            test = objectMapper.readValue(new File("src/test/resources/assemblyPartRelationshipTestData.json"),
                    AssemblyPartRelationship.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThat(test).isNotNull();
        assertThat(test.getCatenaXId()).isEqualTo("09b48bcc-8993-4379-a14d-a7740e1c61d4");
        assertThat(test.getChildParts()).hasSize(3);

        final List<ChildData> childData = new ArrayList<>(test.getChildParts());
        final List<String> childIds = List.of("8c1407e8-a911-4236-ac0a-03c48e6cbf19",
                "c35ee875-5443-4a2d-bc14-fdacd64b9446", "ea724f73-cb93-4b7b-b92f-d97280ff888b");

        assertThat(childIds).containsAnyOf(childData.get(0).getChildCatenaXId(), childData.get(1).getChildCatenaXId(),
                childData.get(2).getChildCatenaXId());
    }
}