//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.controllers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constant class for the IRS API
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IrsAppConstants {

    public static final int UUID_SIZE = 36;
    public static final int URN_PREFIX_SIZE = 9;
    public static final int GLOBAL_ASSET_ID_SIZE = URN_PREFIX_SIZE + UUID_SIZE;
    public static final int JOB_ID_SIZE = UUID_SIZE;

    public static final String INVALID_ARGUMENTS = "Invalid Arguments.";

    public static final String JOB_EXECUTION_FAILED = "Handler of job method failed";

}
