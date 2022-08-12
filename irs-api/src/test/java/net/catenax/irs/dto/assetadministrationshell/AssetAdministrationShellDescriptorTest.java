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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.ProtocolInformation;
import net.catenax.irs.component.assetadministrationshell.Reference;
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
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipId);
    }

    @Test
    void shouldFilterByAspectTypeWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
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
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(serialPartTypizationId)));
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
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(serialPartTypizationIdWithAspectName),
                        createSubmodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));

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
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptor(serialPartTypizationIdWithAspectName,
                                "testSerialPartTypizationEndpoint"),
                        createSubmodelDescriptor(assemblyPartRelationshipIdWithAspectName,
                                "testAssemblyPartRelationshipEndpoint")));

        // Act
        final List<String> result = shellDescriptor.findAssemblyPartRelationshipEndpointAddresses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("testAssemblyPartRelationshipEndpoint");
    }

    private Endpoint createEndpoint(String endpointAddress) {
        return Endpoint.builder()
                       .protocolInformation(ProtocolInformation.builder().endpointAddress(endpointAddress).build())
                       .build();
    }

    private SubmodelDescriptor createSubmodelDescriptor(final String semanticId, final String endpointAddress) {
        final Reference semanticIdSerial = Reference.builder().value(List.of(semanticId)).build();
        final List<Endpoint> endpointSerial = List.of(createEndpoint(endpointAddress));
        return SubmodelDescriptor.builder().semanticId(semanticIdSerial).endpoints(endpointSerial).build();
    }

    private SubmodelDescriptor createSubmodelDescriptorWithoutEndpoint(final String semanticId) {
        return createSubmodelDescriptor(semanticId, null);
    }

    private AssetAdministrationShellDescriptor createShellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder().submodelDescriptors(submodelDescriptors).build();
    }
}