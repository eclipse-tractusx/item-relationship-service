/*
 * Copyright (c) 2022. Copyright Holder (Catena-X Consortium)
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * See the LICENSE file(s) distributed with this work for
 * additional information regarding license terms.
 *
 */
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
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

        assertThat(aasDescriptor.getSubmodelDescriptors()).hasSize(2);
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoints()).isNotNull();
        assertThat(endpointAddress).isEqualTo(catenaXId);

        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getSemanticId().getValue().get(0)).isEqualTo("urn:bamm:com.catenax.assembly_part_relationship:1.0.0");
        assertThat(aasDescriptor.getSubmodelDescriptors().get(1).getSemanticId().getValue().get(0)).isEqualTo("urn:bamm:com.catenax.serial_part_typization:1.0.0");
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