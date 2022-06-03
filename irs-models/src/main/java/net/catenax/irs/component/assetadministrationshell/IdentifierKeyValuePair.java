//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.assetadministrationshell;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * IdentifierKeyValuePair
 */
@Data
@Builder
@Jacksonized
public class IdentifierKeyValuePair {

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
