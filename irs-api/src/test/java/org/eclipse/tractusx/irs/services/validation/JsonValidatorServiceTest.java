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
package org.eclipse.tractusx.irs.services.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;

class JsonValidatorServiceTest {

    private final JsonValidatorService testee = new JsonValidatorService(new JsonUtil());

    @Test
    @Disabled
    void shouldValidateAssemblyPartRelationship() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = readFile("/__files/assemblyPartRelationship.json");

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    @Disabled
    void shouldNotValidateEmptyString() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = "";

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    @Disabled
    void shouldNotValidateEmptyJson() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = "{}";

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationErrors()).isNotEmpty();
    }

    @Test
    @Disabled
    void shouldThrowExceptionOnIllegalSchema() throws Exception {
        final String schema = readFile("/json-schema/invalid.json");
        final String payload = "{}";

        assertThatThrownBy(() -> testee.validate(schema, payload)).isInstanceOf(InvalidSchemaException.class);
    }

    private String readFile(final String path) throws IOException, URISyntaxException {
        final URL resource = getClass().getResource(path);
        Objects.requireNonNull(resource);
        return Files.readString(Path.of(resource.toURI()));
    }
}