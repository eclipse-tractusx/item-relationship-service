/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AASWrapperUriAddressRewritePolicyTest {

    final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy(
            "http://aaswrapper:9191/api/service", new UrlValidator());

    /**
     * @param endpointAddress input to rewrite
     * @param path            expected path result
     * @param query           expected query result
     */
    @ParameterizedTest
    @CsvSource(
            { "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue, /api/service/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel, content=value&extent=withBlobValue&provider-connector-url=https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM",
              "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel?content=value&extent=WithBLOBValue, /api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel, content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE",
              "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn%3Auuid%3A1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn%3Auuid%3A8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel?content=value&extent=WithBLOBValue, /api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel, content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE",
            })
    void shouldRewriteValidEndpointAddressToAASWrapperUri(final String endpointAddress, final String path,
            final String query) {
        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo(path);
        assertThat(uri.getQuery()).isEqualTo(query);
    }

    @Test
    void shouldCreateAASWrapperUriWhenValidEndpointAddress() {
        final String endpointAddress = "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";

        final AASWrapperUriAddressRewritePolicy.AASWrapperUri aasWrapperUri = new AASWrapperUriAddressRewritePolicy.AASWrapperUri(
                endpointAddress);

        assertThat(aasWrapperUri.getProviderConnectorUrl()).isEqualTo(
                "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM");
        assertThat(aasWrapperUri.getPath()).isEqualTo(
                "/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel");
        assertThat(aasWrapperUri.getQuery()).isEqualTo("content=value&extent=withBlobValue");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMalformedEndpointAddress() {
        final String malformedEndpointAddress = "http://xxxxNOURN.pl?x=2";

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> new AASWrapperUriAddressRewritePolicy.AASWrapperUri(malformedEndpointAddress));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMissingHost() {
        final String malformedEndpointAddress = "null/BPNL00000003AYRE/urn%3Auuid%3A0834c0ea-435e-4085-8a9b-ac46af75deae-urn%3Auuid%3A6972cc4c-e31e-438c-8b23-4f32331a73ba/submodel?content=value&extent=WithBLOBValue";

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(malformedEndpointAddress));
    }

}
