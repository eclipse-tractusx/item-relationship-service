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
package org.eclipse.tractusx.irs.registryclient.decentral;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;

/**
 * Service that use edc client to make calls to edc connector endpoints
 * to find DigitalTwinRegistry asset
 */
@Slf4j
@RequiredArgsConstructor
public class EndpointDataForConnectorsService {

    private static final String DT_REGISTRY_ASSET_TYPE = "https://w3id.org/edc/v0.0.1/ns/type";
    private static final String DT_REGISTRY_ASSET_VALUE = "data.core.digitalTwinRegistry";

    private final EdcEndpointReferenceRetriever edcSubmodelFacade;

    public List<CompletableFuture<EndpointDataReference>> findEndpointDataForConnectors(
            final List<String> connectorEndpoints) {

        return connectorEndpoints.stream()
                                 .map(connectorEndpoint -> CompletableFuture.supplyAsync(
                                         () -> getEndpointReferenceForAsset(connectorEndpoint)))
                                 .toList();
    }

    private EndpointDataReference getEndpointReferenceForAsset(final String connector) {
        log.info("Trying to retrieve EndpointDataReference for connector {}", connector);
        try {
            return edcSubmodelFacade.getEndpointReferenceForAsset(connector, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE);
        } catch (EdcRetrieverException e) {
            log.warn("Exception occurred when retrieving EndpointDataReference from connector {}", connector, e);
            throw new CompletionException(e.getMessage(), e);
        }

    }

}
