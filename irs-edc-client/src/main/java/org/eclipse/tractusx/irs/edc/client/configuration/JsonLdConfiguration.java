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
package org.eclipse.tractusx.irs.edc.client.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.monitor.Monitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JsonLD configuration and namespace constants.
 */
@Configuration("irsEdcClientJsonLdConfiguration")
public class JsonLdConfiguration {

    public static final String NAMESPACE_ODRL = "http://www.w3.org/ns/odrl/2/";
    public static final String NAMESPACE_DSPACE = "https://w3id.org/dspace/v0.8/";
    public static final String NAMESPACE_DCAT = "https://www.w3.org/ns/dcat/";
    public static final String NAMESPACE_EDC = "https://w3id.org/edc/v0.0.1/ns/";
    public static final String NAMESPACE_EDC_CID = NAMESPACE_EDC + "cid";
    public static final String NAMESPACE_EDC_PARTICIPANT_ID = NAMESPACE_EDC + "participantId";
    public static final String NAMESPACE_EDC_ID = NAMESPACE_EDC + "id";
    public static final String NAMESPACE_TRACTUSX = "https://w3id.org/tractusx/v0.0.1/ns/";
    public static final String NAMESPACE_DCT = "http://purl.org/dc/terms/";
    public static final String NAMESPACE_AUTH = "https://w3id.org/tractusx/auth/";
    public static final String JSON_LD_OBJECT_MAPPER = "jsonLdObjectMapper";
    public static final String NAMESPACE_CATENAX_POLICY = "https://w3id.org/catenax/policy/";
    public static final String NAMESPACE_CX_TAXONOMY = "https://w3id.org/catenax/taxonomy#";
    public static final String NAMESPACE_CX_ONTOLOGY = "https://w3id.org/catenax/ontology/common#";

    @Bean /* package */ TitaniumJsonLd titaniumJsonLd(final Monitor monitor) {
        final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(monitor);
        titaniumJsonLd.registerNamespace("odrl", NAMESPACE_ODRL);
        titaniumJsonLd.registerNamespace("dct", NAMESPACE_DCT);
        titaniumJsonLd.registerNamespace("tx", NAMESPACE_TRACTUSX);
        titaniumJsonLd.registerNamespace("tx-auth", NAMESPACE_AUTH);
        titaniumJsonLd.registerNamespace("edc", NAMESPACE_EDC);
        titaniumJsonLd.registerNamespace("dcat", NAMESPACE_DCAT);
        titaniumJsonLd.registerNamespace("dspace", NAMESPACE_DSPACE);
        titaniumJsonLd.registerNamespace("cx-policy", NAMESPACE_CATENAX_POLICY);
        return titaniumJsonLd;
    }

    @Bean /* package */ Monitor monitor() {
        return new ConsoleMonitor();
    }

    @Bean(JSON_LD_OBJECT_MAPPER)
        /* package */ ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JSONPModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerSubtypes(AtomicConstraint.class, LiteralExpression.class);
        return objectMapper;
    }
}
