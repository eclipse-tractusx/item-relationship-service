//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import lombok.Data;

/**
 * IdentifierKeyValuePair
 */
@Data
class IdentifierKeyValuePair {

    /**
     * key
     */
    private String key;
    /**
     * subjectId
     */
    private Reference subjectId;
    /**
     * value
     */
    private String value;
    /**
     * semanticId
     */
    private Reference semanticId;

}
