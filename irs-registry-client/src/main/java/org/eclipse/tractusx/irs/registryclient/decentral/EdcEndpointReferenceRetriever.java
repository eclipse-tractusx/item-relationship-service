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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;

/**
 * Interface for EDC endpoint reference retrieval
 */
public interface EdcEndpointReferenceRetriever {

    /**
     * Retrieves the EDC endpoint references from the specified connector endpoint and asset combination
     *
     * @param edcConnectorEndpoint the endpoint URL
     * @param assetType            the asset type id
     * @param assetValue           the asset type value
     * @return the endpoint data references as {@link List<CompletableFuture<EndpointDataReference>>}
     * @throws EdcRetrieverException on any EDC errors
     */
    List<CompletableFuture<EndpointDataReference>> getEndpointReferencesForAsset(String edcConnectorEndpoint,
            String assetType, String assetValue) throws EdcRetrieverException;
}
