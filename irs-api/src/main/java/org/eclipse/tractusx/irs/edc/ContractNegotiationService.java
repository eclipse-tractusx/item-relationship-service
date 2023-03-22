/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.model.CatalogItem;
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

    private final CatalogCache catalogCache;

    public NegotiationResponse negotiate(final String providerConnectorUrl, final String target)
            throws ContractNegotiationException {

        final CatalogItem catalogItem = catalogCache.getCatalogItem(providerConnectorUrl, target);

        final ContractOfferRequest contractOfferRequest = ContractOfferRequest.builder()
                                                                              .offerId(
                                                                                      catalogItem.getItemId())
                                                                              .assetId(target)
                                                                              .policy(catalogItem.getPolicy())
                                                                              .build();

        final NegotiationRequest negotiationRequest = NegotiationRequest.builder()
                                                                        .connectorId(catalogItem.getConnectorId())
                                                                        .connectorAddress(providerConnectorUrl
                                                                                + config.getControlplane().getProviderSuffix())
                                                                        .offer(contractOfferRequest)
                                                                        .build();

        final NegotiationId negotiationId = edcControlPlaneClient.startNegotiations(negotiationRequest);

        log.info("Fetch negotation id: {}", negotiationId.getValue());

        final CompletableFuture<NegotiationResponse> responseFuture = edcControlPlaneClient.getNegotiationResult(
                negotiationId);
        final NegotiationResponse response = Objects.requireNonNull(getNegotiationResponse(responseFuture));

        final var destination = TransferProcessDataDestination.builder()
                                                              .type(TransferProcessDataDestination.DEFAULT_TYPE)
                                                              .build();
        final var request = TransferProcessRequest.builder()
                                                  .requestId(UUID.randomUUID().toString())
                                                  .protocol(TransferProcessRequest.DEFAULT_PROTOCOL)
                                                  .managedResources(TransferProcessRequest.DEFAULT_MANAGED_RESOURCES)
                                                  .connectorId(catalogItem.getConnectorId())
                                                  .connectorAddress(
                                                          providerConnectorUrl + config.getControlplane().getProviderSuffix())
                                                  .contractId(response.getContractAgreementId())
                                                  .assetId(target)
                                                  .dataDestination(destination)
                                                  .build();

        final TransferProcessId transferProcessId = edcControlPlaneClient.startTransferProcess(request);

        // can be added to cache after completed
        edcControlPlaneClient.getTransferProcess(transferProcessId).exceptionally(throwable -> {
            log.error("Error while receiving transfer process", throwable);
            return null;
        });
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

}

