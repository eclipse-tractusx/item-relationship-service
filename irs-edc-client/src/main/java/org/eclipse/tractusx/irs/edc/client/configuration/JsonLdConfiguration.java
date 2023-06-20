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
package org.eclipse.tractusx.irs.edc.client.configuration;

import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLdConfiguration {

    public static final String NAMESPACE_ODRL = "http://www.w3.org/ns/odrl/2/";
    public static final String NAMESPACE_DSPACE = "https://w3id.org/dspace/v0.8/";
    public static final String NAMESPACE_DCAT = "https://www.w3.org/ns/dcat/";
    public static final String NAMESPACE_EDC = "https://w3id.org/edc/v0.0.1/ns/";
    public static final String NAMESPACE_TRACTUSX = "https://w3id.org/tractusx/v0.0.1/ns/";
    public static final String NAMESPACE_DCT = "https://purl.org/dc/terms/";

    @Bean
        /* package */ TitaniumJsonLd titaniumJsonLd(final Monitor monitor) {
        final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(monitor);
        titaniumJsonLd.registerNamespace("odrl", JsonLdConfiguration.NAMESPACE_ODRL);
        titaniumJsonLd.registerNamespace("dct", NAMESPACE_DCT);
        titaniumJsonLd.registerNamespace("tx", NAMESPACE_TRACTUSX);
        titaniumJsonLd.registerNamespace("edc", NAMESPACE_EDC);
        titaniumJsonLd.registerNamespace("dcat", JsonLdConfiguration.NAMESPACE_DCAT);
        titaniumJsonLd.registerNamespace("dspace", NAMESPACE_DSPACE);
        return titaniumJsonLd;
    }

    @Bean
        /* package */ Monitor monitor() {
        return new ConsoleMonitor();
    }
}
