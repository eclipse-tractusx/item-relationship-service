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
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.AspectType;

/**
 * AssetAdministrationShellDescriptor
 */
@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Slf4j
@Schema(description = "AAS shells.")
public class AssetAdministrationShellDescriptor {

    /**
     * administration
     */
    private AdministrativeInformation administration;
    /**
     * description
     */
    @ArraySchema(maxItems = Integer.MAX_VALUE)
    private List<LangString> description;
    /**
     * globalAssetId
     */
    @Schema(description = "Id of global asset.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
            implementation = String.class)
    private String globalAssetId;
    /**
     * idShort
     */
    @Schema(implementation = String.class, example = "future concept x")
    private String idShort;
    /**
     * id
     */
    @SuppressWarnings("PMD.ShortVariable")
    @Schema(implementation = String.class, example = "882fc530-b69b-4707-95f6-5dbc5e9baaa8")
    private String id;
    /**
     * specificAssetIds
     */
    @ArraySchema(maxItems = Integer.MAX_VALUE)
    private List<IdentifierKeyValuePair> specificAssetIds;
    /**
     * submodelDescriptors
     */
    @ArraySchema(maxItems = Integer.MAX_VALUE)
    private List<SubmodelDescriptor> submodelDescriptors;

    /**
     * @return ManufacturerId value from Specific Asset Ids
     */
    public Optional<String> findManufacturerId() {
        return this.specificAssetIds.stream()
                                    .filter(assetId -> "ManufacturerId".equalsIgnoreCase(assetId.getName()))
                                    .map(IdentifierKeyValuePair::getValue)
                                    .findFirst();
    }

    /**
     * @param aspectTypes the aspect types which should be filtered by
     * @return AssetAdministrationShellDescriptor with filtered submodel descriptors
     */
    public AssetAdministrationShellDescriptor withFilteredSubmodelDescriptors(final List<String> aspectTypes) {
        this.setSubmodelDescriptors(this.filterDescriptorsByAspectTypes(aspectTypes));
        return this;
    }

    /**
     * @param relationshipAspect filter for aspect type
     * @return The filtered list of submodel addresses
     */
    public List<Endpoint> findRelationshipEndpointAddresses(final AspectType relationshipAspect) {
        final List<SubmodelDescriptor> filteredSubmodelDescriptors = filterDescriptorsByAspectTypes(
                List.of(relationshipAspect.toString()));
        return filteredSubmodelDescriptors.stream()
                                          .map(SubmodelDescriptor::getEndpoints)
                                          .flatMap(Collection::stream)
                                          .toList();
    }

    /**
     * @param aspectTypes The AspectTypes for which should be filtered
     * @return The filtered list containing only SubmodelDescriptors which are provided as AspectTypes
     */
    public List<SubmodelDescriptor> filterDescriptorsByAspectTypes(final List<String> aspectTypes) {
        log.info("Filtering for Aspect Types '{}'", aspectTypes);
        return this.submodelDescriptors.stream()
                                       .filter(submodelDescriptor -> aspectTypes.stream()
                                                                                .anyMatch(type -> isMatching(
                                                                                        submodelDescriptor, type)))

                                       .toList();
    }

    private boolean isMatching(final SubmodelDescriptor submodelDescriptor, final String aspectTypeFilter) {
        final Optional<String> submodelAspectType = Optional.ofNullable(submodelDescriptor.getSemanticId().getKeys())
                                                            .flatMap(key -> key.stream().findFirst())
                                                            .map(SemanticId::getValue);
        return submodelAspectType.map(
                semanticId -> semanticId.endsWith("#" + aspectTypeFilter) || contains(semanticId, aspectTypeFilter)
                        || semanticId.equals(aspectTypeFilter)).orElse(false);
    }

    private boolean contains(final String semanticId, final String aspectTypeFilter) {
        // https://stackoverflow.com/a/3752693
        final String[] split = aspectTypeFilter.split("(?=\\p{Lu})");
        final String join = String.join("_", split).toLowerCase(Locale.ROOT);
        log.debug("lower case aspect: '{}'", join);
        return semanticId.contains(join);
    }

    private boolean notContainsSingleLevelBomAsBuilt(final List<String> filterAspectTypes) {
        return !filterAspectTypes.contains(AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString());
    }
}
