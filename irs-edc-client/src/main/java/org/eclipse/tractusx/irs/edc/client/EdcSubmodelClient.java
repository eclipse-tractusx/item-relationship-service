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

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_ID;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.edc.client.util.Masker;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * Public API facade for EDC domain
 */
@SuppressWarnings("PMD.ExcessiveImports")
public interface EdcSubmodelClient {

    CompletableFuture<String> getSubmodelRawPayload(String connectorEndpoint, String submodelDataplaneUrl,
            String assetId) throws EdcClientException;

    CompletableFuture<EdcNotificationResponse> sendNotification(String submodelEndpointAddress, String assetId,
            EdcNotification<NotificationContent> notification) throws EdcClientException;

    CompletableFuture<EndpointDataReference> getEndpointReferenceForAsset(String endpointAddress, String filterKey,
            String filterValue) throws EdcClientException;
}

/**
 * Submodel facade stub used in local environment
 */
@Service
@Profile({ "local",
           "stubtest"
})
class EdcSubmodelClientLocalStub implements EdcSubmodelClient {

    private final SubmodelTestdataCreator testdataCreator;

    /* package */ EdcSubmodelClientLocalStub(final CxTestDataContainer cxTestDataContainer) {
        this.testdataCreator = new SubmodelTestdataCreator(cxTestDataContainer);
    }

    @Override
    public CompletableFuture<String> getSubmodelRawPayload(final String connectorEndpoint,
            final String submodelDataplaneUrl, final String assetId) throws EdcClientException {
        if ("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(assetId)) {
            throw new EdcClientException("Dummy Exception");
        }
        final Map<String, Object> submodel = testdataCreator.createSubmodelForId(assetId + "_" + submodelDataplaneUrl);
        return CompletableFuture.completedFuture(StringMapper.mapToString(submodel));
    }

    @Override
    public CompletableFuture<EdcNotificationResponse> sendNotification(final String submodelEndpointAddress,
            final String assetId, final EdcNotification<NotificationContent> notification) {
        // not actually sending anything, just return success response
        return CompletableFuture.completedFuture(() -> true);
    }

    @Override
    public CompletableFuture<EndpointDataReference> getEndpointReferenceForAsset(final String endpointAddress,
            final String filterKey, final String filterValue) throws EdcClientException {
        throw new EdcClientException("Not implemented");
    }
}

/**
 * Public API facade for EDC domain
 */
@Service("irsEdcClientEdcSubmodelClientImpl")
@Slf4j
@RequiredArgsConstructor
@Profile({ "!local && !stubtest" })
@SuppressWarnings("PMD.TooManyMethods")
class EdcSubmodelClientImpl implements EdcSubmodelClient {

    private final EdcConfiguration config;
    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage;
    private final AsyncPollingService pollingService;
    private final RetryRegistry retryRegistry;
    private final EDCCatalogFacade catalogFacade;
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

    private static void stopWatchOnEdcTask(final StopWatch stopWatch) {
        stopWatch.stop();
        log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }

    private CatalogItem fetchCatalogItemWithFilter(final String connectorEndpoint, final String assetId)
            throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + connectorEndpoint);

        final List<CatalogItem> catalog = catalogFacade.fetchCatalogByFilter(connectorEndpoint, NAMESPACE_EDC_ID,
                assetId);

        return catalog.stream()
                      .findFirst()
                      .orElseThrow(() -> new ItemNotFoundInCatalogException(connectorEndpoint, assetId));
    }

    private CompletableFuture<EdcNotificationResponse> sendNotificationAsync(final String contractAgreementId,
            final EdcNotification<NotificationContent> notification, final StopWatch stopWatch)
            throws TransferProcessException {
        if (config.getControlplane().isEdrManagementEnabled()) {
            return CompletableFuture.completedFuture(
                    sendSubmodelNotificationWithManagedEdr(contractAgreementId, notification, stopWatch));
        } else {
            return pollingService.<EdcNotificationResponse>createJob()
                                 .action(() -> sendSubmodelNotification(contractAgreementId, notification, stopWatch))
                                 .timeToLive(config.getSubmodel().getRequestTtl())
                                 .description("waiting for submodel notification to be sent")
                                 .build()
                                 .schedule();
        }
    }

    private Optional<String> retrieveSubmodelData(final String submodelDataplaneUrl, final String contractAgreementId,
            final StopWatch stopWatch) {
        final Optional<EndpointDataReference> dataReference = retrieveEndpointDataReference(contractAgreementId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Retrieving data from EDC data plane for dataReference with id {}", ref.getId());
            final String data = edcDataPlaneClient.getData(ref, submodelDataplaneUrl);
            stopWatchOnEdcTask(stopWatch);

            return Optional.of(data);
        }
        return Optional.empty();
    }

    private Optional<EndpointDataReference> retrieveEndpointReference(final String contractAgreementId,
            final StopWatch stopWatch) {
        final Optional<EndpointDataReference> dataReference = retrieveEndpointDataReference(contractAgreementId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Retrieving Endpoint Reference data from EDC data plane with id: {}", ref.getId());
            stopWatchOnEdcTask(stopWatch);

            return Optional.of(ref);
        }
        return Optional.empty();
    }

    private Optional<EdcNotificationResponse> sendSubmodelNotification(final String contractAgreementId,
            final EdcNotification<NotificationContent> notification, final StopWatch stopWatch) {
        final Optional<EndpointDataReference> dataReference = retrieveEndpointDataReference(contractAgreementId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Sending dataReference to EDC data plane for contractAgreementId '{}'",
                    Masker.mask(contractAgreementId));
            final EdcNotificationResponse response = edcDataPlaneClient.sendData(ref, notification);
            stopWatchOnEdcTask(stopWatch);
            return Optional.of(response);
        }
        return Optional.empty();
    }

    private EdcNotificationResponse sendSubmodelNotificationWithManagedEdr(final String contractAgreementId,
            final EdcNotification<NotificationContent> notification, final StopWatch stopWatch)
            throws TransferProcessException {
        final EndpointDataReference ref = contractNegotiationService.getManagedEndpointDataReference(
                contractAgreementId);

        log.info("Sending dataReference to EDC data plane for contractAgreementId '{}'",
                Masker.mask(contractAgreementId));
        final EdcNotificationResponse response = edcDataPlaneClient.sendData(ref, notification);
        stopWatchOnEdcTask(stopWatch);
        return response;
    }

    @Override
    public CompletableFuture<String> getSubmodelRawPayload(final String connectorEndpoint,
            final String submodelDataplaneUrl, final String assetId) throws EdcClientException {
        return execute(connectorEndpoint, () -> {
            log.info("Requesting raw SubmodelPayload for endpoint '{}'.", connectorEndpoint);
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Get EDC Submodel task for raw payload, endpoint " + connectorEndpoint);

            final var negotiationEndpoint = appendSuffix(connectorEndpoint,
                    config.getControlplane().getProviderSuffix());
            log.debug("Starting negotiation with EDC endpoint: '{}'", negotiationEndpoint);
            final CatalogItem catalogItem = fetchCatalogItemWithFilter(negotiationEndpoint, assetId);
            if (config.getControlplane().isEdrManagementEnabled()) {
                return getSubmodelRawPayloadWithManagedEdr(negotiationEndpoint, catalogItem, submodelDataplaneUrl);
            } else {
                return getSubmodelRawPayloadWithEdrCallback(negotiationEndpoint, catalogItem, submodelDataplaneUrl,
                        stopWatch);
            }
        });
    }

    private CompletableFuture<String> getSubmodelRawPayloadWithEdrCallback(final String negotiationEndpoint,
            final CatalogItem catalogItem, final String submodelDataplaneUrl, final StopWatch stopWatch)
            throws EdcClientException {
        final NegotiationResponse negotiationResponse = contractNegotiationService.negotiate(negotiationEndpoint,
                catalogItem);

        return pollingService.<String>createJob()
                             .action(() -> retrieveSubmodelData(submodelDataplaneUrl,
                                     negotiationResponse.getContractAgreementId(), stopWatch))
                             .timeToLive(config.getSubmodel().getRequestTtl())
                             .description("waiting for submodel retrieval")
                             .build()
                             .schedule();
    }

    private CompletableFuture<String> getSubmodelRawPayloadWithManagedEdr(final String negotiationEndpoint,
            final CatalogItem catalogItem, final String submodelDataplaneUrl) throws EdcClientException {
        final NegotiationResponse negotiationResponse = contractNegotiationService.negotiateWithEdrManagement(
                negotiationEndpoint, catalogItem);
        final EndpointDataReference edr = contractNegotiationService.getManagedEndpointDataReference(
                negotiationResponse.getContractAgreementId());

        return CompletableFuture.completedFuture(edcDataPlaneClient.getData(edr, submodelDataplaneUrl));
    }

    @Override
    public CompletableFuture<EdcNotificationResponse> sendNotification(final String connectorEndpoint,
            final String assetId, final EdcNotification<NotificationContent> notification) throws EdcClientException {
        return execute(connectorEndpoint, () -> {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Send EDC notification task, endpoint " + connectorEndpoint);
            final var negotiationEndpoint = appendSuffix(connectorEndpoint,
                    config.getControlplane().getProviderSuffix());
            final CatalogItem catalogItem = fetchCatalogItemWithFilter(negotiationEndpoint, assetId);
            final NegotiationResponse negotiationResponse = config.getControlplane().isEdrManagementEnabled()
                    ? contractNegotiationService.negotiateWithEdrManagement(negotiationEndpoint, catalogItem)
                    : contractNegotiationService.negotiate(negotiationEndpoint, catalogItem);

            return sendNotificationAsync(negotiationResponse.getContractAgreementId(), notification, stopWatch);
        });
    }

    @Override
    public CompletableFuture<EndpointDataReference> getEndpointReferenceForAsset(final String endpointAddress,
            final String filterKey, final String filterValue) throws EdcClientException {
        return execute(endpointAddress, () -> {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + endpointAddress);
            final String providerWithSuffix = appendSuffix(endpointAddress,
                    config.getControlplane().getProviderSuffix());

            final List<CatalogItem> items = catalogFacade.fetchCatalogByFilter(providerWithSuffix, filterKey,
                    filterValue);

            if (config.getControlplane().isEdrManagementEnabled()) {
                return getManagedEndpointDataReference(providerWithSuffix, items);
            } else {
                return getEndpointDataReferenceWithCallback(providerWithSuffix, items, stopWatch);
            }
        });
    }

    private CompletableFuture<EndpointDataReference> getEndpointDataReferenceWithCallback(
            final String providerWithSuffix, final List<CatalogItem> items, final StopWatch stopWatch)
            throws ContractNegotiationException, UsagePolicyException, TransferProcessException {
        final NegotiationResponse response = contractNegotiationService.negotiate(providerWithSuffix,
                items.stream().findFirst().orElseThrow());

        return pollingService.<EndpointDataReference>createJob()
                             .action(() -> retrieveEndpointReference(response.getContractAgreementId(), stopWatch))
                             .timeToLive(config.getSubmodel().getRequestTtl())
                             .description("waiting for Endpoint Reference retrieval")
                             .build()
                             .schedule();
    }

    @NotNull
    private CompletableFuture<EndpointDataReference> getManagedEndpointDataReference(final String providerWithSuffix,
            final List<CatalogItem> items)
            throws ContractNegotiationException, UsagePolicyException, TransferProcessException {
        final NegotiationResponse negotiationResponse = contractNegotiationService.negotiateWithEdrManagement(
                providerWithSuffix, items.stream().findFirst().orElseThrow());
        final EndpointDataReference edr = contractNegotiationService.getManagedEndpointDataReference(
                negotiationResponse.getContractAgreementId());

        return CompletableFuture.completedFuture(edr);
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

    private Optional<EndpointDataReference> retrieveEndpointDataReference(final String contractAgreementId) {
        log.info("Retrieving dataReference from storage for contractAgreementId {}", Masker.mask(contractAgreementId));
        return endpointDataReferenceStorage.remove(contractAgreementId);
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
