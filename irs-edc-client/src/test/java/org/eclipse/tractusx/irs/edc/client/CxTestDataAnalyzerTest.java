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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.BATCH_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.MATERIAL_FOR_RECYCLING_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.PART_AS_PLANNED_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.PHYSICAL_DIMENSION_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.PRODUCT_DESCRIPTION_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.SERIAL_PART_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.SINGLE_LEVEL_BOM_AS_BUILT_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.SINGLE_LEVEL_BOM_AS_PLANNED_ASPECT_TYPE;
import static org.eclipse.tractusx.irs.data.CxTestDataContainer.CxTestData.SINGLE_LEVEL_USAGE_AS_BUILT_ASPECT_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.edc.client.relationships.RelationshipAspect;
import org.eclipse.tractusx.irs.testing.containers.LocalTestDataConfigurationAware;
import org.junit.jupiter.api.Test;

@Slf4j
class CxTestDataAnalyzerTest extends LocalTestDataConfigurationAware {

    private final CxTestDataContainer cxTestDataContainer;

    CxTestDataAnalyzerTest() throws IOException {
        cxTestDataContainer = localTestDataConfiguration.cxTestDataContainer();
    }

    @Test
    void parseAndPrintExpectedDataResultsAsBuilt() {
        final TestParameters testParameters = TestParameters.builder()
                                                            .globalAssetId(
                                                                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b")
                                                            .bomLifecycle(BomLifecycle.AS_BUILT)
                                                            .direction(Direction.DOWNWARD)
                                                            .shouldCountSingleLevelBomAsBuilt(Boolean.TRUE)
                                                            .shouldCountSerialPart(Boolean.TRUE)
                                                            .shouldCountBatch(Boolean.TRUE)
                                                            .shouldCountMaterialForRecycling(Boolean.TRUE)
                                                            .shouldCountProductDescription(Boolean.TRUE)
                                                            .shouldCountPhysicalDimension(Boolean.TRUE)
                                                            .build();

        final Long expectedNumberOfRelationships = countExpectedNumberOfRelationshipsFor(testParameters.globalAssetId,
                RelationshipAspect.from(testParameters.bomLifecycle, testParameters.direction));
        final Long expectedNumberOfSubmodels = countExpectedNumberOfSubmodelsFor(testParameters.globalAssetId,
                testParameters);

        log.info("Results for globalAssetId {} and bomLifecycle {} with direction {}", testParameters.globalAssetId,
                testParameters.bomLifecycle, testParameters.direction);
        log.info("Expected number of relationships: " + expectedNumberOfRelationships);
        log.info("Expected number of submodels: " + expectedNumberOfSubmodels);

        assertThat(expectedNumberOfRelationships).isNotNull();
        assertThat(expectedNumberOfSubmodels).isNotNull();
    }

    @Test
    void parseAndPrintExpectedDataResultsAsPlanned() {
        final TestParameters testParameters = TestParameters.builder()
                                                            .globalAssetId(
                                                                    "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c")
                                                            .bomLifecycle(BomLifecycle.AS_PLANNED)
                                                            .direction(Direction.DOWNWARD)
                                                            .shouldCountSingleLevelBomAsPlanned(Boolean.TRUE)
                                                            .shouldCountPartAsPlanned(Boolean.TRUE)
                                                            .build();

        final Long expectedNumberOfRelationships = countExpectedNumberOfRelationshipsFor(testParameters.globalAssetId,
                RelationshipAspect.from(testParameters.bomLifecycle, testParameters.direction));
        final Long expectedNumberOfSubmodels = countExpectedNumberOfSubmodelsFor(testParameters.globalAssetId,
                testParameters);

        log.info("Results for globalAssetId {} and bomLifecycle {} with direction {}", testParameters.globalAssetId,
                testParameters.bomLifecycle, testParameters.direction);
        log.info("Expected number of relationships: " + expectedNumberOfRelationships);
        log.info("Expected number of submodels: " + expectedNumberOfSubmodels);

        assertThat(expectedNumberOfRelationships).isNotNull();
        assertThat(expectedNumberOfSubmodels).isNotNull();
    }

    @Test
    void shouldGetSameNumberOfRelationshipsAndCreateExpectationFile() throws IOException {
        final TestParameters testParameters = TestParameters.builder()
                                                            .globalAssetId(
                                                                    "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b")
                                                            .bomLifecycle(BomLifecycle.AS_BUILT)
                                                            .direction(Direction.DOWNWARD)
                                                            .shouldCreateExpectedRelationshipsFile(Boolean.FALSE)
                                                            .build();
        final RelationshipAspect relationshipAspect = RelationshipAspect.from(testParameters.bomLifecycle,
                testParameters.direction);

        final Long expectedNumberOfRelationships = countExpectedNumberOfRelationshipsFor(testParameters.globalAssetId,
                relationshipAspect);
        final List<Relationship> relationships = getRelationshipFor(testParameters.globalAssetId, relationshipAspect);

        log.info("Results for globalAssetId {} and bomLifecycle {} with direction {}", testParameters.globalAssetId,
                testParameters.bomLifecycle, testParameters.direction);
        log.info("Expected number of relationships: " + expectedNumberOfRelationships);
        log.info("Size of relationships: " + relationships.size());

        assertThat(expectedNumberOfRelationships).isEqualTo(relationships.size());

        if (testParameters.shouldCreateExpectedRelationshipsFile) {
            final Map<String, Object> expectedRelationshipsJson = Map.of("relationships", relationships);
            objectMapper.writeValue(new File("expected-relationships.json"), expectedRelationshipsJson);
            log.info("File with expected relationships was created");
        }
    }

    @Test
    void shouldGetSameNumberOfSubmodelsAndCreateExpectationFile() throws IOException {
        final TestParameters testParameters = TestParameters.builder()
                                                            .globalAssetId(
                                                                    "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c")
                                                            .bomLifecycle(BomLifecycle.AS_PLANNED)
                                                            .direction(Direction.DOWNWARD)
                                                            .shouldCountSingleLevelBomAsPlanned(Boolean.TRUE)
                                                            .shouldCountPartAsPlanned(Boolean.TRUE)
                                                            .shouldCreateExpectedSubmodelsFile(Boolean.FALSE)
                                                            .build();

        final Long expectedNumberOfSubmodels = countExpectedNumberOfSubmodelsFor(testParameters.globalAssetId,
                testParameters);
        final List<Submodel> submodels = getSubmodelsFor(testParameters.globalAssetId, testParameters);

        log.info("Results for globalAssetId {} and bomLifecycle {} with direction {}", testParameters.globalAssetId,
                testParameters.bomLifecycle, testParameters.direction);
        log.info("Expected number of submodels: " + expectedNumberOfSubmodels);
        log.info("Size of submodels: " + submodels.size());

        assertThat(expectedNumberOfSubmodels).isEqualTo(submodels.size());

        if (testParameters.shouldCreateExpectedSubmodelsFile) {
            final Map<String, Object> expectedSubmodelsJson = Map.of("submodels", submodels);
            objectMapper.writeValue(new File("expected-submodels.json"), expectedSubmodelsJson);
            log.info("File with expected submodels was created");
        }
    }

    private Long countExpectedNumberOfRelationshipsFor(final String catenaXId,
            final RelationshipAspect relationshipAspect) {
        final Optional<CxTestDataContainer.CxTestData> cxTestData = cxTestDataContainer.getByCatenaXId(catenaXId);

        Optional<Map<String, Object>> relationshipSubmodelData = Optional.empty();

        if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsBuilt);
        } else if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned);
        }

        if (relationshipSubmodelData.isPresent()) {
            final RelationshipSubmodel relationshipSubmodel = objectMapper.convertValue(relationshipSubmodelData,
                    relationshipAspect.getSubmodelClazz());
            final long countRelationships = relationshipSubmodel.asRelationships().size();
            final AtomicLong counter = new AtomicLong(countRelationships);

            relationshipSubmodel.asRelationships().forEach(relationship -> {
                final String childGlobalAssetId = relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId();
                final long expectedNumberOfChildRelationships = countExpectedNumberOfRelationshipsFor(
                        childGlobalAssetId, relationshipAspect);
                counter.addAndGet(expectedNumberOfChildRelationships);
            });

            return counter.get();
        }

        return 0L;
    }

    private List<Relationship> getRelationshipFor(final String catenaXId, final RelationshipAspect relationshipAspect) {
        final Optional<CxTestDataContainer.CxTestData> cxTestData = cxTestDataContainer.getByCatenaXId(catenaXId);

        Optional<Map<String, Object>> relationshipSubmodelData = Optional.empty();

        if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsBuilt);
        } else if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned);
        }

        final List<Relationship> relationships = new ArrayList<>();

        if (relationshipSubmodelData.isPresent()) {
            final RelationshipSubmodel relationshipSubmodel = objectMapper.convertValue(relationshipSubmodelData,
                    relationshipAspect.getSubmodelClazz());
            relationships.addAll(relationshipSubmodel.asRelationships());

            relationshipSubmodel.asRelationships().forEach(relationship -> {
                final String childGlobalAssetId = relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId();
                final List<Relationship> childRelationships = getRelationshipFor(childGlobalAssetId,
                        relationshipAspect);
                relationships.addAll(childRelationships);
            });
        }

        return relationships;
    }

    private Long countExpectedNumberOfSubmodelsFor(final String catenaXId, final TestParameters testParameters) {
        final Optional<CxTestDataContainer.CxTestData> cxTestData = cxTestDataContainer.getByCatenaXId(catenaXId);
        final RelationshipAspect relationshipAspect = RelationshipAspect.from(testParameters.bomLifecycle,
                testParameters.direction);

        Optional<Map<String, Object>> relationshipSubmodelData = Optional.empty();

        final AtomicLong counter = new AtomicLong();

        if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsBuilt);
            checkAndIncrementCounter(testParameters.shouldCountSingleLevelBomAsBuilt, relationshipSubmodelData,
                    counter);

            checkAndIncrementCounter(testParameters.shouldCountSerialPart,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getSerialPart), counter);
            checkAndIncrementCounter(testParameters.shouldCountSingleLevelUsageAsBuilt,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelUsageAsBuilt), counter);
            checkAndIncrementCounter(testParameters.shouldCountBatch,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getBatch), counter);
            checkAndIncrementCounter(testParameters.shouldCountMaterialForRecycling,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getMaterialForRecycling), counter);
            checkAndIncrementCounter(testParameters.shouldCountProductDescription,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getProductDescription), counter);
            checkAndIncrementCounter(testParameters.shouldCountPhysicalDimension,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getPhysicalDimension), counter);
        } else if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned);
            checkAndIncrementCounter(testParameters.shouldCountSingleLevelBomAsPlanned, relationshipSubmodelData,
                    counter);

            checkAndIncrementCounter(testParameters.shouldCountPartAsPlanned,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getPartAsPlanned), counter);
        }

        if (relationshipSubmodelData.isPresent()) {
            final RelationshipSubmodel relationshipSubmodel = objectMapper.convertValue(relationshipSubmodelData,
                    relationshipAspect.getSubmodelClazz());
            relationshipSubmodel.asRelationships().forEach(relationship -> {
                final String childGlobalAssetId = relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId();
                final long expectedNumberOfChildSubmodels = countExpectedNumberOfSubmodelsFor(childGlobalAssetId,
                        testParameters);
                counter.addAndGet(expectedNumberOfChildSubmodels);
            });
        }
        return counter.get();
    }

    private List<Submodel> getSubmodelsFor(final String catenaXId, final TestParameters testParameters) {
        final Optional<CxTestDataContainer.CxTestData> cxTestData = cxTestDataContainer.getByCatenaXId(catenaXId);
        final RelationshipAspect relationshipAspect = RelationshipAspect.from(testParameters.bomLifecycle,
                testParameters.direction);

        Optional<Map<String, Object>> relationshipSubmodelData = Optional.empty();

        final List<Submodel> submodels = new ArrayList<>();

        if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsBuilt);
            checkAndAddSubmodel(testParameters.shouldCountSingleLevelBomAsBuilt, relationshipSubmodelData, submodels,
                    SINGLE_LEVEL_BOM_AS_BUILT_ASPECT_TYPE);

            checkAndAddSubmodel(testParameters.shouldCountSerialPart,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getSerialPart), submodels,
                    SERIAL_PART_ASPECT_TYPE);
            checkAndAddSubmodel(testParameters.shouldCountSingleLevelUsageAsBuilt,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned), submodels,
                    SINGLE_LEVEL_USAGE_AS_BUILT_ASPECT_TYPE);
            checkAndAddSubmodel(testParameters.shouldCountBatch,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getBatch), submodels, BATCH_ASPECT_TYPE);
            checkAndAddSubmodel(testParameters.shouldCountMaterialForRecycling,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getMaterialForRecycling), submodels,
                    MATERIAL_FOR_RECYCLING_ASPECT_TYPE);
            checkAndAddSubmodel(testParameters.shouldCountProductDescription,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getProductDescription), submodels,
                    PRODUCT_DESCRIPTION_ASPECT_TYPE);
            checkAndAddSubmodel(testParameters.shouldCountPhysicalDimension,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getPhysicalDimension), submodels,
                    PHYSICAL_DIMENSION_ASPECT_TYPE);
        } else if (relationshipAspect.equals(RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED)) {
            relationshipSubmodelData = cxTestData.flatMap(CxTestDataContainer.CxTestData::getSingleLevelBomAsPlanned);
            checkAndAddSubmodel(testParameters.shouldCountSingleLevelBomAsPlanned, relationshipSubmodelData, submodels,
                    SINGLE_LEVEL_BOM_AS_PLANNED_ASPECT_TYPE);

            checkAndAddSubmodel(testParameters.shouldCountPartAsPlanned,
                    cxTestData.flatMap(CxTestDataContainer.CxTestData::getPartAsPlanned), submodels,
                    PART_AS_PLANNED_ASPECT_TYPE);
        }

        if (relationshipSubmodelData.isPresent()) {
            final RelationshipSubmodel relationshipSubmodel = objectMapper.convertValue(relationshipSubmodelData,
                    relationshipAspect.getSubmodelClazz());
            relationshipSubmodel.asRelationships().forEach(relationship -> {
                final String childGlobalAssetId = relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId();
                final List<Submodel> childSubmodels = getSubmodelsFor(childGlobalAssetId, testParameters);
                submodels.addAll(childSubmodels);
            });
        }

        return submodels;
    }

    private void checkAndIncrementCounter(final boolean shouldCountSubmodel,
            final Optional<Map<String, Object>> submodel, final AtomicLong counter) {
        if (shouldCountSubmodel && submodel.isPresent()) {
            counter.incrementAndGet();
        }
    }

    private void checkAndAddSubmodel(final boolean shouldCountSubmodel, final Optional<Map<String, Object>> payload,
            final List<Submodel> submodels, final String aspectType) {
        if (shouldCountSubmodel && payload.isPresent()) {
            submodels.add(Submodel.from(UUID.randomUUID().toString(), aspectType, UUID.randomUUID().toString(), payload.get()));
        }
    }

    @Builder
    private static class TestParameters {
        final String globalAssetId;
        final BomLifecycle bomLifecycle;
        final Direction direction;

        final boolean shouldCountSingleLevelBomAsBuilt;
        final boolean shouldCountSerialPart;
        final boolean shouldCountBatch;
        final boolean shouldCountMaterialForRecycling;
        final boolean shouldCountProductDescription;
        final boolean shouldCountPhysicalDimension;
        final boolean shouldCountSingleLevelBomAsPlanned;
        final boolean shouldCountSingleLevelUsageAsBuilt;
        final boolean shouldCountPartAsPlanned;

        final boolean shouldCreateExpectedRelationshipsFile;
        final boolean shouldCreateExpectedSubmodelsFile;
    }

}