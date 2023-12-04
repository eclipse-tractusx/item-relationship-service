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
package org.eclipse.tractusx.irs.edc.client.util;

import java.time.Instant;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.springframework.stereotype.Service;

/**
 * Cache service to check if there is
 * {@link org.eclipse.edc.spi.types.domain.edr.EndpointDataReference} stored.
 */
@Service
@AllArgsConstructor
@Slf4j
public class EndpointDataReferenceCacheService {

    private final EndpointDataReferenceStorage endpointDataReferenceStorage;

    /**
     * Returns {@link org.eclipse.edc.spi.types.domain.edr.EndpointDataReference}
     * for assetId from {@link org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage}
     * @param assetId key for
     * {@link org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage}
     * @return {@link org.eclipse.edc.spi.types.domain.edr.EndpointDataReference}
     * and {@link org.eclipse.tractusx.irs.edc.client.util.EndpointDataReferenceStatus.TokenStatus}
     * describing token status
     */
    public EndpointDataReferenceStatus getEndpointDataReference(final String assetId) {
        final Optional<EndpointDataReference> endpointDataReferenceOptional = retrieveEndpointEndpointReferenceByAssetId(
                assetId);

        if (endpointDataReferenceOptional.isPresent() && endpointDataReferenceOptional.get().getAuthCode() != null) {
            final EndpointDataReference endpointDataReference = endpointDataReferenceOptional.get();
            if (isTokenExpired(endpointDataReference)) {
                log.info("Endpoint data reference with expired token and id: {} for assetId: {} found in storage.", endpointDataReference.getId(), assetId);
                return new EndpointDataReferenceStatus(endpointDataReference, EndpointDataReferenceStatus.TokenStatus.EXPIRED);
            } else {
                log.info("Endpoint data reference with id: {} for assetId: {} found in storage.", endpointDataReference.getId(), assetId);
                return new EndpointDataReferenceStatus(endpointDataReference, EndpointDataReferenceStatus.TokenStatus.VALID);
            }
        }

        log.info("Endpoint data reference for asset id: {} not found in storage.", assetId);
        return new EndpointDataReferenceStatus(null, EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW);
    }

    private Optional<EndpointDataReference> retrieveEndpointEndpointReferenceByAssetId(final String assetId) {
        log.info("Retrieving dataReference from storage for assetId {}", assetId);
        return endpointDataReferenceStorage.get(assetId);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static boolean isTokenExpired(final EndpointDataReference endpointDataReference) {
        final Instant tokenExpirationInstant = extractTokenExpiration(endpointDataReference.getAuthCode());
        return Instant.now().isAfter(tokenExpirationInstant);
    }

    private static Instant extractTokenExpiration(final String token) {
        return Instant.ofEpochSecond(EDRAuthCode.fromAuthCodeToken(token).getExp());
    }
}


