package net.catenax.irs.aspectmodels.assemblypartrelationship;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class AssemblyPartRelationshipTest {

    @Test
    void createAssemblyPartRelationshipTest() {
        final String catenaXId = "catenaXId";
        final String catenaXChildId = "catenaXChildId";
        final Quantity quantity = new Quantity(1.0, new MeasurementUnit("dataTypeUri", "l"));
        final LifecycleContextCharacteristic asbuilt = LifecycleContextCharacteristic.ASBUILT;
        final ChildData childData = new ChildData(null, quantity, null, asbuilt, null, catenaXChildId);

        final Set<ChildData> childDataSet = Set.of(childData);
        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship(catenaXId, childDataSet);

        assertThat(assemblyPartRelationship.getCatenaXId()).isEqualTo(catenaXId);
        assertThat(assemblyPartRelationship.getChildParts()).hasSize(1);

        final ChildData childDataOfObject = assemblyPartRelationship.getChildParts().stream().findFirst().get();
        assertThat(childDataOfObject.getChildCatenaXId()).isEqualTo(catenaXChildId);
        assertThat(childDataOfObject.getLifecycleContext()).isEqualTo(asbuilt);
        assertThat(childDataOfObject.getQuantity().getQuantityNumber()).isEqualTo(1.0);
        assertThat(childDataOfObject.getQuantity().getMeasurementUnit().getDatatypeURI()).isEqualTo("dataTypeUri");
        assertThat(childDataOfObject.getQuantity().getMeasurementUnit().getLexicalValue()).isEqualTo("l");
        assertThat(childDataOfObject.getAssembledOn()).isNull();
        assertThat(childDataOfObject.getCreatedOn()).isNull();
        assertThat(childDataOfObject.getLastModifiedOn()).isNull();
    }
}