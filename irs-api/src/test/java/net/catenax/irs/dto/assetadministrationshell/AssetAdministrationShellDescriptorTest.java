//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dto.assetadministrationshell;

import static net.catenax.irs.util.TestMother.shellDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellDescriptorTest {

    final String assemblyPartRelationshipId = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    final String assemblyPartRelationshipIdWithAspectName = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship";
    final String serialPartTypizationId = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
    final String serialPartTypizationIdWithAspectName = "urn:bamm:com.catenax.serial_part_typization:1.0.0#SerialPartTypization";

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipId);
    }

    @Test
    void shouldFilterByAspectTypeWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
        final List<String> aspectTypeFilter = List.of("AssemblyPartRelationship");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAspectTypeWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(serialPartTypizationId)));
        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationId);
    }

    @Test
    void shouldFilterByAspectTypeWhenWithDifferentAspects() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(serialPartTypizationIdWithAspectName),
                        submodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));

        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationIdWithAspectName);
    }

    @Test
    void shouldReturnEndpointAddressesForSubmodelDescriptors() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptor(serialPartTypizationIdWithAspectName,
                                "testSerialPartTypizationEndpoint"),
                        submodelDescriptor(assemblyPartRelationshipIdWithAspectName,
                                "testAssemblyPartRelationshipEndpoint")));

        // Act
        final List<String> result = shellDescriptor.findAssemblyPartRelationshipEndpointAddresses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("testAssemblyPartRelationshipEndpoint");
    }


}