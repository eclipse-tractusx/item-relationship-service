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
import net.catenax.irs.controllers.ApiErrorsConstants;

/**
 * Exception to indicate an unsupported aspect.
 */
@ExcludeFromCodeCoverageGeneratedReport
public class AspectNotSupportedException extends RuntimeException {

    public AspectNotSupportedException() {
        super(ApiErrorsConstants.ASPECT_NOT_SUPPORTED);
    }

    public AspectNotSupportedException(final String message) {
        super(message);
    }
}
