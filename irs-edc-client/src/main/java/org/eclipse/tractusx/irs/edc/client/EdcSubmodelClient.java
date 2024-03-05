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

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;

/**
 * Public API facade for EDC domain
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.UseObjectForClearerAPI"})
public interface EdcSubmodelClient {

    CompletableFuture<SubmodelDescriptor> getSubmodelPayload(String connectorEndpoint, String submodelDataplaneUrl,
            String assetId, String bpn) throws EdcClientException;

    CompletableFuture<EdcNotificationResponse> sendNotification(String submodelEndpointAddress, String assetId,
            EdcNotification<NotificationContent> notification, String bpn) throws EdcClientException;

    List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(String endpointAddress,
            String filterKey, String filterValue, String bpn) throws EdcClientException;

    List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(String endpointAddress,
            String filterKey, String filterValue, EndpointDataReferenceStatus cachedEndpointDataReference, String bpn)
            throws EdcClientException;
}

