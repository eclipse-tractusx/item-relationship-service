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

import java.util.List;
import java.util.Set;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.springframework.stereotype.Service;

/**
 * Service to validate JSON payloads against JSON schemas.
 */
@Service
@Slf4j
public class JsonValidatorService {
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    /**
     * Validate the payload against the schema.
     *
     * @param jsonSchema  the JSON schema
     * @param jsonPayload the JSON payload to validate
     * @return the validation result, containing the validation errors if applicable
     */
    public ValidationResult validate(final String jsonSchema, final String jsonPayload) throws InvalidSchemaException {
        log.trace("Trying to validate JSON ({}) with schema ({})", jsonPayload, jsonSchema);

        final JsonSchema schemaFromString = loadSchema(jsonSchema);

        try {
            final Set<ValidationMessage> errors = schemaFromString.validate(jsonPayload, InputFormat.JSON);
            return createValidationResult(errors.stream().map(ValidationMessage::getMessage).toList());

        } catch (final IllegalStateException | JsonParseException e) {
            log.warn("Unable to validate JSON payload ({})", jsonPayload, e);
            return ValidationResult.builder()
                                   .valid(false)
                                   .validationError("Illegal JSON payload, cannot be validated")
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

    private JsonSchema loadSchema(final String jsonSchema) throws InvalidSchemaException {
        try {
            return factory.getSchema(jsonSchema);
        } catch (final JsonSchemaException e) {
            throw new InvalidSchemaException("Cannot load JSON schema for validation", e);
        }
    }
}
