/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.tractusx.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.actuate.health.Status;

class HealthStatusHelperTest {

    public static List<Arguments> healthStatusToNumeric() {
        return List.of(Arguments.of(Status.UP, 3), Arguments.of(Status.OUT_OF_SERVICE, 2), Arguments.of(Status.DOWN, 1),
                Arguments.of(Status.UNKNOWN, 0), Arguments.of(null, 0));
    }

    @ParameterizedTest
    @MethodSource
    void healthStatusToNumeric(final Status status, final int numericStatus) {
        assertThat(HealthStatusHelper.healthStatustoNumeric(status)).isEqualTo(numericStatus);
    }
}
