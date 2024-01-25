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
package org.eclipse.tractusx.irs.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.Description;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.junit.jupiter.api.Test;

class StringMapperTest {

    @Test
    void mapToString() {
        final var serialized = StringMapper.mapToString(Bpn.withManufacturerId("test"));

        assertThat(serialized).isEqualTo("{\"manufacturerId\":\"test\",\"manufacturerName\":null}");
    }

    @Test
    void shouldThrowParseExceptionWhenMappingToString() {
        final var object = new Object();
        assertThatThrownBy(() -> StringMapper.mapToString(object)).isInstanceOf(
                JsonParseException.class);
    }

    @Test
    void mapFromString() {
        final var bpn = StringMapper.mapFromString("{\"manufacturerId\":\"test\",\"manufacturerName\":null}",
                Bpn.class);

        assertThat(bpn.getManufacturerId()).isEqualTo("test");
    }

    @Test
    void shouldThrowParseExceptionWhenMappingFromString() {
        assertThatThrownBy(() -> StringMapper.mapFromString("test", Description.class)).isInstanceOf(
                JsonParseException.class);
    }
}