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

import java.util.List;

import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
/**
 * The {@code TraceXEdcAPI} interface defines the contract for interacting with the EDC from trace-x.
 * It includes methods to fetch catalog items and other EDC-related operations.
 *
 * @see CatalogItem
 * @see CatalogRequest
 */
public interface TraceXEdcAPI {

    /**
     * Fetches a list of {@link CatalogItem} objects based on the given {@link CatalogRequest}.
     * <p>
     * This method communicates with the control plane client to retrieve the catalog based on the request,
     * and then maps the result to a list of catalog items.
     *
     * @param catalogRequest The request containing the parameters needed to fetch the catalog.
     * @return A list of {@link CatalogItem} objects representing the items in the catalog.
     */
    List<CatalogItem> fetchCatalogItems(CatalogRequest catalogRequest);
}
