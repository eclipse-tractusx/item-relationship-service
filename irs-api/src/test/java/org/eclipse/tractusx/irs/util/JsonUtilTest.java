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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.eclipse.tractusx.irs.data.JsonParseException;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

    private final JsonUtil sut = new JsonUtil();

    @Test
    void asString_onSuccess() {
        assertThat(sut.asString(new HashMap<String, String>())).isEqualTo("{}");
    }

    @Test
    void asString_onFailure() {
        Object mockItem = mock(Object.class);
        when(mockItem.toString()).thenReturn(mockItem.getClass().getName());
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> sut.asString(mockItem));
    }

    @Test
    void fromString_OnSuccess() {
        assertThat(sut.fromString("{}", HashMap.class)).isEqualTo(new HashMap<String, String>());
    }

    @Test
    void fromString_OnFailure() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> sut.fromString("{", HashMap.class));
    }
}