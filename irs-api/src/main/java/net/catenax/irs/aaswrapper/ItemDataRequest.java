//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import lombok.Value;
import net.catenax.irs.connector.job.DataRequest;

/**
 * Data Request for CatenaX IDs
 */
@Value
public class ItemDataRequest implements DataRequest {

    private final String itemId;

}
