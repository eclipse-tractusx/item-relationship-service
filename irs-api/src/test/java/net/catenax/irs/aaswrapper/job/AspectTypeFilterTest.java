//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.catenax.irs.component.assetadministrationshell.Reference;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.junit.jupiter.api.Test;

class AspectTypeFilterTest {

    final String assemblyPartRelationshipId = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    final String assemblyPartRelationshipIdWithAspectName = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship";
    final String serialPartTypizationId = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
    final String serialPartTypizationIdWithAspectName = "urn:bamm:com.catenax.serial_part_typization:1.0.0#SerialPartTypization";
    private final AspectTypeFilter aspectTypeFilter = new AspectTypeFilter();

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenEndingWithAspectName() {
        // Arrange
        final Reference semanticId = Reference.builder()
                                              .value(List.of(assemblyPartRelationshipIdWithAspectName))
                                              .build();
        final List<SubmodelDescriptor> submodelDescriptors = List.of(
                SubmodelDescriptor.builder().semanticId(semanticId).build());
        // Act
        final List<SubmodelDescriptor> result = aspectTypeFilter.filterDescriptorsByAssemblyPartRelationship(
                submodelDescriptors);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenNotEndingWithAspectName() {
        // Arrange
        final Reference semanticId = Reference.builder().value(List.of(assemblyPartRelationshipId)).build();
        final List<SubmodelDescriptor> submodelDescriptors = List.of(
                SubmodelDescriptor.builder().semanticId(semanticId).build());
        // Act
        final List<SubmodelDescriptor> result = aspectTypeFilter.filterDescriptorsByAssemblyPartRelationship(
                submodelDescriptors);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipId);
    }

    @Test
    void shouldFilterByAspectTypeWhenEndingWithAspectName() {
        // Arrange
        final Reference semanticId = Reference.builder()
                                              .value(List.of(assemblyPartRelationshipIdWithAspectName))
                                              .build();
        final List<SubmodelDescriptor> submodelDescriptors = List.of(
                SubmodelDescriptor.builder().semanticId(semanticId).build());
        final List<String> aspectTypeFilter = List.of("AssemblyPartRelationship");

        // Act
        final List<SubmodelDescriptor> result = this.aspectTypeFilter.filterDescriptorsByAspectTypes(
                submodelDescriptors, aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAspectTypeWhenNotEndingWithAspectName() {
        // Arrange
        final Reference semanticId = Reference.builder().value(List.of(serialPartTypizationId)).build();
        final List<SubmodelDescriptor> submodelDescriptors = List.of(
                SubmodelDescriptor.builder().semanticId(semanticId).build());
        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = this.aspectTypeFilter.filterDescriptorsByAspectTypes(
                submodelDescriptors, aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationId);
    }

    @Test
    void shouldFilterByAspectTypeWhenWithDifferentAspects() {
        // Arrange
        final Reference semanticIdSerial = Reference.builder()
                                                    .value(List.of(serialPartTypizationIdWithAspectName))
                                                    .build();
        final SubmodelDescriptor assemblyPartRelationship = SubmodelDescriptor.builder()
                                                                              .semanticId(semanticIdSerial)
                                                                              .build();
        final Reference semanticIdAssembly = Reference.builder().value(List.of(assemblyPartRelationshipId)).build();
        final SubmodelDescriptor serialPartTypization = SubmodelDescriptor.builder()
                                                                          .semanticId(semanticIdAssembly)
                                                                          .build();

        final List<SubmodelDescriptor> submodelDescriptors = List.of(assemblyPartRelationship, serialPartTypization);
        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = this.aspectTypeFilter.filterDescriptorsByAspectTypes(
                submodelDescriptors, aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationIdWithAspectName);
    }
}