/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.registryclient.central;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.testing.containers.LocalTestDataConfigurationAware;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellTestdataCreatorTest extends LocalTestDataConfigurationAware {

    private final AssetAdministrationShellTestdataCreator assetAdministrationShellTestdataCreator;

    AssetAdministrationShellTestdataCreatorTest() throws IOException {
        super();

        assetAdministrationShellTestdataCreator = new AssetAdministrationShellTestdataCreator(localTestDataConfiguration.cxTestDataContainer());
    }

    @Test
    void shouldReturnAssetAdministrationShellDescriptorWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:a65c35a8-8d31-4a86-899b-57912de33675";

        final AssetAdministrationShellDescriptor aasDescriptor = assetAdministrationShellTestdataCreator.createDummyAssetAdministrationShellDescriptorForId(
                catenaXId);

        final String endpointAddress = aasDescriptor.getSubmodelDescriptors()
                                                    .get(0)
                                                    .getEndpoints()
                                                    .get(0)
                                                    .getProtocolInformation()
                                                    .getHref();

        assertThat(aasDescriptor.getSubmodelDescriptors()).isNotEmpty();
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoints()).isNotNull();
        assertThat(endpointAddress).isEqualTo("singleLevelBomAsBuilt");
        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getEndpoints().get(0).getProtocolInformation().getSubprotocolBody()).contains(catenaXId);

        assertThat(aasDescriptor.getSubmodelDescriptors().get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo("urn:bamm:io.catenax.single_level_bom_as_built:1.0.0");
        assertThat(aasDescriptor.getSubmodelDescriptors().get(1).getSemanticId().getKeys().get(0).getValue()).isEqualTo("urn:bamm:io.catenax.serial_part:1.0.0");
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
        assertThat(aasDescriptor.getId()).isNotNull();
        assertThat(aasDescriptor.getSpecificAssetIds()).isNotNull();
        assertThat(aasDescriptor.getSubmodelDescriptors()).isNotNull();
    }
}