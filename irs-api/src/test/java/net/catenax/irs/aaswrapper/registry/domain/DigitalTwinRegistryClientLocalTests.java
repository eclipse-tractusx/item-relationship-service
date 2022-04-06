package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DigitalTwinRegistryClientLocalTests {

    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    @BeforeEach
    void setUp() {
       digitalTwinRegistryFacade = new DigitalTwinRegistryFacade(new DigitalTwinRegistryClientLocalStub());
    }

    @Test
    void testStubResponseForAssetAdministrationShellDescriptor() {
        var input = digitalTwinRegistryFacade.getAASSubmodelEndpointAddresses("assetIdentifier");

        assertThat(input).isNotNull();
//        assertThat(input.size()).isEqualTo(1);
    }

//    @Test
//    void testStubResponseForAssemblyPartRelationship() {
//        String catenaXId = "testCatenaXId";
//        final AspectModel test = aasWrapperClientLocalStub.getSubmodel(
//                "edc://offer-trace-assembly-part-relationship/shells/" + catenaXId + "/aas/assembly-part-relationship",
//                AssemblyPartRelationship.class);
//
//        assertThat(test).isInstanceOf(AssemblyPartRelationship.class);
//        final AssemblyPartRelationship assemblyPartRelationship = test instanceof AssemblyPartRelationship ?
//                (AssemblyPartRelationship) test :
//                null;
//
//        assertThat(assemblyPartRelationship).isNotNull();
//        assertThat(assemblyPartRelationship.getCatenaXId()).isEqualTo(catenaXId);
//    }
//
//    @Test
//    void testStubResponseForSerialPartTypization() {
//        final AspectModel test = aasWrapperClientLocalStub.getSubmodel("catenaXIdSerialPartTypization",
//                SerialPartTypization.class);
//
//        assertThat(test).isInstanceOf(SerialPartTypization.class);
//        final SerialPartTypization serialPartTypization = test instanceof SerialPartTypization ?
//                (SerialPartTypization) test :
//                null;
//
//        assertThat(serialPartTypization).isNotNull();
//        assertThat(serialPartTypization.getCatenaXId()).isEqualTo("catenaXIdSerialPartTypization");
//    }
}
