/********************************************************************************
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
package org.eclipse.tractusx.irs.semanticshub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

/**
 * WireMock configurations and requests used for testing Semantic Hub fLow.
 */
public final class SemanticHubWireMockConfig {
    public static final String BATCH_URN = "urn:samm:io.catenax.batch:2.0.0%23Batch";
    public static final String SEMANTIC_HUB_SCHEMA_URL = "http://semantic.hub/models/{urn}/json-schema";
    private static final String SINGLE_LEVEL_BOM_AS_BUILT_URN = "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0%23SingleLevelBomAsBuilt";

    private SemanticHubWireMockConfig() {
    }

    public static MappingBuilder batchSchemaResponse200() {
        return schemaResponse200("/models/" + BATCH_URN + "/json-schema", "semantichub/batch-2.0.0-schema.json");
    }

    public static MappingBuilder singleLevelBomAsBuiltSchemaResponse200() {
        return schemaResponse200("/models/" + SINGLE_LEVEL_BOM_AS_BUILT_URN + "/json-schema",
                "semantichub/singleLevelBomAsBuilt-2.0.0-schema.json");
    }

    private static MappingBuilder schemaResponse200(final String urlRegex, final String fileName) {
        return get(urlPathMatching(urlRegex)).withHost(equalTo("semantic.hub"))
                                             .willReturn(responseWithStatus(200).withBodyFile(fileName));
    }
}