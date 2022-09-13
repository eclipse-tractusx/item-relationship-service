package org.eclipse.tractusx.esr.irs;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MeasurementUnit {
    String datatypeURI;
    String lexicalValue;
}
