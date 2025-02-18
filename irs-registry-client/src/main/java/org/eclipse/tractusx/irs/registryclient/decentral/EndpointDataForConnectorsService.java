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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.PreferredConnectorEndpointsCache;
import org.springframework.util.StopWatch;

/**
 * Service that use edc client to make calls to edc connector endpoints
 * to find DigitalTwinRegistry asset
 */
@Slf4j
@RequiredArgsConstructor
public class EndpointDataForConnectorsService {

    private static final String TOOK_MS = "{} took {} ms";

    private final EdcEndpointReferenceRetriever edcSubmodelFacade;

    private final PreferredConnectorEndpointsCache preferredConnectorEndpointsCache;

    public List<CompletableFuture<EndpointDataReference>> createFindEndpointDataForConnectorsFutures(
            final List<String> edcUrls, final String bpn) {

        final var watch = new StopWatch();
        final String msg = "Creating futures to get EndpointDataReferences for endpoints: %s".formatted(edcUrls);
        watch.start(msg);
        log.info(msg);

        List<CompletableFuture<EndpointDataReference>> futures = Collections.emptyList();
        try {
            futures = edcUrls.stream()
                             .flatMap(edcUrl -> createGetEndpointReferencesForAssetFutures(edcUrl, bpn).stream())
                             .toList();
            return futures;
        } finally {
            log.info("Created {} futures", futures.size());
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }
    }

    public List<CompletableFuture<EndpointDataReference>> createGetEndpointReferencesForAssetFutures(
            final String edcUrl, final String bpn) {

        final var watch = new StopWatch();
        final String msg = "Trying to retrieve EndpointDataReference for connector '%s'".formatted(edcUrl);
        watch.start(msg);
        log.info(msg);

        try {
            final List<CompletableFuture<EndpointDataReference>> endpointReferencesForAsset = edcSubmodelFacade.getEndpointReferencesForAsset(
                    edcUrl, bpn);
            preferredConnectorEndpointsCache.store(bpn, edcUrl);
            return endpointReferencesForAsset;
        } catch (EdcRetrieverException e) {
            log.warn("Exception occurred when retrieving EndpointDataReference from connector '{}'", edcUrl, e);
            preferredConnectorEndpointsCache.remove(bpn);
            return List.of(CompletableFuture.failedFuture(
                    new EdcRetrieverException.Builder(e).withBpn(bpn).withEdcUrl(edcUrl).build()));
        } finally {
            watch.stop();
            log.info(TOOK_MS, watch.getLastTaskName(), watch.getLastTaskTimeMillis());
        }

    }

}
