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
package org.eclipse.tractusx.irs.semanticshub;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

/**
 * WireMock configurations and requests used for testing Semantic Hub fLow.
 */
public final class SemanticHubWireMockSupport {
    public static final String BATCH_URN = "urn:samm:io.catenax.batch:2.0.0%23Batch";
    private static final String SINGLE_LEVEL_BOM_AS_BUILT_URN = "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0%23SingleLevelBomAsBuilt";
    public static final String SCHEMA_PATH_PLACEHOLDER = "/models/%s/json-schema";
    public static final String SEMANTIC_HUB_SCHEMA_URL =
            "http://semantic.hub" + SCHEMA_PATH_PLACEHOLDER.formatted("{urn}");
    public static final String MODELS_PATH = "/models";

    private SemanticHubWireMockSupport() {
    }

    public static void semanticHubWillReturnBatchSchema() {
        schemaResponse200(SCHEMA_PATH_PLACEHOLDER.formatted(BATCH_URN), "semantichub/batch-2.0.0-schema.json");
    }

    public static void semanticHubWillReturnSingleLevelBomAsBuiltSchema() {
        schemaResponse200(SCHEMA_PATH_PLACEHOLDER.formatted(SINGLE_LEVEL_BOM_AS_BUILT_URN),
                "semantichub/singleLevelBomAsBuilt-2.0.0-schema.json");
    }

    private static void schemaResponse200(final String urlRegex, final String fileName) {
        stubFor(get(urlPathMatching(urlRegex)).withHost(equalTo("semantic.hub"))
                                              .willReturn(responseWithStatus(200).withBodyFile(fileName)));
    }

    static void semanticHubWillReturnPagedModels(final int page, final int pageSize, final String fileName) {
        stubFor(get(urlPathEqualTo(MODELS_PATH)).withHost(equalTo("semantic.hub"))
                                                .withQueryParam("page", equalTo(String.valueOf(page)))
                                                .withQueryParam("pageSize", equalTo(String.valueOf(pageSize)))
                                                .willReturn(responseWithStatus(200).withBodyFile(fileName)));
    }

    public static void semanticHubWillReturnAllModels(final String fileName) {
        stubFor(get(urlPathEqualTo(MODELS_PATH)).withHost(equalTo("semantic.hub"))
                                                .willReturn(responseWithStatus(200).withBodyFile(fileName)));
    }

    static void semanticHubWillThrowErrorForSemanticModel(final String semanticModel) {
        final String url = SCHEMA_PATH_PLACEHOLDER.formatted(semanticModel);
        stubFor(get(urlPathEqualTo(url)).willReturn(responseWithStatus(500)));
    }

    static void verifySemanticHubWasCalledForModel(final String model, final int times) {
        verify(exactly(times), getRequestedFor(urlPathMatching(SCHEMA_PATH_PLACEHOLDER.formatted(model))));
    }

    static void verifySemanticHubWasCalledForAllModels(final int times) {
        verify(exactly(times), getRequestedFor(urlPathEqualTo(MODELS_PATH)));
    }
}