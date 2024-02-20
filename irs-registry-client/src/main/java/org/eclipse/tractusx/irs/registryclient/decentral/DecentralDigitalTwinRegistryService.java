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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.common.util.concurrent.ResultFinder;
import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
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
@SuppressWarnings("PMD.TooManyMethods")
public class DecentralDigitalTwinRegistryService implements DigitalTwinRegistryService {

    private static final String TOOK_MS = "{} took {} ms";

    private final ConnectorEndpointsService connectorEndpointsService;
    private final EndpointDataForConnectorsService endpointDataForConnectorsService;
    private final DecentralDigitalTwinRegistryClient decentralDigitalTwinRegistryClient;

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
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Collection<Shell> fetchShells(final Collection<DigitalTwinRegistryKey> keys)
            throws RegistryServiceException {

        final var watch = new StopWatch();
        final String msg = "Fetching shell(s) for %s key(s)".formatted(keys.size());
        watch.start(msg);
        log.info(msg);

        try {
            final var calledEndpoints = new HashSet<String>();

            final var collectedShells = groupKeysByBpn(keys).flatMap(entry -> {

                try {
                    return fetchShellDescriptors(entry, calledEndpoints);
                } catch (RuntimeException e) {
                    // catching generic exception is intended here,
                    // otherwise Jobs stay in state RUNNING forever
                    log.warn(e.getMessage(), e);
                    return Stream.empty();
                }

            }).toList();

            if (collectedShells.isEmpty()) {
                log.info("No shells found");
                throw new ShellNotFoundException("Unable to find any of the requested shells", calledEndpoints);
            } else {
                log.info("Found {} shell(s) for {} key(s)", collectedShells.size(), keys.size());
                return collectedShells;
            }

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    private Stream<Shell> fetchShellDescriptors(
            final Map.Entry<String, List<DigitalTwinRegistryKey>> entry, final Set<String> calledEndpoints) {

        try {

            final var futures = fetchShellDescriptors(calledEndpoints, entry.getKey(), entry.getValue());
            final var shellDescriptors = futures.get();
            return shellDescriptors.stream();

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            return Stream.empty();
        } catch (ExecutionException e) {
            log.warn(e.getMessage(), e);
            return Stream.empty();
        }
    }

    private CompletableFuture<List<Shell>> fetchShellDescriptors(
            final Set<String> calledEndpoints, final String bpn, final List<DigitalTwinRegistryKey> keys) {

        final var watch = new StopWatch();
        final String msg = "Fetching %s shells for bpn '%s'".formatted(keys.size(), bpn);
        watch.start(msg);
        log.info(msg);

        try {
            final var connectorEndpoints = connectorEndpointsService.fetchConnectorEndpoints(bpn);

            log.info("Found {} connector endpoints for bpn '{}'", connectorEndpoints.size(), bpn);
            calledEndpoints.addAll(connectorEndpoints);

            return fetchShellDescriptorsForConnectorEndpoints(keys, connectorEndpoints);

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    private CompletableFuture<List<Shell>> fetchShellDescriptorsForConnectorEndpoints(
            final List<DigitalTwinRegistryKey> keys, final List<String> connectorEndpoints) {

        final var service = endpointDataForConnectorsService;
        final var futures = service.createFindEndpointDataForConnectorsFutures(connectorEndpoints)
                                   .stream()
                                   .map(edrFuture -> edrFuture.thenCompose(edr -> CompletableFuture.supplyAsync(
                                           () -> fetchShellDescriptorsForKey(keys, edr))))
                                   .toList();

        log.debug("Created {} futures", futures.size());

        return resultFinder.getFastestResult(futures);
    }

    private List<Shell> fetchShellDescriptorsForKey(
            final List<DigitalTwinRegistryKey> keys, final EndpointDataReference endpointDataReference) {

        final var watch = new StopWatch();
        final String msg = "Fetching shell descriptors for keys %s from endpoint '%s'".formatted(keys,
                endpointDataReference.getEndpoint());
        watch.start(msg);
        log.info(msg);
        try {
            return keys.stream().map(key -> new Shell(contractNegotiationId(endpointDataReference.getAuthCode()),
                    fetchShellDescriptor(endpointDataReference, key))).toList();
        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    private AssetAdministrationShellDescriptor fetchShellDescriptor(final EndpointDataReference endpointDataReference,
            final DigitalTwinRegistryKey key) {

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

    private String contractNegotiationId(final String token) {
        return Optional.ofNullable(token)
                       .map(EDRAuthCode::fromAuthCodeToken)
                       .map(EDRAuthCode::getCid)
                       .orElse("");
    }

    /**
     * This method takes the provided ID and maps it to the corresponding asset administration shell ID.
     * If the ID is already a shellId, the same ID will be returned.
     * If the ID is a globalAssetId, the corresponding shellId will be returned.
     *
     * @param endpointDataReference the reference to access the digital twin registry
     * @param key                   the ambiguous key (shellId or globalAssetId)
     * @return the shellId
     */
    @NotNull
    private String mapToShellId(final EndpointDataReference endpointDataReference, final String key) {

        final var watch = new StopWatch();
        final String msg = "Mapping '%s' to shell ID for endpoint '%s'".formatted(key,
                endpointDataReference.getEndpoint());
        watch.start(msg);
        log.info(msg);

        try {

            final var identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                     .name("globalAssetId")
                                                                     .value(key)
                                                                     .build();
            final var aaShellIdentification = decentralDigitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                                                                                        endpointDataReference, List.of(identifierKeyValuePair))
                                                                                .getResult()
                                                                                .stream()
                                                                                .findFirst()
                                                                                .orElse(key);

            if (key.equals(aaShellIdentification)) {
                log.info("Found shell with shellId {} in registry", aaShellIdentification);
            } else {
                log.info("Retrieved shellId {} for globalAssetId {}", aaShellIdentification, key);
            }

            return aaShellIdentification;

        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Collection<String> lookupShellIds(final String bpn) throws RegistryServiceException {

        log.info("Looking up shell ids for bpn {}", bpn);

        try {

            final var connectorEndpoints = connectorEndpointsService.fetchConnectorEndpoints(bpn);
            log.info("Looking up shell ids for bpn '{}' with connector endpoints {}", bpn, connectorEndpoints);

            final var endpointDataReferenceFutures = endpointDataForConnectorsService.createFindEndpointDataForConnectorsFutures(
                    connectorEndpoints);
            log.debug("Created endpointDataReferenceFutures");

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
            final var shellIds = resultFinder.getFastestResult(futures).get();

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
                    endpointDataReference,
                    List.of(IdentifierKeyValuePair.builder().name("manufacturerId").value(bpn).build())).getResult();
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
