/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
import static org.eclipse.tractusx.irs.edc.client.util.UrlValidator.isValidUrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.eclipse.tractusx.irs.edc.client.util.UriPathJoiner;
import org.springframework.util.StopWatch;

/**
 * Public API facade for EDC domain
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.UseObjectForClearerAPI"
})
public class EdcSubmodelClientImpl implements EdcSubmodelClient {

    private static final String DT_DCAT_TYPE_ID = "'" + JsonLdConfiguration.NAMESPACE_DCT + "type'.'@id'";
    private static final String DT_TAXONOMY_REGISTRY =
            JsonLdConfiguration.NAMESPACE_CX_TAXONOMY + "DigitalTwinRegistry";
    private static final String DT_EDC_TYPE = JsonLdConfiguration.NAMESPACE_EDC + "type";
    private static final String DT_DATA_CORE_REGISTRY = "data.core.digitalTwinRegistry";

    private final EdcConfiguration config;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EdcOrchestrator edcOrchestrator;
    private final RetryRegistry retryRegistry;

    private static void stopWatchOnEdcTask(final StopWatch stopWatch) {
        stopWatch.stop();
        log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }

    @Override
    public CompletableFuture<SubmodelDescriptor> getSubmodelPayload(final String connectorEndpoint,
            final String submodelDataplaneUrl, final String assetId, final String bpn) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for raw payload, endpoint " + connectorEndpoint);

        final String dspEndpointAddress = appendSuffix(connectorEndpoint, config.getControlplane().getProviderSuffix());

        final CompletableFuture<EndpointDataReference> endpointDataReference = execute(dspEndpointAddress,
                () -> edcOrchestrator.getEndpointDataReference(dspEndpointAddress, assetId, bpn, Optional.empty()));

        return execute(dspEndpointAddress, () -> endpointDataReference.thenApply(futureEdr -> {
            log.info("Retrieving data from EDC data plane for dataReference with id {}", futureEdr.getId());
            final String payload = edcDataPlaneClient.getData(futureEdr, submodelDataplaneUrl);
            stopWatchOnEdcTask(stopWatch);

            return new SubmodelDescriptor(futureEdr.getContractId(), payload);
        }));
    }

    @Override
    public CompletableFuture<EdcNotificationResponse> sendNotification(final String connectorEndpoint,
            final String assetId, final EdcNotification<NotificationContent> notification, final String bpn)
            throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Send EDC notification task, endpoint " + connectorEndpoint);

        final String dspEndpointAddress = appendSuffix(connectorEndpoint, config.getControlplane().getProviderSuffix());

        final CatalogItem catalogItem = execute(dspEndpointAddress,
                () -> edcOrchestrator.getCatalogItem(dspEndpointAddress, assetId, bpn));

        final CompletableFuture<EndpointDataReference> endpointDataReference = execute(dspEndpointAddress,
                () -> edcOrchestrator.getEndpointDataReference(dspEndpointAddress, catalogItem));

        return execute(dspEndpointAddress, () -> endpointDataReference.thenApply(futureEdr -> {
            log.info("Sending dataReference to EDC data plane for assetId '{}'", assetId);
            final EdcNotificationResponse response = edcDataPlaneClient.sendData(futureEdr, notification);
            stopWatchOnEdcTask(stopWatch);
            return response;
        }));
    }

    @Override
    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(final String endpointAddress,
            final String filterKey, final String filterValue, final String bpn) throws EdcClientException {
        return getEndpointReferencesForAsset(endpointAddress, filterKey, filterValue,
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW), bpn);
    }

    @Override
    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(final String endpointAddress,
            final String filterKey, final String filterValue,
            final EndpointDataReferenceStatus endpointDataReferenceStatus, final String bpn) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EDC Submodel task for shell descriptor, endpoint " + endpointAddress);

        final String dspEndpointAddress = appendSuffix(endpointAddress, config.getControlplane().getProviderSuffix());

        // CatalogItem = contract offer
        final List<CatalogItem> contractOffers = execute(dspEndpointAddress,
                () -> edcOrchestrator.getCatalogItems(dspEndpointAddress, filterKey, filterValue, bpn));

        if (contractOffers.isEmpty()) {
            throw new EdcClientException(
                    "Catalog is empty for endpointAddress '%s' filterKey '%s', filterValue '%s'".formatted(
                            endpointAddress, filterKey, filterValue));
        }

        return execute(dspEndpointAddress,
                () -> edcOrchestrator.getEndpointDataReferences(dspEndpointAddress, contractOffers));
    }

    @Override
    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForRegistryAsset(
            final String endpointAddress, final String bpn) throws EdcClientException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("Get EndpointDataReference task for shell descriptor, endpoint " + endpointAddress);

        final String dspEndpointAddress = appendSuffix(endpointAddress, config.getControlplane().getProviderSuffix());

        final List<CatalogItem> contractOffers = new ArrayList<>(execute(dspEndpointAddress,
                () -> edcOrchestrator.getCatalogItems(dspEndpointAddress, DT_DCAT_TYPE_ID, DT_TAXONOMY_REGISTRY, bpn)));

        if (contractOffers.isEmpty()) {
            log.info("No contract offers found for type '" + DT_TAXONOMY_REGISTRY + "'. Using fallback type '"
                    + DT_DATA_CORE_REGISTRY + "'.");
            final List<CatalogItem> contractOffersDataCore = execute(dspEndpointAddress,
                    () -> edcOrchestrator.getCatalogItems(dspEndpointAddress, DT_EDC_TYPE, DT_DATA_CORE_REGISTRY, bpn));
            contractOffers.addAll(contractOffersDataCore);
        }

        if (contractOffers.isEmpty()) {
            throw new EdcClientException(
                    "No DigitalTwinRegistry contract offers found for endpointAddress '%s' filterKey '%s', filterValue '%s' or filterKey '%s', filterValue '%s'".formatted(
                            endpointAddress, DT_DCAT_TYPE_ID, DT_TAXONOMY_REGISTRY, DT_EDC_TYPE,
                            DT_DATA_CORE_REGISTRY));
        }

        return execute(dspEndpointAddress,
                () -> edcOrchestrator.getEndpointDataReferences(dspEndpointAddress, contractOffers));
    }

    private String appendSuffix(final String endpointAddress, final String providerSuffix) throws EdcClientException {
        String addressWithSuffix;
        if (endpointAddress.endsWith(providerSuffix)) {
            addressWithSuffix = endpointAddress;
        } else {
            try {
                addressWithSuffix = UriPathJoiner.appendPath(endpointAddress, providerSuffix);
            } catch (URISyntaxException e) {
                throw new EdcClientException(e);
            }
        }
        return addressWithSuffix;
    }

    @SuppressWarnings({ "PMD.AvoidRethrowingException",
                        "PMD.AvoidCatchingGenericException"
    })
    private <T> T execute(final String endpointAddress, final CheckedSupplier<T> supplier) throws EdcClientException {
        if (!isValidUrl(endpointAddress)) {
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
