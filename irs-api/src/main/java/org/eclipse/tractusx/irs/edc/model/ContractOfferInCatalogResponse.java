package org.eclipse.tractusx.irs.edc.model;

import lombok.Builder;
import lombok.Value;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;

/**
 * EDC catalog and contract offer response.
 */
@Value
@Builder(toBuilder = true)
public class ContractOfferInCatalogResponse {

    private String connectorId;
    private ContractOffer contractOffer;

}
