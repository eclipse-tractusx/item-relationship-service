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
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.ContractOfferDescription;
import org.eclipse.tractusx.irs.edc.client.model.EndpointDataReferenceEntryResponse;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.Response;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessDataDestination;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.springframework.stereotype.Service;

/**
 * Negotiates contracts using the EDC resulting in a data transfer endpoint being sent to the IRS.
 */
@Slf4j
@Service("irsEdcClientContractNegotiationService")
@RequiredArgsConstructor
public class ContractNegotiationService {

    public static final String EDC_PROTOCOL = "dataspace-protocol-http";
    private final EdcControlPlaneClient edcControlPlaneClient;

    private final PolicyCheckerService policyCheckerService;

    private final EdcConfiguration config;

    public NegotiationResponse negotiate(final String providerConnectorUrl, final CatalogItem catalogItem)
            throws ContractNegotiationException, UsagePolicyException, TransferProcessException {
        if (!policyCheckerService.isValid(catalogItem.getPolicy())) {
            log.info("Policy was not allowed, canceling negotiation.");
            throw new UsagePolicyException(catalogItem.getItemId());
        }

        final NegotiationRequest negotiationRequest = createNegotiationRequestFromCatalogItem(providerConnectorUrl,
                catalogItem);

        final Response negotiationId = edcControlPlaneClient.startNegotiations(negotiationRequest);

        log.info("Fetch negotiation id: {}", negotiationId.getResponseId());

        final CompletableFuture<NegotiationResponse> responseFuture = edcControlPlaneClient.getNegotiationResult(
                negotiationId);
        final NegotiationResponse negotiationResponse = Objects.requireNonNull(getNegotiationResponse(responseFuture));

        final TransferProcessRequest transferProcessRequest = createTransferProcessRequest(providerConnectorUrl,
                catalogItem, negotiationResponse);

        final Response transferProcessId = edcControlPlaneClient.startTransferProcess(transferProcessRequest);

        // can be added to cache after completed
        final CompletableFuture<TransferProcessResponse> transferProcessFuture = edcControlPlaneClient.getTransferProcess(
                transferProcessId);
        final TransferProcessResponse transferProcessResponse = Objects.requireNonNull(
                getFutureResult(transferProcessFuture));
        log.info("Transfer process completed for transferProcessId: {}", transferProcessResponse.getResponseId());
        return negotiationResponse;
    }

    private TransferProcessRequest createTransferProcessRequest(final String providerConnectorUrl,
            final CatalogItem catalogItem, final NegotiationResponse response) {
        final var destination = DataAddress.Builder.newInstance()
                                                   .type(TransferProcessDataDestination.DEFAULT_TYPE)
                                                   .build();
        final var transferProcessRequestBuilder = TransferProcessRequest.builder()
                                                                        .protocol(
                                                                                TransferProcessRequest.DEFAULT_PROTOCOL)
                                                                        .managedResources(
                                                                                TransferProcessRequest.DEFAULT_MANAGED_RESOURCES)
                                                                        .connectorId(catalogItem.getConnectorId())
                                                                        .connectorAddress(providerConnectorUrl)
                                                                        .contractId(response.getContractAgreementId())
                                                                        .assetId(catalogItem.getAssetPropId())
                                                                        .dataDestination(destination);
        if (StringUtils.isNotBlank(config.getCallbackUrl())) {
            log.info("Setting EDR callback to {}", config.getCallbackUrl());
            transferProcessRequestBuilder.privateProperties(Map.of("receiverHttpEndpoint", config.getCallbackUrl()));
        }
        return transferProcessRequestBuilder.build();
    }

    private NegotiationRequest createNegotiationRequestFromCatalogItem(final String providerConnectorUrl,
            final CatalogItem catalogItem) {
        final var contractOfferDescription = ContractOfferDescription.builder()
                                                                     .offerId(catalogItem.getOfferId())
                                                                     .assetId(catalogItem.getPolicy().getTarget())
                                                                     .policy(catalogItem.getPolicy())
                                                                     .build();

        return NegotiationRequest.builder()
                                 .connectorId(catalogItem.getConnectorId())
                                 .connectorAddress(providerConnectorUrl)
                                 .protocol(EDC_PROTOCOL)
                                 .offer(contractOfferDescription)
                                 .build();
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

    private <T> T getFutureResult(final CompletableFuture<T> future) throws TransferProcessException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new TransferProcessException(e);
        }
        return null;
    }

    public NegotiationResponse negotiateWithEdrManagement(final String providerConnectorUrl,
            final CatalogItem catalogItem) throws ContractNegotiationException, UsagePolicyException {
        if (!policyCheckerService.isValid(catalogItem.getPolicy())) {
            log.info("Policy was not allowed, canceling negotiation.");
            throw new UsagePolicyException(catalogItem.getItemId());
        }

        final NegotiationRequest negotiationRequest = createNegotiationRequestFromCatalogItem(providerConnectorUrl,
                catalogItem);

        final Response negotiationId = edcControlPlaneClient.startEDRNegotiations(negotiationRequest);

        log.info("Fetch negotiation id: {}", negotiationId.getResponseId());

        final CompletableFuture<NegotiationResponse> responseFuture = edcControlPlaneClient.getNegotiationResult(
                negotiationId);
        return Objects.requireNonNull(getNegotiationResponse(responseFuture));
    }

    public EndpointDataReference getManagedEndpointDataReference(final String contractAgreementId)
            throws TransferProcessException {
        final CompletableFuture<EndpointDataReferenceEntryResponse> edrEntryFuture = edcControlPlaneClient.getEndpointDataReferenceEntry(
                contractAgreementId);

        final EndpointDataReferenceEntryResponse edrEntryResponse = Objects.requireNonNull(
                getFutureResult(edrEntryFuture));

        final EndpointDataReference edr = edcControlPlaneClient.getEndpointDataReference(
                edrEntryResponse.getTransferProcessId());
        log.info("Got EDR with id: {}", edr.getId());
        return edr;
    }

}

