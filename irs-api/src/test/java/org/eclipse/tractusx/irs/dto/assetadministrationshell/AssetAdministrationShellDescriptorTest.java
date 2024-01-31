/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.dto.assetadministrationshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutHref;

import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellDescriptorTest {

    final String singleLevelBomAsBuiltId = "urn:bamm:com.catenax.single_level_bom_as_built:1.0.0";
    final String singleLevelBomAsBuiltIdWithAspectName = "urn:bamm:com.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt";
    final String serialPartId = "urn:bamm:com.catenax.serial_part:1.0.0";
    final String serialPartIdWithAspectName = "urn:bamm:com.catenax.serial_part:1.0.0#SerialPart";

    @Test
    void shouldFilterByAspectTypeWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutHref(singleLevelBomAsBuiltIdWithAspectName)));
        final List<String> aspectTypeFilter = List.of("SingleLevelBomAsBuilt");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo(singleLevelBomAsBuiltIdWithAspectName);
    }

    @Test
    void shouldFilterByAspectTypeWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutHref(serialPartId)));
        final List<String> aspectTypeFilter = List.of("SerialPart");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo(serialPartId);
    }

    @Test
    void shouldFilterByAspectTypeWhenWithDifferentAspects() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutHref(serialPartIdWithAspectName),
                        submodelDescriptorWithoutHref(singleLevelBomAsBuiltId)));

        final List<String> aspectTypeFilter = List.of("SerialPart");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo(serialPartIdWithAspectName);
    }

    @Test
    void shouldFilterByAspectTypeForUrnFormat() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutHref(serialPartIdWithAspectName),
                        submodelDescriptorWithoutHref(singleLevelBomAsBuiltIdWithAspectName)));

        final List<String> aspectTypeFilter = List.of(serialPartIdWithAspectName,
                singleLevelBomAsBuiltIdWithAspectName);

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo(serialPartIdWithAspectName);
        assertThat(result.get(1).getSemanticId().getKeys().get(0).getValue()).isEqualTo(singleLevelBomAsBuiltIdWithAspectName);
    }

}