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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client;

import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@Service("irsEdcClientEdcSubmodelFacade")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class EdcSubmodelFacade {

    private final EdcSubmodelClient client;

    @SuppressWarnings("PMD.PreserveStackTrace")
    public String getSubmodelRawPayload(final String connectorEndpoint, final String submodelSufix,
            final String assetId) throws EdcClientException {
        try {
            return client.getSubmodelRawPayload(connectorEndpoint, submodelSufix, assetId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public EdcNotificationResponse sendNotification(final String submodelEndpointAddress, final String assetId,
            final EdcNotification notification) throws EdcClientException {
        try {
            return client.sendNotification(submodelEndpointAddress, assetId, notification).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public EndpointDataReference getEndpointReferenceForAsset(final String endpointAddress, final String filterKey,
            final String filterValue) throws EdcClientException {
        try {
            return client.getEndpointReferenceForAsset(endpointAddress, filterKey, filterValue).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        }
    }

}
