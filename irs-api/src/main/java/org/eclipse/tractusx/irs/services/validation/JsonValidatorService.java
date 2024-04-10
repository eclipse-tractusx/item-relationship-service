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
package org.eclipse.tractusx.irs.services.validation;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.Validator;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.stereotype.Service;

/**
 * Service to validate JSON payloads against JSON schemas.
 */
@Service
@Slf4j
public class JsonValidatorService {
    private final SchemaStore schemaStore = new SchemaStore();
    private final JsonUtil parser;

    /**
     * Creates a new validation service
     *
     * @param parser the parser to use for the JSON
     */
    public JsonValidatorService(final JsonUtil parser) {
        this.parser = parser;
    }

    /**
     * Validate the payload against the schema.
     *
     * @param jsonSchema  the JSON schema
     * @param jsonPayload the JSON payload to validate
     * @return the validation result, containing the validation errors if applicable
     */
    public ValidationResult validate(final String jsonSchema, final String jsonPayload) throws InvalidSchemaException {
        log.debug("Trying to validate JSON ({}) with schema ({})", jsonPayload, jsonSchema);

        final Schema schema = loadSchema(jsonSchema);

        final Validator validator = new Validator();
        try {
            final Object payload = parser.fromString(jsonPayload, Object.class);

            final List<String> errors = new ArrayList<>();
            validator.validate(schema, payload, validationError -> errors.add(validationError.getMessage()));
            return createValidationResult(errors);

        } catch (final IllegalStateException | JsonParseException e) {
            log.warn("Unable to validate JSON payload ({})", jsonPayload, e);
            return ValidationResult.builder()
                                   .valid(false)
                                   .validationError("Illegal JSON policies, cannot be validated")
                                   .build();
        }
    }

    private ValidationResult createValidationResult(final List<String> errors) {
        if (errors.isEmpty()) {
            log.debug("Validation was successful");
            return ValidationResult.builder().valid(true).build();
        } else {
            log.debug("Validation failed with {} errors", errors.size());
            return ValidationResult.builder().valid(false).validationErrors(errors).build();
        }
    }

    private Schema loadSchema(final String jsonSchema) throws InvalidSchemaException {
        try {
            return schemaStore.loadSchemaJson(jsonSchema);
        } catch (final GenerationException e) {
            throw new InvalidSchemaException("Cannot load JSON schema for validation", e);
        }
    }
}
