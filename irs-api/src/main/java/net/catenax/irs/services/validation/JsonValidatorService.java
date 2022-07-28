//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services.validation;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.exceptions.JsonParseException;
import net.catenax.irs.util.JsonUtil;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import net.jimblackler.jsonschemafriend.Validator;
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

    private Schema loadSchema(final String jsonSchema) throws InvalidSchemaException {
        try {
            return schemaStore.loadSchemaJson(jsonSchema);
        } catch (final GenerationException e) {
            throw new InvalidSchemaException("Cannot load JSON schema for validation", e);
        }
    }
}
