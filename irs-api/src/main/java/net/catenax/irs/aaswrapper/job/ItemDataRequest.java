//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import lombok.Value;
import net.catenax.irs.connector.job.DataRequest;

/**
 * Data Request for CatenaX IDs
 */
@Value
public class ItemDataRequest implements DataRequest {

    private final String itemId;
    private final Integer depth;

    public static ItemDataRequest rootNode(final String itemId) {
        return new ItemDataRequest(itemId, 0);
    }

    public static ItemDataRequest nextDepthNode(final String itemId, final Integer currentDepth) {
        return new ItemDataRequest(itemId, currentDepth + 1);
    }
}
