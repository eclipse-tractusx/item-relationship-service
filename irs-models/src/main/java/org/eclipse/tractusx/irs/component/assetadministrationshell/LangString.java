//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * LangString
 */
@Data
@Builder
@Jacksonized
public class LangString {

    /**
     * language
     */
    private String language;
    /**
     * text
     */
    private String text;

}
