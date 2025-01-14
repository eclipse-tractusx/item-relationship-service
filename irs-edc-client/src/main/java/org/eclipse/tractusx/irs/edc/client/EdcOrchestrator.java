/********************************************************************************
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

import static org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus.TokenStatus.VALID;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * Orchestrates interactions with the EDC, including retrieving catalog items,
 * negotiating contracts, and managing endpoint data references.
 */
@Slf4j
@Service
@SuppressWarnings({ "PMD.TooManyMethods",
                    "PMD.UseObjectForClearerAPI"
})
public class EdcOrchestrator {
    private final EdcConfiguration config;
    private final ContractNegotiationService contractNegotiationService;
    private final AsyncPollingService pollingService;
    private final EDCCatalogFacade catalogFacade;
    private final EndpointDataReferenceCacheService endpointDataReferenceCacheService;
    private final ExecutorService executorService;
    private final OngoingNegotiationStorage ongoingNegotiationStorage;

    public EdcOrchestrator(final EdcConfiguration config, final ContractNegotiationService contractNegotiationService,
            final AsyncPollingService pollingService, final EDCCatalogFacade catalogFacade,
            final EndpointDataReferenceCacheService endpointDataReferenceCacheService,
            final ExecutorService fixedThreadPoolExecutorService, final OngoingNegotiationStorage ongoingNegotiationStorage) {
        this.config = config;
        this.contractNegotiationService = contractNegotiationService;
        this.pollingService = pollingService;
        this.catalogFacade = catalogFacade;
        this.endpointDataReferenceCacheService = endpointDataReferenceCacheService;
        this.executorService = fixedThreadPoolExecutorService;
        this.ongoingNegotiationStorage = ongoingNegotiationStorage;
    }

    private static void stopWatchOnEdcTask(final StopWatch stopWatch) {
        stopWatch.stop();
        log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }

    /**
     * Retrieves a list of catalog items from a specified endpoint, filtered by the given criteria.
     *
     * @param dspEndpointAddress The address of the endpoint from which to retrieve catalog items.
     * @param filterKey          The key used to filter the catalog items.
     * @param filterValue        The value associated with the filter key to filter the catalog items.
     * @param bpn                The business partner number associated with the catalog items.
     * @return A list of {@link CatalogItem} objects that match the specified filter criteria.
     * @throws EdcClientException If an error occurs while retrieving the catalog items.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException") // catching a generic exception is intended here
    public List<CatalogItem> getCatalogItems(final String dspEndpointAddress, final String filterKey,
            final String filterValue, final String bpn) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get Catalog Items");

        CompletableFuture<List<CatalogItem>> objectCompletableFuture;
        try {
            objectCompletableFuture = CompletableFuture.supplyAsync(() -> {
                final List<CatalogItem> contractOffers = catalogFacade.fetchCatalogByFilter(dspEndpointAddress,
                        filterKey, filterValue, bpn);

                log.debug("Retrieved catalog items: '{}'", StringMapper.mapToString(contractOffers));
                stopWatchOnEdcTask(stopWatch);
                return contractOffers;

            }, executorService);
        } catch (Exception e) {
            objectCompletableFuture = CompletableFuture.failedFuture(
                    new EdcClientException("Error retrieving catalog items.", e));
        }
        try {
            return objectCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new EdcClientException("Error retrieving catalog items.", e);
        }
    }

    /**
     * Retrieves a single catalog item based on the provided asset ID and BPN.
     *
     * @param dspEndpointAddress The address of the endpoint to retrieve the catalog item from.
     * @param assetId            The unique identifier of the asset for which the catalog item is required.
     * @param bpn                The business partner number associated with the catalog item.
     * @return The first matching catalog item found for the given asset ID and BPN.
     * @throws EdcClientException If an error occurs while retrieving the catalog item.
     */
    public CatalogItem getCatalogItem(final String dspEndpointAddress, final String assetId, final String bpn)
            throws EdcClientException {
        final List<CatalogItem> catalogItems = getCatalogItems(dspEndpointAddress, JsonLdConfiguration.NAMESPACE_EDC_ID,
                assetId, bpn);
        return catalogItems.stream()
                           .findFirst()
                           .orElseThrow(() -> new EdcClientException(
                                   "Catalog is empty for endpointAddress '%s' filterKey '%s', filterValue '%s'".formatted(
                                           dspEndpointAddress, JsonLdConfiguration.NAMESPACE_EDC_ID, assetId)));

    }

    /**
     * Retrieves an {@link EndpointDataReference} for a given catalog item from the specified endpoint address.
     * The method first checks if a valid endpoint data reference is available in the cache.
     * If not, it checks if a negotiation is already ongoing for the asset.
     * If the token is expired, it attempts to renew the token; otherwise, it starts a new negotiation.
     *
     * @param dspEndpointAddress The address of the endpoint from which to retrieve the endpoint data reference.
     * @param catalogItem        The catalog item for which the endpoint data reference is required.
     * @return A {@link CompletableFuture} that will complete with the {@link EndpointDataReference} for the specified
     * catalog item.
     * @throws EdcClientException If an error occurs while retrieving the endpoint data reference.
     */
    public CompletableFuture<EndpointDataReference> getEndpointDataReference(final String dspEndpointAddress,
            final CatalogItem catalogItem) throws EdcClientException {
        return getEndpointDataReference(dspEndpointAddress, catalogItem.getItemId(), catalogItem.getConnectorId(),
                Optional.of(catalogItem));

    }

    public CompletableFuture<EndpointDataReference> getEndpointDataReference(final String dspEndpointAddress,
            final String assetId, final String bpn, final Optional<CatalogItem> optionalCatalogItem)
            throws EdcClientException {
        log.info("Retrieving endpoint data reference from cache for asset id: '{}' on edc: '{}'", assetId,
                dspEndpointAddress);
        final String storageId = assetId + dspEndpointAddress;

        synchronized (ongoingNegotiationStorage) {
            final EndpointDataReferenceStatus cachedEdr = endpointDataReferenceCacheService.getEndpointDataReference(
                    storageId);
            if (VALID.equals(cachedEdr.tokenStatus())) {
                log.info("Endpoint data reference found in cache with token status valid, reusing cache record.");
                return CompletableFuture.completedFuture(cachedEdr.endpointDataReference());
            }
            if (ongoingNegotiationStorage.isNegotiationOngoing(storageId)) {
                log.info(
                        "Negotiation for asset id '{}' on edc: '{}' is already in progress. Returning ongoing negotiation.",
                        assetId, dspEndpointAddress);
                return ongoingNegotiationStorage.getOngoingNegotiation(storageId);
            }

            final CatalogItem catalogItem;
            if (optionalCatalogItem.isPresent()) {
                catalogItem = optionalCatalogItem.get();
                log.debug("Reusing existing catalogItem: '{}'", catalogItem);
            } else {
                catalogItem = getCatalogItem(dspEndpointAddress, assetId, bpn);
                log.debug("No catalogItem provided, requesting new: '{}'", catalogItem);
            }

            log.info("No previous or ongoing negotiations for asset id '{}' on edc '{}'. Starting new negotiation.",
                    assetId, dspEndpointAddress);
            return negotiateEndpointDataReference(dspEndpointAddress, catalogItem, cachedEdr);
        }
    }

    /**
     * Retrieves a list of {@link CompletableFuture} objects, each representing the retrieval of an
     * {@link EndpointDataReference} for a specific {@link CatalogItem} from the specified endpoint.
     *
     * @param endpointAddress The address of the endpoint from which to retrieve the {@link EndpointDataReference}s.
     * @param catalogItems    A list of {@link CatalogItem} objects for which to retrieve {@link EndpointDataReference}s.
     * @return A list of {@link CompletableFuture} objects, each representing the retrieval of an
     * {@link EndpointDataReference}. If an error occurs while retrieving an {@link EndpointDataReference} for a
     * specific {@link CatalogItem},
     * the corresponding {@link CompletableFuture} will be completed exceptionally with an {@link EdcClientException}.
     */
    public List<CompletableFuture<EndpointDataReference>> getEndpointDataReferences(final String endpointAddress,
            final List<CatalogItem> catalogItems) {
        return catalogItems.stream().map(catalogItem -> {
            try {
                return getEndpointDataReference(endpointAddress, catalogItem);
            } catch (EdcClientException e) {
                final String message = "Failed to get EndpointDataReference for endpointAddress '%s', catalogItem = '%s'".formatted(
                        endpointAddress, catalogItem);
                log.warn(message);
                return CompletableFuture.<EndpointDataReference>failedFuture(e);
            }
        }).toList();
    }

    private CompletableFuture<EndpointDataReference> negotiateEndpointDataReference(final String dspEndpointAddress,
            final CatalogItem catalogItem, final EndpointDataReferenceStatus endpointDataReferenceStatus) {
        final String assetId = catalogItem.getItemId();
        final String storageId = assetId + dspEndpointAddress;

        final CompletableFuture<EndpointDataReference> completableFuture = awaitEndpointReferenceForAsset(
                dspEndpointAddress, catalogItem, endpointDataReferenceStatus);
        log.info("Initiated negotiation for id '{}' on edc '{}' and storing it in ongoing negotiations", assetId,
                dspEndpointAddress);
        ongoingNegotiationStorage.addToOngoingNegotiations(storageId, completableFuture);

        completableFuture.whenCompleteAsync((endpointDataReference, throwable) -> {
            log.info("Completed waiting for EndpointDataReference. Storing EDR and removing from ongoing negotiations");
            endpointDataReferenceCacheService.putEndpointDataReferenceIntoStorage(storageId,
                    endpointDataReference);
            ongoingNegotiationStorage.removeFromOngoingNegotiations(storageId);
        }, executorService);

        return completableFuture;
    }

    private CompletableFuture<EndpointDataReference> awaitEndpointReferenceForAsset(final String dspEndpointAddress,
            final CatalogItem catalogItem, final EndpointDataReferenceStatus endpointDataReferenceStatus) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + dspEndpointAddress);
        final String bpn = catalogItem.getConnectorId();

        final CompletableFuture<String> futureStorageId = CompletableFuture.supplyAsync(() -> {
            try {
                final TransferProcessResponse response = contractNegotiationService.negotiate(dspEndpointAddress,
                        catalogItem, endpointDataReferenceStatus, bpn);
                return getStorageId(endpointDataReferenceStatus, response);
            } catch (EdcClientException e) {
                throw new CompletionException(e);
            }
        });

        return futureStorageId.thenComposeAsync(storageId -> pollingService.<EndpointDataReference>createJob()
                                                                           .action(() -> retrieveEndpointReference(
                                                                                   storageId, stopWatch))
                                                                           .timeToLive(
                                                                                   config.getSubmodel().getRequestTtl())
                                                                           .description(
                                                                                   "waiting for Endpoint Reference retrieval")
                                                                           .build()
                                                                           .schedule());
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

    private static String getStorageId(final EndpointDataReferenceStatus endpointDataReferenceStatus,
            final TransferProcessResponse response) {
        final String storageId;
        if (response != null) {
            storageId = response.getContractId();
        } else {
            storageId = endpointDataReferenceStatus.endpointDataReference().getContractId();
        }
        return storageId;
    }
}
