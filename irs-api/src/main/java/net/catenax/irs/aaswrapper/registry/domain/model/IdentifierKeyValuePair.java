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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * IdentifierKeyValuePair
 */
@Data
@AllArgsConstructor
public class IdentifierKeyValuePair extends HasSemantics {

    /**
     * key
     */
    private String key;
    /**
     * subjectId
     */
//    private Reference subjectId;
    /**
     * value
     */
    private String value;

}
