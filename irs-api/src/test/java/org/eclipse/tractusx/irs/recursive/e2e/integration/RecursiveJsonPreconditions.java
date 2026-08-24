/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_CATENAX_POLICY;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCAT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DSPACE;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_ODRL;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_TRACTUSX;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;

/**
 * Keeps mocked EDC/DTR JSON visible while validating it against the type used by the IRS where possible.
 */
final class RecursiveJsonPreconditions {

    private static final ObjectMapper OBJECT_MAPPER = objectMapper();
    private static final EdcTransformer EDC_TRANSFORMER = edcTransformer();

    private RecursiveJsonPreconditions() {
    }

    static JsonPrecondition<JsonNode> json(final String json) {
        return new JsonPrecondition<>(json, read(json, JsonNode.class));
    }

    static JsonPrecondition<Catalog> edcCatalog(final String json) {
        try {
            return new JsonPrecondition<>(json, EDC_TRANSFORMER.transformCatalog(json, StandardCharsets.UTF_8));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid recursive integration EDC catalog precondition JSON", e);
        }
    }

    private static <T> T read(final String json, final Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid recursive integration precondition JSON", e);
        }
    }

    record JsonPrecondition<T>(String json, T parsed) {
    }

    private static ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JSONPModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerSubtypes(AtomicConstraint.class, LiteralExpression.class);
        return objectMapper;
    }

    private static EdcTransformer edcTransformer() {
        final TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("odrl", NAMESPACE_ODRL);
        jsonLd.registerNamespace("dct", NAMESPACE_DCT);
        jsonLd.registerNamespace("tx", NAMESPACE_TRACTUSX);
        jsonLd.registerNamespace("edc", NAMESPACE_EDC);
        jsonLd.registerNamespace("dcat", NAMESPACE_DCAT);
        jsonLd.registerNamespace("dspace", NAMESPACE_DSPACE);
        jsonLd.registerNamespace("cx-policy", NAMESPACE_CATENAX_POLICY);
        return new EdcTransformer(OBJECT_MAPPER, jsonLd, new TypeTransformerRegistryImpl());
    }
}
