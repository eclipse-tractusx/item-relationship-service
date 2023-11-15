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
package org.eclipse.tractusx.irs.cucumber;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.Builder;

@Builder
/* package */ class AuthenticationProperties {
    private final String uri;
    private final String clientId;
    private final String clientSecret;
    private final String oauth2Url;
    private final String grantType;
    private final String tokenPath;

    /* package */ AuthenticationProperties(final String uri, final String clientId, final String clientSecret,
            final String oauth2Url, final String grantType, final String tokenPath) {
        this.uri = uri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.oauth2Url = oauth2Url;
        this.grantType = grantType;
        this.tokenPath = tokenPath;
    }

    private String obtainAccessToken() {
        final Map<String, String> oauth2Payload = new HashMap<>();
        oauth2Payload.put("grant_type", grantType);
        oauth2Payload.put("client_id", clientId);
        oauth2Payload.put("client_secret", clientSecret);

        return given().params(oauth2Payload).post(oauth2Url).then().extract().jsonPath().getString(tokenPath);
    }

    /* package */ RequestSpecification getNewAuthenticationRequestSpecification() {
        final String accessToken = obtainAccessToken();
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("Authorization", "Bearer " + accessToken);
        builder.setBaseUri(uri);

        return builder.build();
    }
}
