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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.eclipse.tractusx.irs.recursive.model.RecursiveJobRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Raw Item Stock recursive rejection integration")
class RawItemStockRecursiveRejectionIntegrationTest extends RecursiveIntegrationTestBase {

    @Test
    @DisplayName("Atlas rejects an explicit raw ItemStock recursive request")
    void shouldRejectExplicitRawItemStockRequest() {
        final RecursiveIrsInstance atlas = startInstance(ATLAS, ATLAS_BPN);

        assertThatThrownBy(() -> client.startRootJob(atlas, RecursiveJobRequest.builder()
                .openingId(OPENING_ID)
                .useCase(PURIS_USE_CASE)
                .globalAssetId(ATLAS_ASSET)
                .aspects(List.of(ITEM_STOCK))
                .build()))
                .isInstanceOf(HttpClientErrorException.class)
                .satisfies(exception -> {
                    final HttpClientErrorException response = (HttpClientErrorException) exception;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(response.getResponseBodyAsString()).contains("UNSUPPORTED_ASPECT", ITEM_STOCK);
                });
        assertThat(client.jobs(atlas)).isEmpty();
    }
}
