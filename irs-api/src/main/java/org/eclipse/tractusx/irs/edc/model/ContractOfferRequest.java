package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.dataspaceconnector.policy.model.Policy;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ContractOfferRequest {

    private String offerId;
    private String assetId;
    private Policy policy;
}
