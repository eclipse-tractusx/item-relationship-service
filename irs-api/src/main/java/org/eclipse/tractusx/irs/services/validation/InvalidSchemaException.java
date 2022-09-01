//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.services.validation;

/**
 * Exception that describes invalid JSON schemas that cannot be loaded.
 */
public class InvalidSchemaException extends Exception {

    /**
     * Create a new InvalidSchemaException
     * @param message the error message
     * @param cause the root cause
     */
    public InvalidSchemaException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
