package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.catenax.irs.aaswrapper.submodel.domain.SubmodelTestdataCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistryAssetAdministrationShellTestdataCreatorTest {
    private AssetAdministrationShellTestdataCreator assetAdministrationShellTestdataCreator;

    @BeforeEach
    void setUp() {
        SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();
        assetAdministrationShellTestdataCreator = new AssetAdministrationShellTestdataCreator(submodelTestdataCreator);
    }

    @Test
    void testCreateDummyAssetAdministrationShellDescriptorForId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssetAdministrationShellDescriptor aasDescriptor = assetAdministrationShellTestdataCreator.createAASShellDescriptorForIdFromTestData(
                catenaXId);

        assertThat(aasDescriptor.getSubmodelDescriptors()).hasSize(1);
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoint()).isNotNull();

        final String endpointAddress = aasDescriptor.getSubmodelDescriptors()
                                                    .get(0)
                                                    .getEndpoint()
                                                    .getProtocolInformation()
                                                    .getEndpointAddress();
        // assertThat("edc://test.url/shells/" + aasDescriptor.getIdentification() + "/aas/assembly-part-relationship").isEqualTo(endpointAddress);
        assertThat(aasDescriptor.getIdentification()).isEqualTo(endpointAddress);
    }

}