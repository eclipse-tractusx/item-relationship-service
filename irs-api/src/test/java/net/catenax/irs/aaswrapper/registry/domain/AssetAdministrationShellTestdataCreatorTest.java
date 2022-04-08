package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellTestdataCreatorTest {
    private AssetAdministrationShellTestdataCreator assetAdministrationShellTestdataCreator;

    @BeforeEach
    void setUp() {
        assetAdministrationShellTestdataCreator = new AssetAdministrationShellTestdataCreator();
    }

    @Test
    void shouldReturnAssetAdministrationShellDescriptorWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssetAdministrationShellDescriptor aasDescriptor = assetAdministrationShellTestdataCreator.createDummyAssetAdministrationShellDescriptorForId(
                catenaXId);

        assertThat(aasDescriptor.getSubmodelDescriptors()).hasSize(1);
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoint()).isNotNull();

        final String endpointAddress = aasDescriptor.getSubmodelDescriptors()
                                                    .get(0)
                                                    .getEndpoint()
                                                    .getProtocolInformation()
                                                    .getEndpointAddress();
        assertThat(endpointAddress).isEqualTo(catenaXId);
    }

}