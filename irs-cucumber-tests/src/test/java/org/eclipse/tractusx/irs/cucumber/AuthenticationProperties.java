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
package org.eclipse.tractusx.irs.cucumber;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.Builder;

@Builder
/* package */ class AuthenticationProperties {
    private final String uri;
    private final String apiKey;

    /* package */ AuthenticationProperties(final String uri, final String apiKey) {
        this.uri = uri;
        this.apiKey = apiKey;
    }

    /* package */ RequestSpecification getNewAuthenticationRequestSpecification() {
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader("X-API-KEY", apiKey);
        builder.setBaseUri(uri);

        return builder.build();
    }
}
