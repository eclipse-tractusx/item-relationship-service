package org.eclipse.tractusx.esr.irs;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class IrsRelationship {

    String catenaXId;
    LinkedItem linkedItem;
    String aspectType;

}
