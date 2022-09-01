//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package org.eclipse.tractusx.irs.persistence;

/**
 * Exception for everything related to BlobPersistence actions
 */
public class BlobPersistenceException extends Exception {

    public BlobPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
