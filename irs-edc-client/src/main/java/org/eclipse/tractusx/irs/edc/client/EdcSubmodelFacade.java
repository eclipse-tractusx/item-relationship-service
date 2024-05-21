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
package org.eclipse.tractusx.irs.edc.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;

/**
 * Public API Facade for submodel domain
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.UseObjectForClearerAPI"
})
public class EdcSubmodelFacade {

    private final EdcSubmodelClient client;

    private final EdcConfiguration config;

    @SuppressWarnings("PMD.PreserveStackTrace")
    public SubmodelDescriptor getSubmodelPayload(final String connectorEndpoint, final String submodelDataplaneUrl,
            final String assetId, final String bpn) throws EdcClientException {
        try {
            return client.getSubmodelPayload(connectorEndpoint, submodelDataplaneUrl, assetId, bpn)
                         .get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.debug("InterruptedException occurred.", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            log.debug("ExecutionException occurred.", e);
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        } catch (TimeoutException e) {
            throw new EdcClientException("Timeout while getting submodel payload", e);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public EdcNotificationResponse sendNotification(final String submodelEndpointAddress, final String assetId,
            final EdcNotification<NotificationContent> notification, final String bpn) throws EdcClientException {
        try {
            log.debug("Sending EDC Notification '{}'", notification);
            return client.sendNotification(submodelEndpointAddress, assetId, notification, bpn)
                         .get(config.getAsyncTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        } catch (TimeoutException e) {
            throw new EdcClientException("Timeout while sending notification", e);
        }
    }

    public List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForRegistryAsset(
            final String endpointAddress, final String bpn) throws EdcClientException {
        return client.getEndpointReferencesForRegistryAsset(endpointAddress, bpn);
    }

}
