package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class NegotiationResponse {

    private String id;
    private String contractAgreementId;
    private String counterPartyAddress;
    private String errorDetail;
    private String protocol;
    private String state;
    private String type;

}
