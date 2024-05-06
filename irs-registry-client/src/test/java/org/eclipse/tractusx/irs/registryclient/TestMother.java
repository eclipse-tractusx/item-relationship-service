/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.registryclient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.testing.wiremock.DtrWiremockSupport;
import org.springframework.http.HttpHeaders;

public class TestMother {

    public static EndpointDataReference endpointDataReference(final String contractAgreementId) {
        return endpointDataReference(contractAgreementId, DtrWiremockSupport.DATAPLANE_URL);
    }

    public static EndpointDataReference endpointDataReference(final String contractAgreementId, final String endpointUrl) {
        return endpointDataReference(contractAgreementId, HttpHeaders.AUTHORIZATION, edrAuthCode(contractAgreementId),
                endpointUrl, "process-id");
    }

    public static EndpointDataReference endpointDataReference(final String contractAgreementId,
            final String authorization, final String authCode, final String dataplaneUrl, final String processId) {
        return EndpointDataReference.Builder.newInstance()
                                            .authKey(authorization)
                                            .authCode(authCode)
                                            .endpoint(dataplaneUrl)
                                            .contractId(contractAgreementId)
                                            .id(processId)
                                            .build();
    }

    private static String edrAuthCode(final String contractAgreementId) {
        final EDRAuthCode edrAuthCode = EDRAuthCode.builder()
                                                   .cid(contractAgreementId)
                                                   .dad("test")
                                                   .exp(9999999999L)
                                                   .build();
        final String b64EncodedAuthCode = Base64.getUrlEncoder()
                                                .encodeToString(StringMapper.mapToString(edrAuthCode)
                                                                            .getBytes(StandardCharsets.UTF_8));
        return "eyJhbGciOiJSUzI1NiJ9." + b64EncodedAuthCode + ".test";
    }
}