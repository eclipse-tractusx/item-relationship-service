/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.util.singleleveltestdatagenerator;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.WiremockSupport;
import org.springframework.data.util.Pair;
import wiremock.com.fasterxml.jackson.databind.JsonNode;
import wiremock.com.fasterxml.jackson.databind.node.ArrayNode;
import wiremock.com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is a utility intended for use during IRS integration tests with Wiremock.
 * It makes use of the {@link SemanticModelTemplate} class to generate a hierarchy of test data in
 * a potentially recursive execution flow.
 *
 * @see SemanticModelTemplate
 */
@Slf4j
public abstract class SingleLevelTestDataGenerator {
    private static void validateArguments(final int numberOfRelationships, final int maxDepth) {
        if (numberOfRelationships < 1 || maxDepth < 1) {
            throw new IllegalArgumentException(
                    "Test data generation aborted: the number of relationships to generate and the recursion depth to satisfy"
                            + " must be greater than 0");
        }
    }

    /**
     * Sets up recursive test data generation for a specific semantic model.
     *
     * @param templatesFilePath               Path to a file containing semantic model template JSON objects.
     * @param modelName                       Name of the model for which to generate test data
     * @param modelVersion                    Version of the model for which to generate test data
     * @param additionalModelNamesAndVersions Set of pairs of additional semantic models and their versions
     *                                        to associate with the {@code SemanticModelTemplate} created during
     *                                        test data generation.
     * @param numberOfRelationships           How many relationships each asset should have. Must be > 0.
     * @param maxDepth                        Recursion depth that the test data should satisfy.
     * @return A {@code Pair} which contains a {@code SemanticModelTemplate} and a {@code Map} of global asset ids that are
     * associated with the assets they correspond to
     */
    public static Pair<SemanticModelTemplate, Map<String, JsonNode>> generateDataForTemplate(
            final String templatesFilePath, final String modelName, final String modelVersion,
            final Set<Pair<String, String>> additionalModelNamesAndVersions, final int numberOfRelationships,
            final int maxDepth) {

        validateArguments(numberOfRelationships, maxDepth);

        try {
            final File templatesFile = new File(templatesFilePath);

            final SemanticModelTemplate template = new SemanticModelTemplate(templatesFile, modelName, modelVersion,
                    additionalModelNamesAndVersions);

            log.info("Generating test data for model {} {}...", modelName, modelVersion);
            final Map<String, JsonNode> generatedItems = doGenerate(template, WiremockSupport.randomUUIDwithPrefix(),
                    numberOfRelationships, 0, maxDepth, new LinkedHashMap<>());

            return Pair.of(template, generatedItems);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For a given {@code SemanticModelTemplate} and a set of global asset ids, for every global asset id,
     * generate exactly the main item. The {@code SemanticModelTemplate} should not be a template of a model
     * which has any relationships.
     *
     * @param additionalTemplate The {@code SemanticModelTemplate} to use to generate a main item.
     * @param uuids              Set of uuids for which to generate a main item.
     * @return A {@code Map} which has all the global asset ids associated to the assets they correspond to
     */
    public static Map<String, JsonNode> generateDataForAdditionalTemplate(
            final SemanticModelTemplate additionalTemplate, final Set<String> uuids) {
        Map<String, JsonNode> additionallyGeneratedItems = new LinkedHashMap<>();
        log.info("Generating additional test data for model {} {}...", additionalTemplate.getModelName(),
                additionalTemplate.getModelVersion());
        uuids.forEach(uuid -> doGenerate(additionalTemplate, uuid, 0, 0, 0, additionallyGeneratedItems));

        return additionallyGeneratedItems;
    }

    private static Map<String, JsonNode> doGenerate(final SemanticModelTemplate template, final String uuid,
            final int numberOfRelationships, final int depth, final int maxDepth,
            final Map<String, JsonNode> generatedItems) {
        final boolean stopRecursion = depth > maxDepth || numberOfRelationships < 1;
        final int effectiveNumOfRelationships = stopRecursion ? 0 : numberOfRelationships;

        final JsonNode generated = generateMainItemWithRelationships(template, uuid, effectiveNumOfRelationships);

        generatedItems.put(uuid, generated);

        if (!stopRecursion) {
            recurseOnRelationshipItems(template, numberOfRelationships, depth, maxDepth, generatedItems, generated);
        }

        return generatedItems;
    }

    private static void recurseOnRelationshipItems(final SemanticModelTemplate template,
            final int numberOfRelationships, final int depth, final int maxDepth,
            final Map<String, JsonNode> generatedItems, final JsonNode generated) {
        final ArrayNode relationshipsArray = (ArrayNode) generated.get(template.getRelationshipsArrayKey());

        relationshipsArray.forEach(relationshipItem -> doGenerate(template,
                relationshipItem.get(template.getRelationshipItemIdKey()).asText(), numberOfRelationships, depth + 1,
                maxDepth, generatedItems));
    }

    private static JsonNode generateMainItemWithRelationships(final SemanticModelTemplate template, final String uuid,
            final int numberOfRelationships) {
        JsonNode newMainItem = template.generateMainItem(uuid);

        if (numberOfRelationships > 0) {
            final ArrayNode relationshipItems = template.generateRelationshipItems(numberOfRelationships);
            ((ObjectNode) newMainItem).putArray(template.getRelationshipsArrayKey()).addAll(relationshipItems);
        }

        return newMainItem;
    }

}
