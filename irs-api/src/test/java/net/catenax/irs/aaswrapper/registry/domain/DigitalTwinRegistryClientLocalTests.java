package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.catenax.irs.aaswrapper.AASWrapperClient;
import net.catenax.irs.aaswrapper.AASWrapperClientLocalStub;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClientLocalStub;
import net.catenax.irs.aspectmodels.AspectModel;
import net.catenax.irs.aspectmodels.AspectModelTypes;
import net.catenax.irs.aspectmodels.assemblypartrelationship.AssemblyPartRelationship;
import net.catenax.irs.aspectmodels.serialparttypization.SerialPartTypization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DigitalTwinRegistryClientLocalTests {

    private AASWrapperClient aasWrapperClientLocalStub;

    @BeforeEach
    void setUp() {

        aasWrapperClientLocalStub = new AASWrapperClientLocalStub(new DigitalTwinRegistryClientLocalStub(),
                new SubmodelClientLocalStub());
    }

    @Test
    void testStubResponseForAssetAdministrationShellDescriptor() {
        var input = aasWrapperClientLocalStub.getAssetAdministrationShellDescriptor("assetIdentifier");

        assertThat(input.getIdentification()).isEqualTo("assetIdentifier");
        assertThat(input.getIdShort()).isEqualTo("brake_dt_2019_snr.asm");
    }

    @Test
    void testStubResponseForAssemblyPartRelationship() {
        String catenaXId = "testCatenaXId";
        final AspectModel test = aasWrapperClientLocalStub.getSubmodel(
                "edc://offer-trace-assembly-part-relationship/shells/" + catenaXId + "/aas/assembly-part-relationship",
                AspectModelTypes.ASSEMBLY_PART_RELATIONSHIP);

        assertThat(test).isInstanceOf(AssemblyPartRelationship.class);
        final AssemblyPartRelationship assemblyPartRelationship = test instanceof AssemblyPartRelationship ?
                (AssemblyPartRelationship) test :
                null;

        assertThat(assemblyPartRelationship).isNotNull();
        assertThat(assemblyPartRelationship.getCatenaXId()).isEqualTo(catenaXId);
    }

    @Test
    void testStubResponseForSerialPartTypization() {
        final AspectModel test = aasWrapperClientLocalStub.getSubmodel("catenaXIdSerialPartTypization",
                AspectModelTypes.SERIAL_PART_TYPIZATION);

        assertThat(test).isInstanceOf(SerialPartTypization.class);
        final SerialPartTypization serialPartTypization = test instanceof SerialPartTypization ?
                (SerialPartTypization) test :
                null;

        assertThat(serialPartTypization).isNotNull();
        assertThat(serialPartTypization.getCatenaXId()).isEqualTo("catenaXIdSerialPartTypization");
    }
}
