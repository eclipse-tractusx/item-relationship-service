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

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * Result object of the JSON validation process.
 * Contains validation status and errors.
 */
@Value
@Builder
public class ValidationResult {

    boolean valid;

    @Singular
    List<String> validationErrors;
}
