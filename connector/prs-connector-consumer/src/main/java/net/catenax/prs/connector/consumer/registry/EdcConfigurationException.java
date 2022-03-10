//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.registry;

import org.eclipse.dataspaceconnector.spi.EdcException;

/**
 * Exception thrown in case of invalid configuration.
 */
public final class EdcConfigurationException extends EdcException {
    /**
     * Generate a new instance of a {@link EdcConfigurationException}
     *
     * @param message Exception message.
     */
    public EdcConfigurationException(final String message) {
        super(message);
    }
}
