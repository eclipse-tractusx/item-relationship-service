/********************************************************************************
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc.client.transformer;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.model.DSPVersionParamsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Unit tests for {@link JsonObjectFromDspVersionParamsRequestTransformer}.
 */
@ExtendWith(MockitoExtension.class)
class JsonObjectFromDspVersionParamsRequestTransformerTest {

    private JsonObjectFromDspVersionParamsRequestTransformer transformer;

    @Mock
    private TransformerContext context;

    @BeforeEach
    void setUp() {
        JsonBuilderFactory jsonFactory = Json.createBuilderFactory(null);
        transformer = new JsonObjectFromDspVersionParamsRequestTransformer(jsonFactory);
    }

    @Test
    void shouldTransformRequestToExpectedJsonLd() {
        String bpnl = "BPNL00000003AXS1";
        String edcUrl = "https://provider.example.com/api/v1/dsp";
        DSPVersionParamsRequest dto = new DSPVersionParamsRequest(edcUrl, bpnl);

        JsonObject result = transformer.transform(dto, context);

        assertThat(result).isNotNull();
        assertThat(result.getJsonObject("@context")).isNotNull();
        assertThat(result.getJsonObject("@context").getString("tx"))
                .isEqualTo("https://w3id.org/tractusx/v0.0.1/ns/");
        assertThat(result.getJsonObject("@context").getString("edc"))
                .isEqualTo("https://w3id.org/edc/v0.0.1/ns/");

        assertThat(result.getString("tx:bpnl")).isEqualTo(bpnl);
        assertThat(result.getString("edc:counterPartyAddress")).isEqualTo(edcUrl);
    }
}
