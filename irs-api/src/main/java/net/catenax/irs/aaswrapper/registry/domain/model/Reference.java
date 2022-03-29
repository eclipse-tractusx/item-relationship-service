//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Reference
 */
@Data
@AllArgsConstructor
public class Reference {

    /**
     * value
     */
    private List<String> value;

}
