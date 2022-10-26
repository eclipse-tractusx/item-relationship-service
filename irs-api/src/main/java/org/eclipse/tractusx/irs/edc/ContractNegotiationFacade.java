package org.eclipse.tractusx.irs.edc;

import static org.eclipse.tractusx.irs.edc.EdcClient.CONTROL_PLANE_SUFIX;
import static org.eclipse.tractusx.irs.edc.EdcClient.PROVIDER_CONTROL_PLANE;

import java.util.NoSuchElementException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.policy.model.PolicyType;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.irs.edc.model.ContractOfferRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessDataDestination;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractNegotiationFacade {

    private final EdcClient edcClient;

    public TransferProcessId negotiate(String target) {
        log.info("Get catalog from EDC provider.");
        Catalog catalog = edcClient.getCatalog();

        log.info("Search for offer for asset id: {}", target);
        final ContractOffer contractOfferForGivenAssetId = findOffer(target, catalog);

        final ContractOfferRequest contractOfferRequest = ContractOfferRequest.builder()
                            .offerId(contractOfferForGivenAssetId.getId())
                            .assetId(target)
                            .policy(policyFor(target))
                            .build();

        final NegotiationRequest negotiationRequest = NegotiationRequest.builder()
                            .connectorId(catalog.getId())
                            .connectorAddress(PROVIDER_CONTROL_PLANE + CONTROL_PLANE_SUFIX)
                            .offer(contractOfferRequest)
                            .build();

        final NegotiationId negotiationId = edcClient.startNegotiations(negotiationRequest);

        log.info("Fetch negotation id: {}", negotiationId.getId());

        final NegotiationResponse negotiationResponse = edcClient.getNegotiationResult(negotiationId);

        TransferProcessRequest transferProcessRequest = TransferProcessRequest.builder()
                .id(UUID.randomUUID().toString())
                .connectorId(catalog.getId())
                .connectorAddress(PROVIDER_CONTROL_PLANE + CONTROL_PLANE_SUFIX)
                .contractId(negotiationResponse.getContractAgreementId())
                .assetId(target)
                .dataDestination(TransferProcessDataDestination.builder().build())
                .build();

        final TransferProcessId transferProcessId = edcClient.startTransferProcess(transferProcessRequest);

        edcClient.getTransferProcess(transferProcessId);
        log.info("Transfer process completed for transferProcessId: {}", transferProcessId.getId());
        return transferProcessId;
    }

    private static ContractOffer findOffer(final String target, final Catalog catalog) {
        return catalog.getContractOffers()
                      .stream()
                      .filter(contractOffer -> contractOffer.getAsset().getId().equals(target))
                      .findFirst()
                      .orElseThrow(NoSuchElementException::new);
    }

    private static Policy policyFor(final String target) {
        return Policy.Builder.newInstance()
                             .permission(Permission.Builder.newInstance()
                                 .target(target)
                                 .action(Action.Builder.newInstance().type("USE").build())
                                 .build())
                             .type(PolicyType.SET)
                             .build();
    }

}
