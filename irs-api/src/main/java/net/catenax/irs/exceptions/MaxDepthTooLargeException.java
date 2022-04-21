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

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.configuration.IrsConfiguration;

/**
 * Exception thrown by the service when the {@link net.catenax.irs.component.RegisterJob#getDepth()}
 * parameter is larger than {@link IrsConfiguration#getPartsTreeMaxDepth()}.
 */
@ExcludeFromCodeCoverageGeneratedReport
public class MaxDepthTooLargeException extends RuntimeException {
    /**
     * Generate a new instance of a {@link MaxDepthTooLargeException}
     *
     * @param message Exception message.
     */
    public MaxDepthTooLargeException(final String message) {
        super(message);
    }
}
