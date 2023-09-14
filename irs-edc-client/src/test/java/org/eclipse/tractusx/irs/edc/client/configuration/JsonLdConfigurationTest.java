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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.spi.result.Result;
import org.junit.jupiter.api.Test;

class JsonLdConfigurationTest {
    @Test
    void shouldConvertJsonLD() {
        final JsonLdConfiguration jsonLdConfiguration = new JsonLdConfiguration();
        final TitaniumJsonLd titaniumJsonLd = jsonLdConfiguration.titaniumJsonLd(jsonLdConfiguration.monitor());
        final JsonObject build = Json.createObjectBuilder()
                                     .add("dct", "https://purl.org/dc/terms/")
                                     .add("tx", "https://w3id.org/tractusx/v0.0.1/ns/")
                                     .add("edc", "https://w3id.org/edc/v0.0.1/ns/")
                                     .add("dcat", "https://www.w3.org/ns/dcat/")
                                     .add("odrl", "http://www.w3.org/ns/odrl/2/")
                                     .add("dspace", "https://w3id.org/dspace/v0.8/")
                                     .build();
        final JsonObject jsonObject = Json.createObjectBuilder()
                                          .add("@type", "edc:NegotiationState")
                                          .add("edc:state", "FINALIZED")
                                          .add("@context", build)
                                          .build();
        final Result<JsonObject> expand = titaniumJsonLd.expand(jsonObject);
        final JsonObject expandedObject = expand.getContent();
        assertThat(expandedObject).containsKey("https://w3id.org/edc/v0.0.1/ns/state");
        final Result<JsonObject> compact = titaniumJsonLd.compact(expandedObject);
        final JsonObject compactetObject = compact.getContent();
        assertThat(compactetObject).containsKey("edc:state");
    }

    @Test
    void shouldMapContstraints() throws JsonProcessingException {
        final AtomicConstraint atomicConstraint = AtomicConstraint.Builder.newInstance()
                                                                          .operator(Operator.EQ)
                                                                          .leftExpression(
                                                                                  new LiteralExpression("idsc:PURPOSE"))
                                                                          .rightExpression(
                                                                                  new LiteralExpression("ID 3.0 Trace"))
                                                                          .build();
        final ObjectMapper objectMapper = new JsonLdConfiguration().objectMapper();
        final String s = objectMapper.writeValueAsString(atomicConstraint);
        System.out.println(s);
        assertThat(s).isNotBlank().contains("dataspaceconnector:literalexpression");
    }
}
