/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.edc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.edc.exceptions.EdcClientException;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EdcSubmodelFacade {

    public static final String EDC_REST_TEMPLATE = "edcRestTemplate";

    private final EdcSubmodelClient client;

    @SuppressWarnings("PMD.PreserveStackTrace")
    public List<Relationship> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType) throws EdcClientException {
        try {
            return client.getRelationships(submodelEndpointAddress, traversalAspectType).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof EdcClientException exceptionCause) {
                throw exceptionCause;
            }
            throw new EdcClientException(cause);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public String getSubmodelRawPayload(final String submodelEndpointAddress) throws EdcClientException {
        try {
            return client.getSubmodelRawPayload(submodelEndpointAddress).get();
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
    public EdcNotificationResponse sendNotification(final String submodelEndpointAddress,
            final EdcNotification notification) throws EdcClientException {
        try {
            return client.sendNotification(submodelEndpointAddress, notification).get();
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
