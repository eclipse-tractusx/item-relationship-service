/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.policy.model.PolicyType;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.model.ContractOfferRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessDataDestination;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.exceptions.ContractNegotiationException;
import org.springframework.stereotype.Service;

/**
 * Negotiates contracts using the EDC resulting in a data transfer endpoint being sent to the IRS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractNegotiationService {

    private final EdcControlPlaneClient edcControlPlaneClient;

    private final EdcConfiguration config;

    public NegotiationResponse negotiate(final String providerConnectorUrl, final String target)
            throws ContractNegotiationException {
        log.info("Get catalog from EDC provider.");
        final Catalog catalog = edcControlPlaneClient.getCatalog(providerConnectorUrl);

        log.info("Search for offer for asset id: {}", target);
        final ContractOffer contractOfferForGivenAssetId = findOffer(target, catalog);

        final ContractOfferRequest contractOfferRequest = ContractOfferRequest.builder()
                                                                              .offerId(
                                                                                      contractOfferForGivenAssetId.getId())
                                                                              .assetId(target)
                                                                              .policy(policyFor(target))
                                                                              .build();

        final NegotiationRequest negotiationRequest = NegotiationRequest.builder()
                                                                        .connectorId(catalog.getId())
                                                                        .connectorAddress(providerConnectorUrl
                                                                                + config.getControlPlaneProviderSuffix())
                                                                        .offer(contractOfferRequest)
                                                                        .build();

        final NegotiationId negotiationId = edcControlPlaneClient.startNegotiations(negotiationRequest);

        log.info("Fetch negotation id: {}", negotiationId.getValue());

        final CompletableFuture<NegotiationResponse> responseFuture = edcControlPlaneClient.getNegotiationResult(
                negotiationId);
        final NegotiationResponse response = Objects.requireNonNull(getNegotiationResponse(responseFuture));

        final var request = TransferProcessRequest.builder()
                                                  .requestId(UUID.randomUUID().toString())
                                                  .connectorId(catalog.getId())
                                                  .connectorAddress(
                                                          providerConnectorUrl + config.getControlPlaneProviderSuffix())
                                                  .contractId(response.getContractAgreementId())
                                                  .assetId(target)
                                                  .dataDestination(TransferProcessDataDestination.builder().build())
                                                  .build();

        final TransferProcessId transferProcessId = edcControlPlaneClient.startTransferProcess(request);

        // can be added to cache after completed
        edcControlPlaneClient.getTransferProcess(transferProcessId);
        log.info("Transfer process completed for transferProcessId: {}", transferProcessId.getValue());
        return response;
    }

    private NegotiationResponse getNegotiationResponse(final CompletableFuture<NegotiationResponse> negotiationResponse)
            throws ContractNegotiationException {
        try {
            return negotiationResponse.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new ContractNegotiationException(e);
        }
        return null;
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

