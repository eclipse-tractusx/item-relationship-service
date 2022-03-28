//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import lombok.Data;

/**
 * Quantity
 */
@Data
class Quantity {

    /**
     * quantityNumber
     */
    private Double quantityNumber;

    /**
     * measurementUnit
     */
    private MeasurementUnit measurementUnit;

    @Data
    static class MeasurementUnit {
        private String lexicalValue;
    }
}
