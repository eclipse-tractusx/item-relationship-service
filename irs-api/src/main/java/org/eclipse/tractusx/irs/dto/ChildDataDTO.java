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
package org.eclipse.tractusx.irs.dto;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ChildDataDTO model used for internal application use
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class ChildDataDTO {

    private ZonedDateTime assembledOn;

    private QuantityDTO quantity;

    private ZonedDateTime lastModifiedOn;

    private String lifecycleContext;

    private String childCatenaXId;

}
