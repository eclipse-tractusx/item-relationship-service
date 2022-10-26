package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class NegotiationRequest {

    private String connectorId;
    private String connectorAddress;
    private ContractOfferRequest offer;


}
