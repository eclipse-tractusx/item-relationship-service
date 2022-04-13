//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

/**
 * Submodel Exception thrown by SubmodelClient
 */
public class SubmodelClientException extends Exception {
    public SubmodelClientException(final String message) {
        super(message);
    }

    public SubmodelClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
