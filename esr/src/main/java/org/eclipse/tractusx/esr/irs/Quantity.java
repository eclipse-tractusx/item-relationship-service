package org.eclipse.tractusx.esr.irs;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Quantity {
    BigDecimal quantityNumber;
    MeasurementUnit measurementUnit;
}
