package org.eclipse.tractusx.esr.irs;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LinkedItem {

    String childCatenaXId;
    ZonedDateTime assembledOn;
    ZonedDateTime lastModifiedOn;
    String lifecycleContext;
    Quantity quantity;

}
