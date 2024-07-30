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
package org.eclipse.tractusx.irs.registryclient.decentral;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EdcRetrieverExceptionTest {

    @Test
    void test() throws EdcRetrieverException {

        final EdcRetrieverException build = new EdcRetrieverException.Builder(
                new IllegalArgumentException("my illegal arg")).withEdcUrl("my url").withBpn("my bpn").build();

        assertThat(build.getBpn()).isEqualTo("my bpn");
        assertThat(build.getEdcUrl()).isEqualTo("my url");

        Assertions.assertThatThrownBy(() -> {
            throw build;
        }).hasMessageContaining("my illegal arg");

    }
}