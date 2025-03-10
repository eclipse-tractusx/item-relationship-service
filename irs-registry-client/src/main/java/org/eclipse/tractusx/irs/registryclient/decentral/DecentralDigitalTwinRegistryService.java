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
package org.eclipse.tractusx.irs.registryclient.decentral;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.resilience4j.core.functions.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.common.ExceptionUtils;
import org.eclipse.tractusx.irs.common.util.concurrent.ResultFinder;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.PreferredConnectorEndpointsCache;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.registryclient.exceptions.ShellNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StopWatch;

/**
 * Decentral implementation of DigitalTwinRegistryService
 */
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({ "PMD.TooManyMethods",
                    "PMD.ExcessiveImports"
})
public class DecentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private static final String TOOK_MS = "{} took {} ms";

    private final ConnectorEndpointsService connectorEndpointsService;
    private final EndpointDataForConnectorsService endpointDataForConnectorsService;
    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient;
    private final EdcConfiguration config;
    private final PreferredConnectorEndpointsCache preferredConnectorEndpointsCache;

    private ResultFinder resultFinder = new ResultFinder();

    private static Stream<Map.Entry<String, List<DigitalTwinRegistryKey>>> groupKeysByBpn(
            final Collection<DigitalTwinRegistryKey> keys) {
        return keys.stream().collect(Collectors.groupingBy(DigitalTwinRegistryKey::bpn)).entrySet().stream();
    }

    /**
     * Package private setter in order to allow simulating {@link InterruptedException}
     * and {@link ExecutionException} in tests.
     *
     * @param resultFinder the {@link ResultFinder}
     */
    /* package */ void setResultFinder(final ResultFinder resultFinder) {
        this.resultFinder = resultFinder;
    }

    @Override
    public Collection<Either<Exception, Shell>> fetchShells(final Collection<DigitalTwinRegistryKey> keys)
            throws RegistryServiceException {

        if (!config.isCacheEdcUrls()) {
            return fetchShellsOnCacheMiss(keys);
        }

        final Collection<Either<Exception, Shell>> results = keys.stream()
                                                                 .map(this::fetchShellForKey)
                                                                 .flatMap(Collection::stream)
                                                                 .toList();

        return results.isEmpty() || results.stream().anyMatch(Either::isLeft)
                ? fetchShellsOnCacheMiss(keys)
                : results;
    }

    private Collection<Either<Exception, Shell>> fetchShellForKey(final DigitalTwinRegistryKey key) {
        return preferredConnectorEndpointsCache.findByBpn(key.bpn())
                                               .map(edcUrl -> fetchShellFromEdcUrl(edcUrl, key))
                                               .orElseGet(() -> {
                                                   log.info("Cached EDC url for BPN: {} not found", key.bpn());
                                                   return Collections.emptyList();
                                               });
    }

    private Collection<Either<Exception, Shell>> fetchShellFromEdcUrl(final String edcUrl, final DigitalTwinRegistryKey key) {
        return endpointDataForConnectorsService.createGetEndpointReferencesForAssetFutures(edcUrl, key.bpn())
                                               .stream()
                                               .map(future -> future.handle((edr, ex) -> {
                                                   if (ex != null) {
                                                       return Either.<Exception, Shell>left(new Exception(ex instanceof ExecutionException ? ex.getCause() : ex));
                                                   }
                                                   try {
                                                       return Either.<Exception, Shell>right(new Shell(edr.getContractId(),
                                                                   fetchShellDescriptor(edr, key)));
                                                   } catch (RegistryServiceException e) {
                                                       return Either.<Exception, Shell>left(e);
                                                   }
                                               }))
                                               .map(CompletableFuture::join)
                                               .toList();
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Collection<Either<Exception, Shell>> fetchShellsOnCacheMiss(final Collection<DigitalTwinRegistryKey> keys)
            throws RegistryServiceException {

        final var watch = new StopWatch();
        final String msg = "Fetching shell(s) for %s key(s)".formatted(keys.size());
        watch.start(msg);
        log.info(msg);

        try {
            final var calledEndpoints = new HashSet<String>();

            final List<Either<Exception, Shell>> collectedShells = groupKeysByBpn(keys).flatMap(entry -> {

                try {
                    return fetchShellDescriptors(entry, calledEndpoints);
                } catch (TimeoutException | RuntimeException e) {
                    // catching generic exception is intended here,
                    // otherwise Jobs stay in state RUNNING forever
                    log.warn(e.getMessage(), e);
                    return Stream.of(Either.<Exception, Shell>left(e));
                }

            }).toList();

            if (collectedShells.stream().noneMatch(Either::isRight)) {
                log.info("No shells found");

                final ShellNotFoundException shellNotFoundException = new ShellNotFoundException(
                        "Unable to find any of the requested shells", calledEndpoints);
                ExceptionUtils.addSuppressedExceptions(collectedShells, shellNotFoundException);
                throw shellNotFoundException;
            } else {
                log.info("Found {} shell(s) for {} key(s)", collectedShells.size(), keys.size());
                return collectedShells;
            }

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    @Override
    public Optional<Shell> fetchShell(final PartChainIdentificationKey key) throws RegistryServiceException {
        if (StringUtils.isBlank(key.getGlobalAssetId())) {
            final List<String> edcUrls = connectorEndpointsService.fetchConnectorEndpoints(key.getBpn());

            return endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(edcUrls, key.getBpn())
                                                   .stream()
                                                   .map(edrFuture -> {
                                                       try {
                                                           final EndpointDataReference edr = edrFuture.get();
                                                           return new Shell(edr.getContractId(),
                                                                   decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                                                                           edr, key.getIdentifier()));
                                                       } catch (InterruptedException e) {
                                                           log.info("Unable to find any of the requested shells, reason: {}", e.getMessage());
                                                           Thread.currentThread().interrupt();
                                                       } catch (ExecutionException e) {
                                                           log.info("Unable to find any of the requested shells, reason: {}", e.getMessage());
                                                       }
                                                       return null;
                                                   })
                                                   .filter(Objects::nonNull)
                                                   .findFirst();
        }

        return fetchShells(List.of(new DigitalTwinRegistryKey(key.getGlobalAssetId(), key.getBpn()))).stream()
                                                                                                     .map(Either::getOrNull)
                                                                                                     .filter(Objects::nonNull)
                                                                                                     .findFirst();
    }

    private Stream<Either<Exception, Shell>> fetchShellDescriptors(
            final Map.Entry<String, List<DigitalTwinRegistryKey>> entry, final Set<String> calledEndpoints)
            throws TimeoutException {

        try {

            final var futures = fetchShellDescriptors(calledEndpoints, entry.getKey(), entry.getValue());
            final var shellDescriptors = futures.get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
            return shellDescriptors.stream();

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            return Stream.of(Either.left(e));
        } catch (ExecutionException | RegistryServiceException e) {
            log.warn(e.getMessage(), e);
            return Stream.of(Either.left(e));
        }
    }

    private CompletableFuture<List<Either<Exception, Shell>>> fetchShellDescriptors(final Set<String> calledEndpoints,
            final String bpn, final List<DigitalTwinRegistryKey> keys) throws RegistryServiceException {

        final var watch = new StopWatch();
        final String msg = "Fetching %s shells for bpn '%s'".formatted(keys.size(), bpn);
        watch.start(msg);
        log.info(msg);

        try {
            final var edcUrls = connectorEndpointsService.fetchConnectorEndpoints(bpn);
            if (edcUrls.isEmpty()) {
                throw new RegistryServiceException("No EDC Endpoints could be discovered for BPN '%s'".formatted(bpn));
            }

            log.info("Found {} connector endpoints for bpn '{}'", edcUrls.size(), bpn);
            calledEndpoints.addAll(edcUrls);

            return fetchShellDescriptorsForConnectorEndpoints(keys, edcUrls, bpn);

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    private CompletableFuture<List<Either<Exception, Shell>>> fetchShellDescriptorsForConnectorEndpoints(
            final List<DigitalTwinRegistryKey> keys, final List<String> edcUrls, final String bpn) {

        final var shellsFuture = endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(edcUrls,
                                                                         bpn)
                                                                 .stream()
                                                                 .map(edrFuture -> edrFuture.thenCompose(
                                                                         edr -> CompletableFuture.supplyAsync(
                                                                                 () -> fetchShellDescriptorsForKey(keys,
                                                                                         edr))))
                                                                 .toList();

        log.debug("Created {} futures", shellsFuture.size());

        return resultFinder.getFastestResult(shellsFuture);
    }

    private List<Either<Exception, Shell>> fetchShellDescriptorsForKey(final List<DigitalTwinRegistryKey> keys,
            final EndpointDataReference endpointDataReference) {

        final var watch = new StopWatch();
        final String msg = "Fetching shell descriptors for keys %s from endpoint '%s'".formatted(keys,
                endpointDataReference.getEndpoint());
        watch.start(msg);
        log.info(msg);

        try {
            return keys.stream()
                       .map(key -> {
                           try {
                               return Either.<Exception, Shell>right(
                                       new Shell(endpointDataReference.getContractId(),
                                               fetchShellDescriptor(endpointDataReference, key)));
                           } catch (RegistryServiceException e) {
                               return Either.<Exception, Shell>left(e);
                           }
                       })
                       .toList();
        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    private AssetAdministrationShellDescriptor fetchShellDescriptor(final EndpointDataReference endpointDataReference,
            final DigitalTwinRegistryKey key) throws RegistryServiceException {

        final var watch = new StopWatch();
        final String msg = "Retrieving AAS identification for DigitalTwinRegistryKey: '%s'".formatted(key);
        watch.start(msg);
        log.info(msg);
        try {
            final String aaShellIdentification = mapToShellId(endpointDataReference, key.shellId());
            return decentralDigitalTwinRegistryClient.getAssetAdministrationShellDescriptor(endpointDataReference,
                    aaShellIdentification);
        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    /**
     * This method takes the provided globalAssetId and maps it to the corresponding asset administration shell ID.
     *
     * @param endpointDataReference the reference to access the digital twin registry
     * @param globalAssetId         globalAssetId
     * @throws RegistryServiceException if the mapping does not return any results
     * @return the corresponding asset administration shell ID
     */
    @NotNull
    private String mapToShellId(final EndpointDataReference endpointDataReference, final String globalAssetId)
            throws RegistryServiceException {

        final var watch = new StopWatch();
        final String msg = "Mapping '%s' to shell ID for endpoint '%s'".formatted(globalAssetId,
                endpointDataReference.getEndpoint());
        watch.start(msg);
        log.info(msg);

        try {

            final var identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                     .name("globalAssetId")
                                                                     .value(globalAssetId)
                                                                     .build();

            // Try to map the provided ID to the corresponding asset administration shell ID
            final var mappingResultStream = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                    endpointDataReference, identifierKeyValuePair).getResult().stream();

            // Special scenario: Multiple DTs with the same globalAssetId in one DTR, see:
            // docs/arc42/cross-cutting/discovery-DTR--multiple-DTs-with-the-same-globalAssedId-in-one-DTR.puml
            final var mappingResult = mappingResultStream.findFirst();

            mappingResult.ifPresent(s -> log.info("Retrieved shellId {} for globalAssetId {}", s, globalAssetId));

            return mappingResult.orElseThrow(() -> new RegistryServiceException(
                    "Unable to find shell ID for global asset ID '%s'".formatted(globalAssetId)));

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Collection<String> lookupShellIds(final String bpn) throws RegistryServiceException {

        log.info("Looking up shell ids for bpn {}", bpn);

        try {

            final var edcUrls = connectorEndpointsService.fetchConnectorEndpoints(bpn);
            log.info("Looking up shell ids for bpn '{}' with connector endpoints {}", bpn, edcUrls);

            final var endpointDataReferenceFutures = endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(
                    edcUrls, bpn);

            return lookupShellIds(bpn, endpointDataReferenceFutures);

        } catch (RuntimeException e) {
            // catching generic exception is intended here,
            // otherwise Jobs stay in state RUNNING forever
            log.error(e.getMessage(), e);
            throw new RegistryServiceException(
                    "%s occurred while looking up shell ids for bpn '%s'".formatted(e.getClass().getSimpleName(), bpn),
                    e);
        }
    }

    @NotNull
    private Collection<String> lookupShellIds(final String bpn,
            final List<CompletableFuture<EndpointDataReference>> endpointDataReferenceFutures)
            throws RegistryServiceException {

        try {
            final var futures = endpointDataReferenceFutures.stream()
                                                            .map(edrFuture -> edrFuture.thenCompose(
                                                                    edr -> CompletableFuture.supplyAsync(
                                                                            () -> lookupShellIds(bpn, edr))))
                                                            .toList();
            final var shellIds = resultFinder.getFastestResult(futures)
                                             .get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);

            log.info("Found {} shell id(s) in total", shellIds.size());
            return shellIds;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryServiceException(
                    "%s occurred while looking up shell ids for bpn '%s'".formatted(e.getClass().getSimpleName(), bpn),
                    e);
        } catch (ExecutionException e) {
            throw new RegistryServiceException(
                    "%s occurred while looking up shell ids for bpn '%s'".formatted(e.getClass().getSimpleName(), bpn),
                    e);
        } catch (TimeoutException e) {
            throw new RegistryServiceException("Timeout during shell ID lookup for bpn '%s'".formatted(bpn), e);
        }
    }

    private Collection<String> lookupShellIds(final String bpn, final EndpointDataReference endpointDataReference) {

        final var watch = new StopWatch();
        final String msg = "Looking up shell IDs for bpn '%s' with endpointDataReference '%s'".formatted(bpn,
                endpointDataReference);
        watch.start(msg);
        log.info(msg);

        try {
            return decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                                                             endpointDataReference, IdentifierKeyValuePair.builder().name("manufacturerId").value(bpn).build())
                                                     .getResult();
        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    @Override
    public Collection<DigitalTwinRegistryKey> lookupShellIdentifiers(final String bpn) throws RegistryServiceException {
        return lookupShellIds(bpn).stream().map(id -> new DigitalTwinRegistryKey(id, bpn)).toList();
    }
}
