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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;

/**
 * Submodel facade stub used in local environment
 */
public class EdcSubmodelClientLocalStub implements EdcSubmodelClient {

    private final SubmodelTestdataCreator testdataCreator;

    /* package */
    public EdcSubmodelClientLocalStub(final CxTestDataContainer cxTestDataContainer) {
        this.testdataCreator = new SubmodelTestdataCreator(cxTestDataContainer);
    }

    @Override
    public CompletableFuture<SubmodelDescriptor> getSubmodelPayload(final String connectorEndpoint,
            final String submodelDataplaneUrl, final String assetId) throws EdcClientException {
        if ("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(assetId)) {
            throw new EdcClientException("Dummy Exception");
        }
        final Map<String, Object> submodel = testdataCreator.createSubmodelForId(assetId + "_" + submodelDataplaneUrl);
        return CompletableFuture.completedFuture(new SubmodelDescriptor(UUID.randomUUID().toString(),
                StringMapper.mapToString(submodel)));
    }

    @Override
    public CompletableFuture<EdcNotificationResponse> sendNotification(final String submodelEndpointAddress,
            final String assetId, final EdcNotification<NotificationContent> notification) {
        // not actually sending anything, just return success response
        return CompletableFuture.completedFuture(() -> true);
    }

    @Override
    public CompletableFuture<EndpointDataReference> getEndpointReferenceForAsset(final String endpointAddress,
            final String filterKey, final String filterValue,
            final EndpointDataReferenceStatus cachedEndpointDataReference) throws EdcClientException {
        throw new EdcClientException("Not implemented");
    }

    @Override
    public CompletableFuture<EndpointDataReference> getEndpointReferenceForAsset(final String endpointAddress,
            final String filterKey, final String filterValue) throws EdcClientException {
        throw new EdcClientException("Not implemented");
    }
}
