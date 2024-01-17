/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.semanticshub;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.tractusx.irs.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@ActiveProfiles(profiles = { "test", "local" })
class SemanticHubCacheInitializerTests {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    SemanticsHubCacheInitializer semanticsHubCacheInitializer;

    private Optional<String> getJsonSchemaFromCache() {
        final String defaultUrn = "urn:bamm:io.catenax.serial_part:1.0.0#SerialPart";

        return ofNullable(cacheManager.getCache("schema_cache")).map(
                cache -> cache.get(defaultUrn, String.class));
    }

    @Test
    void shouldFindJsonSchemaInCacheAfterSpringContextStartup() {
        final Optional<String> jsonSchemaFromCache = getJsonSchemaFromCache();

        assertThat(jsonSchemaFromCache).isNotEmpty();
        assertThat(jsonSchemaFromCache.get()).contains("http://json-schema.org/draft-07/schema#");
    }

    @Test
    void shouldFindJsonSchemaInCacheAfterReinitialization() {
        semanticsHubCacheInitializer.reinitializeAllCacheInterval();

        final Optional<String> jsonSchemaFromCache = getJsonSchemaFromCache();

        assertThat(jsonSchemaFromCache).isNotEmpty();
        assertThat(jsonSchemaFromCache.get()).contains("http://json-schema.org/draft-07/schema#");
    }

}