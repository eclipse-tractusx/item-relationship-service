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
package org.eclipse.tractusx.irs.recursive.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

/**
 * Verifies the grant validity window and chain opening identifier on input and output.
 */
class RecursiveChainOpeningGrantSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    void shouldAcceptNewValidToOnInput() throws Exception {
        final String json = """
                {"openingId":"opening-42","useCase":"PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE",
                 "validTo":"2099-12-31T23:59:59Z"}""";

        final RecursiveChainOpeningGrant grant = objectMapper.readValue(json, RecursiveChainOpeningGrant.class);

        assertThat(grant.getValidTo()).isEqualTo(ZonedDateTime.parse("2099-12-31T23:59:59Z"));
    }

    @Test
    void shouldAcceptOpeningIdInGrant() throws Exception {
        final String json = """
                {"openingId":"opening-4711","useCase":"PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE"}""";

        final RecursiveChainOpeningGrant grant = objectMapper.readValue(json, RecursiveChainOpeningGrant.class);

        assertThat(grant.getOpeningId()).isEqualTo("opening-4711");
    }

    @Test
    void shouldSerializeOpeningIdInGrant() throws Exception {
        final RecursiveChainOpeningGrant grant = RecursiveChainOpeningGrant.builder()
                .openingId("opening-4711")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .build();

        final String json = objectMapper.writeValueAsString(grant);

        assertThat(objectMapper.readTree(json).get("openingId").asText())
                .isEqualTo("opening-4711");
    }

    @Test
    void shouldAcceptOpeningIdInJobRequest() throws Exception {
        final String json = """
                {"openingId":"opening-4711","useCase":"PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE",
                 "globalAssetId":"urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b"}""";

        final RecursiveJobRequest request = objectMapper.readValue(json, RecursiveJobRequest.class);

        assertThat(request.getOpeningId()).isEqualTo("opening-4711");
    }

    @Test
    void shouldSerializeUsingValidTo() throws Exception {
        final RecursiveChainOpeningGrant grant = RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .validTo(ZonedDateTime.parse("2099-12-31T23:59:59Z"))
                .build();

        final String json = objectMapper.writeValueAsString(grant);

        assertThat(json).contains("validTo").doesNotContain("validUntil");
    }
}
