//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.exceptions;

/**
 * General entity not found exception.
 */
public class EntityCancelException extends RuntimeException {
    /**
     * Generate a new instance of a {@link EntityCancelException}
     *
     * @param message Exception message.
     */
    public EntityCancelException(final String message) {
        super(message);
    }
}
