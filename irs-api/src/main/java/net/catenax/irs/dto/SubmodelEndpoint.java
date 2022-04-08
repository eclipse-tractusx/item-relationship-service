//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents submodel descriptor endpoint addresses
 */
@Getter
@RequiredArgsConstructor
public class SubmodelEndpoint {

    private final String address;
    private final SubmodelType submodelType;
}
