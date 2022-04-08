//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

/**
 * Exception related to Job problems
 */
public class JobException extends RuntimeException {

    public JobException(final String message) {
        super(message);
    }

    public JobException(final Throwable throwable) {
        super(throwable);
    }
}
