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

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.component.enums.AspectType;
import org.jetbrains.annotations.NotNull;

/**
 * Class with methods to filter a list of SubmodelDescriptors by a list of AspectTypes
 */
@Slf4j
public class AspectTypeFilter {

    /**
     * @param aasSubmodelEndpoints A list of SubmodelEndpoints which needs to be filtered by
     *                             AssemblyPartRelationship
     * @return The filtered list containing only SubmodelDescriptors which are AssemblyPartRelationship
     */
    @NotNull
    public List<SubmodelDescriptor> filterDescriptorsByAssemblyPartRelationship(
            final List<SubmodelDescriptor> aasSubmodelEndpoints) {
        return filterDescriptorsByAspectTypes(aasSubmodelEndpoints,
                List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()));
    }

    /**
     * @param aasSubmodelEndpoints A list of SubmodelEndpoints which needs to be filtered by AspectTypes
     * @param aspectTypes          The AspectTypes for which should be filtered
     * @return The filtered list containing only SubmodelDescriptors which are provided as AspectTypes
     */
    @NotNull
    public List<SubmodelDescriptor> filterDescriptorsByAspectTypes(final List<SubmodelDescriptor> aasSubmodelEndpoints,
            final List<String> aspectTypes) {
        log.info("Filtering for Aspect Types '{}'", aspectTypes);
        return aasSubmodelEndpoints.stream()
                                   .filter(submodelDescriptor -> aspectTypes.stream()
                                                                            .anyMatch(type -> isMatching(
                                                                                    submodelDescriptor, type)))

                                   .collect(Collectors.toList());
    }

    private boolean isMatching(final SubmodelDescriptor submodelDescriptor, final String aspectTypeFilter) {
        final Optional<String> submodelAspectType = submodelDescriptor.getSemanticId().getValue().stream().findFirst();
        return submodelAspectType.map(
                                         semanticId -> semanticId.endsWith("#" + aspectTypeFilter) || contains(semanticId, aspectTypeFilter))
                                 .orElse(false);
    }

    private boolean contains(final String semanticId, final String aspectTypeFilter) {
        // https://stackoverflow.com/a/3752693
        final String[] split = aspectTypeFilter.split("(?=\\p{Lu})");
        final String join = String.join("_", split).toLowerCase(Locale.ROOT);
        log.debug("lower case aspect: '{}'", join);
        return semanticId.contains(join);
    }
}
