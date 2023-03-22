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
package org.eclipse.tractusx.irs.edc;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.model.CatalogItem;
import org.springframework.stereotype.Component;

/**
 * EDC Catalog facade which handles pagination of the catalog, aggregation of contract offers
 * and transformation into {@link CatalogItem}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EDCCatalogFacade {

    private final EdcControlPlaneClient controlPlaneClient;
    private final EdcConfiguration config;

    public List<CatalogItem> getCatalog(final String connectorUrl, final String target) {
        int offset = 0;
        final int pageSize = config.getControlplane().getCatalogPageSize();

        log.info("Get catalog from EDC provider.");
        final Catalog pageableCatalog = controlPlaneClient.getCatalog(connectorUrl, offset);
        final List<ContractOffer> contractOffers = new ArrayList<>(pageableCatalog.getContractOffers());

        boolean isLastPage = pageableCatalog.getContractOffers().size() < pageSize;
        boolean isTheSamePage = false;
        Optional<ContractOffer> optionalContractOffer = findOfferIfExist(target, pageableCatalog);

        while (!isLastPage && !isTheSamePage && optionalContractOffer.isEmpty()) {
            offset += pageSize;
            final Catalog newPageableCatalog = controlPlaneClient.getCatalog(connectorUrl, offset);
            isTheSamePage = theSameCatalog(pageableCatalog, newPageableCatalog);
            isLastPage = newPageableCatalog.getContractOffers().size() < pageSize;
            optionalContractOffer = findOfferIfExist(target, newPageableCatalog);

            if (!isTheSamePage) {
                contractOffers.addAll(newPageableCatalog.getContractOffers());
            }
        }

        final String connectorId = pageableCatalog.getId();

        log.info("Search for offer for asset id: {}", target);

        return contractOffers.stream()
                             .map(contractOffer -> CatalogItem.builder()
                                                              .itemId(contractOffer.getId())
                                                              .assetPropId(contractOffer.getAsset().getId())
                                                              .connectorId(connectorId)
                                                              .policy(contractOffer.getPolicy())
                                                              .build())
                             .toList();
    }

    private Optional<ContractOffer> findOfferIfExist(final String target, final Catalog catalog) {
        return catalog.getContractOffers()
                      .stream()
                      .filter(contractOffer -> contractOffer.getAsset().getId().equals(target))
                      .findFirst();
    }

    private boolean theSameCatalog(final Catalog pageableCatalog, final Catalog newPageableCatalog) {
        final Set<String> previousOffers = pageableCatalog.getContractOffers()
                                                          .stream()
                                                          .map(ContractOffer::getId)
                                                          .collect(toSet());
        final Set<String> nextOffers = newPageableCatalog.getContractOffers()
                                                         .stream()
                                                         .map(ContractOffer::getId)
                                                         .collect(toSet());
        return previousOffers.equals(nextOffers);
    }
}
