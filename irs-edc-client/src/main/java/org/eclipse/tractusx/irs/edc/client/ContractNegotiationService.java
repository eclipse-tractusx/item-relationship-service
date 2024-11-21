/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.callback.CallbackAddress;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyExpiredException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyPermissionException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.ContractOffer;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.Response;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessDataDestination;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.springframework.stereotype.Service;

/**
 * Negotiates contracts using the EDC resulting in a data transfer endpoint being sent to the IRS.
 */
@Slf4j
@Service("irsEdcClientContractNegotiationService")
@RequiredArgsConstructor
public class ContractNegotiationService {

    public static final String EDC_PROTOCOL = "dataspace-protocol-http";
    public static final String EVENT_TRANSFER_PROCESS_STARTED = "transfer.process.started";
    public static final String HTTP_DATA_PULL = "HttpData-PULL";
    private final EdcControlPlaneClient edcControlPlaneClient;
    private final PolicyCheckerService policyCheckerService;
    private final EdcConfiguration config;

    public TransferProcessResponse negotiate(final String providerConnectorUrl, final CatalogItem catalogItem,
            final EndpointDataReferenceStatus endpointDataReferenceStatus, final String bpn)
            throws ContractNegotiationException, UsagePolicyPermissionException, TransferProcessException,
            UsagePolicyExpiredException {

        EndpointDataReferenceStatus resultEndpointDataReferenceStatus;

        if (endpointDataReferenceStatus == null) {
            log.info(
                    "Missing information about endpoint data reference from storage, setting token status to REQUIRED_NEW.");
            resultEndpointDataReferenceStatus = new EndpointDataReferenceStatus(null,
                    EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW);
        } else {
            resultEndpointDataReferenceStatus = endpointDataReferenceStatus;
        }

        NegotiationResponse negotiationResponse;
        String contractAgreementId;

        switch (resultEndpointDataReferenceStatus.tokenStatus()) {
            case REQUIRED_NEW -> {
                final CompletableFuture<NegotiationResponse> responseFuture = startNewNegotiation(providerConnectorUrl,
                        catalogItem, bpn);
                negotiationResponse = Objects.requireNonNull(getNegotiationResponse(responseFuture));
                contractAgreementId = negotiationResponse.getContractAgreementId();
            }
            case EXPIRED -> {
                contractAgreementId = resultEndpointDataReferenceStatus.endpointDataReference().getContractId();
                log.info(
                        "Cached endpoint data reference has expired token. Refreshing token without new contract negotiation for contractAgreementId: {}",
                        Masker.mask(contractAgreementId));
            }
            case VALID -> throw new IllegalStateException(
                    "Token is present and valid. Contract negotiation should not be started.");
            default -> throw new IllegalStateException("Unknown token status.");
        }

        final TransferProcessRequest transferProcessRequest = createTransferProcessRequest(providerConnectorUrl,
                catalogItem, contractAgreementId);

        final Response transferProcessId = edcControlPlaneClient.startTransferProcess(transferProcessRequest);

        final CompletableFuture<TransferProcessResponse> transferProcessFuture = edcControlPlaneClient.getTransferProcess(
                transferProcessId);
        final TransferProcessResponse transferProcessResponse = Objects.requireNonNull(
                getTransferProcessResponse(transferProcessFuture));
        log.info("Transfer process completed for transferProcessId: {}", transferProcessResponse.getResponseId());

        return transferProcessResponse;
    }

    private CompletableFuture<NegotiationResponse> startNewNegotiation(final String providerConnectorUrl,
            final CatalogItem catalogItem, final String bpn)
            throws UsagePolicyPermissionException, UsagePolicyExpiredException {
        log.info("Staring new contract negotiation.");

        if (!policyCheckerService.isValid(catalogItem.getPolicy(), bpn)) {
            log.warn("Policy was not allowed, canceling negotiation.");
            throw new UsagePolicyPermissionException(
                    policyCheckerService.getValidStoredPolicies(catalogItem.getConnectorId()), catalogItem.getPolicy(),
                    catalogItem.getConnectorId());
        }

        if (policyCheckerService.isExpired(catalogItem.getPolicy(), bpn)) {
            log.warn("Policy is expired, canceling negotiation.");
            throw new UsagePolicyExpiredException(
                    policyCheckerService.getValidStoredPolicies(catalogItem.getConnectorId()), catalogItem.getPolicy(),
                    catalogItem.getConnectorId());
        }

        final NegotiationRequest negotiationRequest = createNegotiationRequestFromCatalogItem(providerConnectorUrl,
                catalogItem);
        final Response negotiationId = edcControlPlaneClient.startNegotiations(negotiationRequest);
        log.info("Fetch negotiation id: {}", negotiationId.getResponseId());

        return edcControlPlaneClient.getNegotiationResult(negotiationId);
    }

    private TransferProcessRequest createTransferProcessRequest(final String providerConnectorUrl,
            final CatalogItem catalogItem, final String agreementId) {
        final var destination = DataAddress.Builder.newInstance()
                                                   .type(TransferProcessDataDestination.DEFAULT_TYPE)
                                                   .build();
        final var transferProcessRequestBuilder = TransferProcessRequest.builder()
                                                                        .protocol(
                                                                                TransferProcessRequest.DEFAULT_PROTOCOL)
                                                                        .managedResources(
                                                                                TransferProcessRequest.DEFAULT_MANAGED_RESOURCES)
                                                                        .connectorId(catalogItem.getConnectorId())
                                                                        .counterPartyAddress(providerConnectorUrl)
                                                                        .transferType(HTTP_DATA_PULL)
                                                                        .contractId(agreementId)
                                                                        .assetId(catalogItem.getAssetPropId())
                                                                        .dataDestination(destination);
        if (StringUtils.isNotBlank(config.getCallbackUrl())) {
            log.info("Setting EDR callback to {}", config.getCallbackUrl());
            transferProcessRequestBuilder.privateProperties(Map.of("receiverHttpEndpoint", config.getCallbackUrl()));
            final CallbackAddress callbackAddress = CallbackAddress.Builder.newInstance()
                                                                           .uri(config.getCallbackUrl())
                                                                           .events(Set.of(
                                                                                   EVENT_TRANSFER_PROCESS_STARTED))
                                                                           .build();
            transferProcessRequestBuilder.callbackAddresses(List.of(callbackAddress));
        }
        return transferProcessRequestBuilder.build();
    }

    private NegotiationRequest createNegotiationRequestFromCatalogItem(final String providerConnectorUrl,
            final CatalogItem catalogItem) {

        return NegotiationRequest.builder()
                                 .counterPartyAddress(providerConnectorUrl)
                                 .counterPartyId(catalogItem.getConnectorId())
                                 .protocol(EDC_PROTOCOL)
                                 .contractOffer(
                                         ContractOffer.fromPolicy(catalogItem.getPolicy(), catalogItem.getOfferId(),
                                                 catalogItem.getAssetPropId(), catalogItem.getConnectorId()))
                                 .build();
    }

    private NegotiationResponse getNegotiationResponse(final CompletableFuture<NegotiationResponse> negotiationResponse)
            throws ContractNegotiationException {
        try {
            return negotiationResponse.get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            throw new ContractNegotiationException(e);
        }
        return null;
    }

    private TransferProcessResponse getTransferProcessResponse(
            final CompletableFuture<TransferProcessResponse> transferProcessResponse) throws TransferProcessException {
        try {
            return transferProcessResponse.get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            throw new TransferProcessException(e);
        }
        return null;
    }
}

