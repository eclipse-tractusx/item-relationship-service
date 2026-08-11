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
package org.eclipse.tractusx.irs.recursive.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Privacy guard for the recursive failure detail anonymizer: external tombstone details must never
 * leak partner BPNs (L/S/A), URLs, UUIDs/globalAssetIds or asset identifiers.
 */
class RecursiveFailureDetailsTest {

    private static final String LEAKED_BPNL = "BPNL000000000065";
    private static final String LEAKED_BPNS = "BPNS0000000001VY";
    private static final String LEAKED_BPNA = "BPNA00000000084C";
    private static final String LEAKED_URL = "https://edc.example/dsp";
    private static final String LEAKED_DSP_URL = "https://edc-belfast.example/api/v1/dsp";
    private static final String LEAKED_GLOBAL_ASSET_ID = "urn:uuid:2c57b0e9-a653-411d-bdcd-64787e9fd3a7";
    private static final String LEAKED_UUID = "3388bd08-4ccf-4aaa-ab51-4aba63d7d8d1";
    private static final String LEAKED_ASSET_ID = "belfast-gearbox-item-stock-asset";

    @Test
    void shouldStripBpnLsAndA() {
        final String sanitized = RecursiveFailureDetails.anonymizedDetail(
                "failed for %s at %s site %s".formatted(LEAKED_BPNL, LEAKED_BPNS, LEAKED_BPNA));

        assertThat(sanitized).doesNotContain(LEAKED_BPNL, LEAKED_BPNS, LEAKED_BPNA)
                             .contains("<bpn>");
    }

    @Test
    void shouldStripUrlsUuidsAndGlobalAssetIds() {
        final String sanitized = RecursiveFailureDetails.anonymizedDetail(
                "GET %s for %s id %s failed".formatted(LEAKED_DSP_URL, LEAKED_GLOBAL_ASSET_ID, LEAKED_UUID));

        assertThat(sanitized)
                .doesNotContain(LEAKED_DSP_URL, LEAKED_GLOBAL_ASSET_ID, LEAKED_UUID)
                .contains("<url>", "<globalAssetId>", "<id>");
    }

    @Test
    void shouldStripNamedKeyValueLeaks() {
        final String sanitized = RecursiveFailureDetails.anonymizedDetail(
                "context globalAssetId=urn:uuid:abc, bpn=%s, assetId=%s, connectorEndpoint=%s".formatted(
                        LEAKED_BPNL, LEAKED_ASSET_ID, LEAKED_URL));

        assertThat(sanitized)
                .doesNotContain(LEAKED_ASSET_ID, LEAKED_BPNL, LEAKED_URL)
                .contains("globalAssetId=<globalAssetId>", "bpn=<bpn>", "assetId=<assetId>",
                        "connectorEndpoint=<url>");
    }

    @Test
    void shouldTruncateOverlyLongDetail() {
        final String sanitized = RecursiveFailureDetails.anonymizedDetail("x".repeat(500));

        assertThat(sanitized).hasSizeLessThanOrEqualTo(240).endsWith("...");
    }

    @Test
    void shouldKeepNeutralDetailUnchanged() {
        final String detail = "A recursive child branch failed.";

        assertThat(RecursiveFailureDetails.anonymizedDetail(detail)).isEqualTo(detail);
    }
}
