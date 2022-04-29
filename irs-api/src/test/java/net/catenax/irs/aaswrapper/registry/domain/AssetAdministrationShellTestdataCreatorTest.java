package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellTestdataCreatorTest {

    private final AssetAdministrationShellTestdataCreator assetAdministrationShellTestdataCreator = new AssetAdministrationShellTestdataCreator();

    @Test
    void shouldReturnAssetAdministrationShellDescriptorWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssetAdministrationShellDescriptor aasDescriptor = assetAdministrationShellTestdataCreator.createDummyAssetAdministrationShellDescriptorForId(
                catenaXId);

        final String endpointAddress = aasDescriptor.getSubmodelDescriptors()
                                                    .get(0)
                                                    .getEndpoints()
                                                    .get(0)
                                                    .getProtocolInformation()
                                                    .getEndpointAddress();

        assertThat(aasDescriptor.getSubmodelDescriptors()).hasSize(1);
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoints()).isNotNull();
        assertThat(endpointAddress).isEqualTo(catenaXId);
    }

    @Test
    void shouldReturnCompleteShellDescriptorWhenProvidedWithJson() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();

        final BufferedReader bufferedReader = Files.newBufferedReader(
                Path.of("src/test/resources/AASShellTestdata.json"));

        final AssetAdministrationShellDescriptor aasDescriptor = objectMapper.readValue(bufferedReader,
                AssetAdministrationShellDescriptor.class);

        assertThat(aasDescriptor.getDescription()).isNotNull();
        assertThat(aasDescriptor.getGlobalAssetId()).isNotNull();
        assertThat(aasDescriptor.getIdShort()).isNotNull();
        assertThat(aasDescriptor.getIdentification()).isNotNull();
        assertThat(aasDescriptor.getSpecificAssetIds()).isNotNull();
        assertThat(aasDescriptor.getSubmodelDescriptors()).isNotNull();
    }
}