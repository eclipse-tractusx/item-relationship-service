//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.extension;


import net.catenax.prs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

/**
 * Utilities for extension initialization.
 */
@ExcludeFromCodeCoverageGeneratedReport
public final class ExtensionUtils {
    private ExtensionUtils() {
    }

    static /* package */ EdcException fatal(final ServiceExtensionContext context, final String message, final Throwable cause) {
        context.getMonitor().severe(message, cause);
        return new EdcException(message, cause);
    }
}
