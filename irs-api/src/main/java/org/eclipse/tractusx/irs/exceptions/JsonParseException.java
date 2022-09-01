//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.exceptions;

/**
 * Exception when parsing JSON
 */
public class JsonParseException extends RuntimeException {
    public JsonParseException(final Throwable cause) {
        super(cause);
    }
}
