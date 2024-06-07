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
package org.eclipse.tractusx.irs.edc.client.relationships;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RelationshipAspectTest {

    @ParameterizedTest
    @MethodSource("data")
    void shouldFindCorrectRelationshipAspect(final BomLifecycle bomLifecycle, final Direction direction, final RelationshipAspect expected) {
        final RelationshipAspect from = RelationshipAspect.from(bomLifecycle, direction);

        assertThat(from).isEqualTo(expected);
    }

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(BomLifecycle.AS_BUILT, Direction.DOWNWARD, RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT),
                Arguments.of(BomLifecycle.AS_PLANNED, Direction.DOWNWARD, RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED),
                Arguments.of(BomLifecycle.AS_BUILT, Direction.UPWARD, RelationshipAspect.SINGLE_LEVEL_USAGE_AS_BUILT)
        );
    }

}
