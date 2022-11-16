package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class TransferProcessResponse {

    private String id;
    private String type;
    private String state;
    private String errorDetail;

}
