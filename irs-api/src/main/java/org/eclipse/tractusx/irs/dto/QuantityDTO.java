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

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * QuantityDTO model used for internal application use
 */
@Data
@Builder
@Jacksonized
public class QuantityDTO {

    private Double quantityNumber;

    private MeasurementUnitDTO measurementUnit;

    /**
     * MeasurementUnitDTO
     */
    @Data
    @Builder
    @Jacksonized
    public static class MeasurementUnitDTO {
        private String lexicalValue;
        private String datatypeURI;
    }
}
