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

import static org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus.TokenStatus;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_ID;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.StopWatch;

/**
 * Public API facade for EDC domain
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.TooManyMethods",
                    "PMD.ExcessiveImports"
})
public class EdcSubmodelClientImpl implements EdcSubmodelClient {

    private final EdcConfiguration config;
    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final AsyncPollingService pollingService;
    private final RetryRegistry retryRegistry;
    private final EDCCatalogFacade catalogFacade;
    private final EndpointDataReferenceCacheService endpointDataReferenceCacheService;
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

    private static void stopWatchOnEdcTask(final StopWatch stopWatch) {
        stopWatch.stop();
        log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }

    private CompletableFuture<EdcNotificationResponse> sendNotificationAsync(final String assetId,
            final EdcNotification<NotificationContent> notification, final StopWatch stopWatch,
            final EndpointDataReference endpointDataReference) {

        return pollingService.<EdcNotificationResponse>createJob()
                             .action(() -> sendSubmodelNotification(assetId, notification, stopWatch,
                                     endpointDataReference))
                             .timeToLive(config.getSubmodel().getRequestTtl())
                             .description("waiting for submodel notification to be sent")
                             .build()
                             .schedule();
    }

    private Optional<SubmodelDescriptor> retrieveSubmodelData(final String submodelDataplaneUrl,
            final StopWatch stopWatch, final EndpointDataReference endpointDataReference) {
        if (endpointDataReference != null) {
            log.info("Retrieving data from EDC data plane for dataReference with id {}", endpointDataReference.getId());
            final String payload = edcDataPlaneClient.getData(endpointDataReference, submodelDataplaneUrl);
            stopWatchOnEdcTask(stopWatch);

            return Optional.of(
                    new SubmodelDescriptor(getContractAgreementId(endpointDataReference.getAuthCode()), payload));
        }

        return Optional.empty();
    }

    @Nullable
    private String getContractAgreementId(final String authCode) {
        return StringUtils.isNotBlank(authCode) ? EDRAuthCode.fromAuthCodeToken(authCode).getCid() : null;
    }

    private Optional<EndpointDataReference> retrieveEndpointReference(final String storageId,
            final StopWatch stopWatch) {

        log.info("Retrieving dataReference from storage for storageId (assetId or contractAgreementId): {}",
                Masker.mask(storageId));
        final var dataReference = endpointDataReferenceCacheService.getEndpointDataReferenceFromStorage(storageId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Retrieving Endpoint Reference data from EDC data plane with id: {}", ref.getId());
            stopWatchOnEdcTask(stopWatch);
            return Optional.of(ref);
        }

        return Optional.empty();
    }

    private Optional<EdcNotificationResponse> sendSubmodelNotification(final String assetId,
            final EdcNotification<NotificationContent> notification, final StopWatch stopWatch,
            final EndpointDataReference endpointDataReference) {

        if (endpointDataReference != null) {
            log.info("Sending dataReference to EDC data plane for assetId '{}'", assetId);
            final EdcNotificationResponse response = edcDataPlaneClient.sendData(endpointDataReference, notification);
            stopWatchOnEdcTask(stopWatch);
            return Optional.of(response);
        }

        return Optional.empty();
    }

    @Override
    public CompletableFuture<SubmodelDescriptor> getSubmodelPayload(final String connectorEndpoint,
            final String submodelDataplaneUrl, final String assetId) throws EdcClientException {

        final CheckedSupplier<CompletableFuture<SubmodelDescriptor>> waitingForSubmodelRetrieval = () -> {

            log.info("Requesting raw SubmodelPayload for endpoint '{}'.", connectorEndpoint);
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Get EDC Submodel task for raw payload, endpoint " + connectorEndpoint);

            final EndpointDataReference dataReference = getEndpointDataReference(connectorEndpoint, assetId);

            return pollingService.<SubmodelDescriptor>createJob()
                                 .action(() -> retrieveSubmodelData(submodelDataplaneUrl, stopWatch, dataReference))
                                 .timeToLive(config.getSubmodel().getRequestTtl())
                                 .description("waiting for submodel retrieval")
                                 .build()
                                 .schedule();
        };

        return execute(connectorEndpoint, waitingForSubmodelRetrieval);
    }

    private EndpointDataReference getEndpointDataReference(final String connectorEndpoint, final String assetId)
            throws EdcClientException {

        final EndpointDataReference result;

        log.info("Retrieving endpoint data reference from cache for asset id: {}", assetId);
        final var cachedReference = endpointDataReferenceCacheService.getEndpointDataReference(assetId);

        if (cachedReference.tokenStatus() == TokenStatus.VALID) {
            log.info("Endpoint data reference found in cache with token status valid, reusing cache record.");
            result = cachedReference.endpointDataReference();
        } else {
            result = getEndpointDataReferenceAndAddToStorage(connectorEndpoint, assetId, cachedReference);
        }

        return result;
    }

    private EndpointDataReference getEndpointDataReferenceAndAddToStorage(final String connectorEndpoint,
            final String assetId, final EndpointDataReferenceStatus cachedEndpointDataReference)
            throws EdcClientException {
        try {
            final EndpointDataReference endpointDataReference = awaitEndpointReferenceForAsset(connectorEndpoint,
                    NAMESPACE_EDC_ID, assetId, cachedEndpointDataReference).get();
            endpointDataReferenceCacheService.putEndpointDataReferenceIntoStorage(assetId, endpointDataReference);

            return endpointDataReference;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EdcClientException(e);
        } catch (CompletionException | ExecutionException e) {
            throw new EdcClientException(e);
        }
    }

    @Override
    public CompletableFuture<EdcNotificationResponse> sendNotification(final String connectorEndpoint,
            final String assetId, final EdcNotification<NotificationContent> notification) throws EdcClientException {

        return execute(connectorEndpoint, () -> {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Send EDC notification task, endpoint " + connectorEndpoint);
            final EndpointDataReference endpointDataReference = getEndpointDataReference(connectorEndpoint, assetId);

            return sendNotificationAsync(assetId, notification, stopWatch, endpointDataReference);
        });
    }

    @Override
    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(final String endpointAddress,
            final String filterKey, final String filterValue) throws EdcClientException {
        return execute(endpointAddress, () -> getEndpointReferencesForAsset(endpointAddress, filterKey, filterValue,
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)));
    }

    @Override
    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(final String endpointAddress,
            final String filterKey, final String filterValue,
            final EndpointDataReferenceStatus endpointDataReferenceStatus) throws EdcClientException {

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + endpointAddress);

        final String providerWithSuffix = appendSuffix(endpointAddress, config.getControlplane().getProviderSuffix());

        // CatalogItem = contract offer
        final List<CatalogItem> contractOffers = catalogFacade.fetchCatalogByFilter(providerWithSuffix, filterKey,
                filterValue);

        if (contractOffers.isEmpty()) {
            throw new EdcClientException(
                    "Catalog is empty for endpointAddress '%s' filterKey '%s', filterValue '%s'".formatted(
                            endpointAddress, filterKey, filterValue));
        }

        // We need to process each contract offer in parallel
        // (see src/docs/arc42/cross-cutting/discovery-DTR--multiple-EDCs-with-multiple-DTRs.puml
        // and src/docs/arc42/cross-cutting/discovery-DTR--multiple-EDCs-with-multiple-DTRs--detailed.puml)
        return contractOffers.stream().map(contractOffer -> {

            final NegotiationResponse negotiationResponse;
            try {
                negotiationResponse = negotiateContract(endpointDataReferenceStatus, contractOffer, providerWithSuffix);

                final String storageId = getStorageId(endpointDataReferenceStatus, negotiationResponse);

                return pollingService.<EndpointDataReference>createJob()
                                     .action(() -> retrieveEndpointReference(storageId, stopWatch))
                                     .timeToLive(config.getSubmodel().getRequestTtl())
                                     .description("waiting for Endpoint Reference retrieval")
                                     .build()
                                     .schedule();
            } catch (EdcClientException e) {
                log.warn(("Negotiate contract failed for "
                        + "endpointDataReferenceStatus = '%s', catalogItem = '%s', providerWithSuffix = '%s' ").formatted(
                        endpointDataReferenceStatus, contractOffer, providerWithSuffix));
                return (CompletableFuture<EndpointDataReference>) Stream.empty();
            }

        }).toList();
    }

    private NegotiationResponse negotiateContract(final EndpointDataReferenceStatus endpointDataReferenceStatus,
            final CatalogItem catalogItem, final String providerWithSuffix) throws EdcClientException {
        final NegotiationResponse response;
        try {
            response = contractNegotiationService.negotiate(providerWithSuffix, catalogItem,
                    endpointDataReferenceStatus);
        } catch (TransferProcessException | UsagePolicyException | ContractNegotiationException e) {
            throw new EdcClientException(("Negotiation failed for endpoint '%s', " + "tokenStatus '%s', "
                    + "providerWithSuffix '%s', catalogItem '%s'").formatted(
                    endpointDataReferenceStatus.endpointDataReference(), endpointDataReferenceStatus.tokenStatus(),
                    providerWithSuffix, endpointDataReferenceStatus), e);
        }
        return response;
    }

    private CompletableFuture<EndpointDataReference> awaitEndpointReferenceForAsset(final String endpointAddress,
            final String filterKey, final String filterValue,
            final EndpointDataReferenceStatus endpointDataReferenceStatus) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();

        stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + endpointAddress);
        final String providerWithSuffix = appendSuffix(endpointAddress, config.getControlplane().getProviderSuffix());

        final List<CatalogItem> items = catalogFacade.fetchCatalogByFilter(providerWithSuffix, filterKey, filterValue);

        final NegotiationResponse response = contractNegotiationService.negotiate(providerWithSuffix,
                items.stream().findFirst().orElseThrow(), endpointDataReferenceStatus);

        final String storageId = getStorageId(endpointDataReferenceStatus, response);

        return pollingService.<EndpointDataReference>createJob()
                             .action(() -> retrieveEndpointReference(storageId, stopWatch))
                             .timeToLive(config.getSubmodel().getRequestTtl())
                             .description("waiting for Endpoint Reference retrieval")
                             .build()
                             .schedule();
    }

    private static String getStorageId(final EndpointDataReferenceStatus endpointDataReferenceStatus,
            final NegotiationResponse response) {
        final String storageId;
        if (response != null) {
            storageId = response.getContractAgreementId();
        } else {
            final String authCode = endpointDataReferenceStatus.endpointDataReference().getAuthCode();
            if (authCode == null) {
                throw new IllegalStateException("Missing information about AuthCode.");
            }
            storageId = EDRAuthCode.fromAuthCodeToken(authCode).getCid();
        }
        return storageId;
    }

    private String appendSuffix(final String endpointAddress, final String providerSuffix) {
        String addressWithSuffix;
        if (endpointAddress.endsWith(providerSuffix)) {
            addressWithSuffix = endpointAddress;
        } else if (endpointAddress.endsWith("/") && providerSuffix.startsWith("/")) {
            addressWithSuffix = endpointAddress.substring(0, endpointAddress.length() - 1) + providerSuffix;
        } else {
            addressWithSuffix = endpointAddress + providerSuffix;
        }
        return addressWithSuffix;
    }

    @SuppressWarnings({ "PMD.AvoidRethrowingException",
                        "PMD.AvoidCatchingGenericException"
    })
    private <T> T execute(final String endpointAddress, final CheckedSupplier<T> supplier) throws EdcClientException {
        if (!urlValidator.isValid(endpointAddress)) {
            throw new IllegalArgumentException(String.format("Malformed endpoint address '%s'", endpointAddress));
        }
        final String host = URI.create(endpointAddress).getHost();
        final Retry retry = retryRegistry.retry(host, "default");
        try {
            return Retry.decorateCallable(retry, supplier::get).call();
        } catch (EdcClientException e) {
            throw e;
        } catch (Exception e) {
            throw new EdcClientException(e);
        }
    }

    /**
     * Functional interface for a supplier that may throw a checked exception.
     *
     * @param <T> the returned type
     */
    private interface CheckedSupplier<T> {
        T get() throws EdcClientException;
    }
}
