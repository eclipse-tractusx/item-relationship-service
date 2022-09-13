package org.eclipse.tractusx.esr.irs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class IrsRequest {
    String globalAssetId;
    String bomLifecycle;
}
