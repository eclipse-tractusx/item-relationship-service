//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

/**
 * Constant class for the IRS API
 */
public final class IrsApiConstants {

    public static final String GLOBAL_ASSET_ID_REGEX = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static final int UUID_SIZE = 36;
    public static final int URN_PREFIX_SIZE = 9;
    public static final int GLOBAL_ASSET_ID_SIZE = URN_PREFIX_SIZE + UUID_SIZE;
    public static final int JOB_ID_SIZE = UUID_SIZE;
}
