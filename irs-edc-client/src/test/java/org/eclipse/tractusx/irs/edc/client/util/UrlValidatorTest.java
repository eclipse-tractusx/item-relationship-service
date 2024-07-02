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
package org.eclipse.tractusx.irs.edc.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = { "http://www.irs.com",
                             "https://www.provider.com",
                             "http://www.edc.test1",
                             "http://provider.edc/path?param=test&param2=something",
                             "http://irs.tx.test",
                             "http://localhost:8080"
    })
    void shouldValidateCorrectUrls(final String url) {
        assertThat(UrlValidator.isValidUrl(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "smtp://www.google.com",
                             "htp://test",
                             "unkown"
    })
    void shouldFailOnInvalidUrls(final String url) {
        assertThat(UrlValidator.isValidUrl(url)).isFalse();
    }
}