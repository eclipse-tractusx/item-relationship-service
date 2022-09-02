//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Quantity
 */
@Data
@Jacksonized
class Quantity {

    private Double quantityNumber;
    private MeasurementUnit measurementUnit;

    /**
     * MeasurementUnit
     */
    @Data
    @Jacksonized
    /* package */ static class MeasurementUnit {
        private String lexicalValue;
        private String datatypeURI;
    }
}
