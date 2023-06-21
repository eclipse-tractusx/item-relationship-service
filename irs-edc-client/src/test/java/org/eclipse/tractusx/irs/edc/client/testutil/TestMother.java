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
package org.eclipse.tractusx.irs.edc.client.testutil;

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCAT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DSPACE;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_ID;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_ODRL;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_TRACTUSX;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcObjectMapper;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.jetbrains.annotations.NotNull;

public class TestMother {

    public static EdcTransformer createEdcTransformer() {
        final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        titaniumJsonLd.registerNamespace("odrl", NAMESPACE_ODRL);
        titaniumJsonLd.registerNamespace("dct", NAMESPACE_DCT);
        titaniumJsonLd.registerNamespace("tx", NAMESPACE_TRACTUSX);
        titaniumJsonLd.registerNamespace("edc", NAMESPACE_EDC);
        titaniumJsonLd.registerNamespace("dcat", NAMESPACE_DCAT);
        titaniumJsonLd.registerNamespace("dspace", NAMESPACE_DSPACE);
        return new EdcTransformer(EdcObjectMapper.MAPPER, titaniumJsonLd);
    }

    public static Catalog createCatalog(final String assetId, final int numberOfOffers) {
        final Policy policy = mock(Policy.class);
        final Distribution distribution = Distribution.Builder.newInstance()
                                                              .format("HttpProxy")
                                                              .dataService(new DataService())
                                                              .build();

        final List<Dataset> datasets = IntStream.range(0, numberOfOffers)
                                                .boxed()
                                                .map(i -> Dataset.Builder.newInstance()
                                                                         .properties(
                                                                                 Map.of(NAMESPACE_EDC_ID, assetId + i))
                                                                         .offer(getOfferId(assetId + i), policy)
                                                                         .distribution(distribution)
                                                                         .build())
                                                .toList();
        return Catalog.Builder.newInstance().datasets(datasets).build();
    }

    @NotNull
    private static String getOfferId(final String assetId) {
        return UUID.randomUUID() + ":" + assetId + ":" + UUID.randomUUID();
    }

}
