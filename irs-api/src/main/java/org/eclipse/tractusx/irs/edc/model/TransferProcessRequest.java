package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class TransferProcessRequest {

    private String id;
    private String connectorId;
    private String connectorAddress;
    private String contractId;
    private String assetId;
    private String protocol = "ids-multipart";
    private String managedResources = "false";
    private TransferProcessDataDestination dataDestination;




}
