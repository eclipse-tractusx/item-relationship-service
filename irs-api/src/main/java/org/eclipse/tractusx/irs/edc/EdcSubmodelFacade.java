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
package org.eclipse.tractusx.irs.edc;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EdcSubmodelFacade {

    private final OutboundMeterRegistryService meterRegistryService;
    private final RetryRegistry retryRegistry;
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
    private final EdcSubmodelClient facade;

    public List<Relationship> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType) throws EdcClientException {
        return execute(submodelEndpointAddress, () -> {
            try {
                return facade.getRelationships(submodelEndpointAddress, traversalAspectType).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof EdcClientException) {
                    throw (EdcClientException) e.getCause();
                }
                throw new EdcClientException(e.getCause());
            }
        });
    }

    public String getSubmodelRawPayload(final String submodelEndpointAddress) throws EdcClientException {
        return execute(submodelEndpointAddress, () -> {
            try {
                return facade.getSubmodelRawPayload(submodelEndpointAddress).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                throw new EdcClientException(e.getCause());
            }
        });
    }

    private <T> T execute(final String endpointAddress, final CheckedSupplier<T> supplier) throws EdcClientException {
        if (!urlValidator.isValid(endpointAddress)) {
            throw new IllegalArgumentException(String.format("Malformed endpoint address '%s'", endpointAddress));
        }
        final String host = URI.create(endpointAddress).getHost();
        final Retry retry = retryRegistry.retry(host, "default");
        try {
            return Retry.decorateCallable(retry, () -> {
                try {
                    return supplier.get();
                } catch (ResourceAccessException e) {
                    if (e.getCause() instanceof SocketTimeoutException) {
                        meterRegistryService.incrementSubmodelTimeoutCounter(endpointAddress);
                    }
                    throw e;
                }
            }).call();
        } catch (EdcClientException e) {
            throw e;
        } catch (Exception e) {
            throw new EdcClientException(e);
        }
    }

    private interface CheckedSupplier<T> {
        T get() throws EdcClientException;
    }
}
