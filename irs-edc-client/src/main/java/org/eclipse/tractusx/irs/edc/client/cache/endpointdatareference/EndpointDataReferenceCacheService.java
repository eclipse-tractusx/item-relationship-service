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
package org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference;

import java.time.Instant;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.storage.EndpointDataReferenceStorage;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus.TokenStatus;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.jetbrains.annotations.NotNull;
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
     * for assetId from {@link EndpointDataReferenceStorage}
     *
     * @param assetId key for
     *                {@link EndpointDataReferenceStorage}
     * @return {@link org.eclipse.edc.spi.types.domain.edr.EndpointDataReference}
     * and {@link EndpointDataReferenceStatus.TokenStatus}
     * describing token status
     */

    public EndpointDataReferenceStatus getEndpointDataReference(final String assetId) {

        log.info("Retrieving dataReference from storage for assetId {}", assetId);
        final Optional<EndpointDataReference> endpointDataReferenceOptional = endpointDataReferenceStorage.get(assetId);

        if (endpointDataReferenceOptional.isPresent()) {
            final String authCode = endpointDataReferenceOptional.get().getAuthCode();
            if (authCode != null) {
                final EndpointDataReference endpointDataReference = endpointDataReferenceOptional.get();
                if (isTokenExpired(authCode)) {
                    log.info("Endpoint data reference with expired token and id: {} for assetId: {} found in storage.",
                            endpointDataReference.getId(), assetId);
                    return new EndpointDataReferenceStatus(endpointDataReference, TokenStatus.EXPIRED);
                } else {
                    log.info("Endpoint data reference with id: {} for assetId: {} found in storage.",
                            endpointDataReference.getId(), assetId);
                    return new EndpointDataReferenceStatus(endpointDataReference, TokenStatus.VALID);
                }
            }
        }

        log.info("Endpoint data reference for asset id: {} not found in storage.", assetId);
        return new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW);
    }

    public Optional<EndpointDataReference> getEndpointDataReferenceFromStorage(final String storageId) {
        return endpointDataReferenceStorage.get(storageId);
    }

    public void putEndpointDataReferenceIntoStorage(final String assetId,
            final EndpointDataReference endpointDataReference) {
        endpointDataReferenceStorage.put(assetId, endpointDataReference);
    }

    private static boolean isTokenExpired(final @NotNull String authCode) {
        final Instant tokenExpirationInstant = extractTokenExpiration(authCode);
        return Instant.now().isAfter(tokenExpirationInstant);
    }

    private static Instant extractTokenExpiration(final String token) {
        return Instant.ofEpochSecond(EDRAuthCode.fromAuthCodeToken(token).getExp());
    }

}


