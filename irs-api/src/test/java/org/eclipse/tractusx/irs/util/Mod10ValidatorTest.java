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
package org.eclipse.tractusx.irs.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tractusx.irs.validators.Mod10Validator;
import org.junit.jupiter.api.Test;

class Mod10ValidatorTest {

    final Mod10Validator mod10Validator = new Mod10Validator();

    @Test
    void shouldReturnTrueForModulo10Value() {
        final int value = 50;

        final boolean valid = mod10Validator.isValid(value, null);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseForNotModulo10Value() {
        final int value = 99;

        final boolean valid = mod10Validator.isValid(value, null);

        assertThat(valid).isFalse();
    }
}
