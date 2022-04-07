//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
//

package net.catenax.irs.aaswrapper.dto;

import lombok.Builder;
import lombok.Data;

/**
 * ChildDataDTO model used for internal application use
 */
@Data
@Builder
public class ChildDataDTO {
    /**
     * lifecycleContext
     */
    private LifecycleContextCharacteristic lifecycleContext;

    /**
     * childCatenaXId
     */
    private String childCatenaXId;
}
