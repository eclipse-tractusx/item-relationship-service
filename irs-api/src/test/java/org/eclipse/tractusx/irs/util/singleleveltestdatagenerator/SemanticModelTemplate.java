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
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.WiremockSupport;
import org.springframework.data.util.Pair;
import wiremock.com.fasterxml.jackson.databind.JsonNode;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;
import wiremock.com.fasterxml.jackson.databind.node.ArrayNode;
import wiremock.com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@code SemanticModelTemplate} represents a JSON object that contains data used in the runtime generation of a
 * hierarchy of single-level test data by {@link SingleLevelTestDataGenerator}. The object must have the fields of the
 * following example:
 * <pre>{@code
 * {
 *   "SingleLevelBomAsBuilt": {
 *     "3.0.0": {
 *       "mainItem": {
 *         "catenaXId": "",
 *         "childItems": []
 *       },
 *       "relationshipItem": {
 *         "catenaXId": "",
 *         "quantity": {
 *           "value": 0.2014,
 *           "unit": "unit:kilogram"
 *         },
 *         "hasAlternatives": true,
 *         "businessPartner": "BPNL00000000TEST",
 *         "createdOn": "2022-02-03T14:48:54.709Z",
 *         "lastModifiedOn": "2022-02-03T14:48:54.709Z"
 *       },
 *       "mainItemIdKey": "catenaXId",
 *       "aspectName": "urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt",
 *       "relationshipsArrayKey": "childItems",
 *       "relationshipItemIdKey": "catenaXId"
 *     }
 *   }
 * }}</pre>
 * <p>The {@code "mainItem"} is the part of a single-level asset which contains at least the global asset id while the
 * {@code "relationshipItem"} is an element of an array of relationships, given the semantic model specifies that {@code "mainItem"}
 * is a relationship aspect.</p>
 * The class provides methods which copy {@code "mainItem"} or {@code "relationshipItem"} and populate the fields of the
 * copy with appropriate data.
 *
 * @see SingleLevelTestDataGenerator
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class SemanticModelTemplate {
    ObjectMapper mapper = new ObjectMapper();
    /**
     * Represents the JSON file containing the data needed for test data generation.
     */
    File templatesFile;
    String modelName;
    String modelVersion;
    String aspectName;
    JsonNode mainItem;
    JsonNode relationshipItem;
    /**
     * Name of the key that holds the global asset id in the main item.
     */
    String mainItemIdKey;
    /**
     * Name of the key that holds the global asset id of an element in the main item's
     * array of relationships.
     */
    String relationshipItemIdKey;
    /**
     * Name of the key that holds the array of relationships in the main item.
     */
    String relationshipsArrayKey;
    /**
     * Additional semantic models to generate test data for during a specific generation run.
     */
    Set<SemanticModelTemplate> additionalTemplates;

    public SemanticModelTemplate(final File templatesFile, final String modelName, final String modelVersion,
            final Set<Pair<String, String>> additionalModelNamesAndVersions) throws InstantiationException {
        this.templatesFile = templatesFile;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        try {
            JsonNode template = parseTemplateNode(modelName, modelVersion);
            validateTemplateHasAllFields(template, modelName, modelVersion);

            aspectName = template.get("aspectName").asText();
            mainItem = template.get("mainItem");
            mainItemIdKey = template.get("mainItemIdKey").asText();
            relationshipItem = template.get("relationshipItem");
            relationshipItemIdKey = template.get("relationshipItemIdKey").asText();
            relationshipsArrayKey = template.get("relationshipsArrayKey").asText();
            additionalTemplates = templatesFromModelsAndVersions(additionalModelNamesAndVersions);
        } catch (IOException e) {
            throw new InstantiationException(
                    "Failed to instantiate SemanticModelTemplate for model %s %s due to exception: %s".formatted(
                            modelName, modelVersion, e));
        }
    }

    /**
     * Instantiates a set of additional SemanticModelTemplates indicated by a set of pairs of model names and versions.
     *
     * @param additionalModelNamesAndVersions Set of pairs in which every pair holds the model name and the model version
     * @return A set of type {@code SemanticModelTemplate}
     */
    private Set<SemanticModelTemplate> templatesFromModelsAndVersions(
            final Set<Pair<String, String>> additionalModelNamesAndVersions) {
        final Set<SemanticModelTemplate> templates = new HashSet<>();
        additionalModelNamesAndVersions.forEach(modelNameAndVersion -> {
            final SemanticModelTemplate additionalTemplate;
            try {
                additionalTemplate = new SemanticModelTemplate(templatesFile, modelNameAndVersion.getFirst(),
                        modelNameAndVersion.getSecond(), Set.of());
                templates.add(additionalTemplate);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        return templates;
    }

    /**
     * Using the JSON file holding the template JSON objects, return a {@code JsonNode} instance
     * of the template object specified by a model's name and its version.
     *
     * @param modelName    Name of the semantic model template object to parse
     * @param modelVersion Version of the semantic model to parse
     * @return A {@code JsonNode} of the template JSON object.
     * @throws IOException            when parsing fails
     * @throws NoSuchElementException when no template with the specified name and version is found in the file
     */
    private JsonNode parseTemplateNode(final String modelName, final String modelVersion) throws IOException {
        log.info("Trying to parse template for model {} {}", modelName, modelVersion);
        final JsonNode template = mapper.readTree(templatesFile).get(modelName).get(modelVersion);
        if (template == null) {
            throw new NoSuchElementException(
                    "Failed to find a JSON template object for model %s %s in file %s".formatted(modelName,
                            modelVersion, templatesFile));
        }
        return template;
    }

    private void validateTemplateHasAllFields(JsonNode template, String modelName, String modelVersion) {
        if (!template.has("aspectName") || !template.has("mainItem") || !template.has("mainItemIdKey") || !template.has(
                "relationshipItem") || !template.has("relationshipItemIdKey") || !template.has(
                "relationshipsArrayKey")) {
            throw new NoSuchElementException(
                    "The template for model %s %s in file %s does not contain all required fields.".formatted(modelName,
                            modelVersion, templatesFile)
                            + " Required fields are: mainItem, mainItemIdKey, aspectName, relationshipItem, "
                            + "relationshipsArrayKey, relationshipItemIdKey.");
        }
    }

    /**
     * Generate a semantic model's main item. The main item is the part of the model which almost always has
     * an array of relationship items, depending on whether the semantic model is a relationship aspect.
     * The main item's id will be the passed uuid.
     *
     * @param uuid UUID string for the item
     * @return A JsonNode with the modifications described above.
     */
    public JsonNode generateMainItem(final String uuid) {
        final JsonNode newMainItem = mainItem.deepCopy();

        // set main item id
        ((ObjectNode) newMainItem).put(mainItemIdKey, uuid);

        return newMainItem;
    }

    /**
     * Generate an array of relationship items for a semantic model. A relationship item's id will be a
     * randomly generated uuid.
     *
     * @param numberOfRelationships How many relationship items to generate.
     * @return An {@link ArrayNode} that contains the generated relationship items.
     */
    public ArrayNode generateRelationshipItems(final int numberOfRelationships) {
        final ArrayNode relationshipItems = mapper.createArrayNode();

        for (int i = 0; i < numberOfRelationships; i++) {
            final JsonNode newRelationshipItem = relationshipItem.deepCopy();
            // set relationship item id
            ((ObjectNode) newRelationshipItem).put(relationshipItemIdKey, WiremockSupport.randomUUIDwithPrefix());
            relationshipItems.add(newRelationshipItem);
        }

        return relationshipItems;
    }

}
