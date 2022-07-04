//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Submodel with identification of SubmodelDescriptor, aspect type and payload as String
 */
@Getter
@Builder
@Jacksonized
public class Submodel {
    private String identification;
    private String aspectType;
    private String payload;
}
