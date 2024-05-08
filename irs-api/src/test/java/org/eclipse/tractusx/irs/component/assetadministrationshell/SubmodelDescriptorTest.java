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
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutHref;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SubmodelDescriptorTest {

    @ParameterizedTest
    @MethodSource
    void shouldValidateByFilterAspect(final String aspectFilter, final boolean expected) {
        // Arrange
        final String singleLevelBomTwoZeroVersion = "urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt";
        final SubmodelDescriptor submodelDescriptor = submodelDescriptorWithoutHref(singleLevelBomTwoZeroVersion);

        // Act
        final boolean result = submodelDescriptor.isAspect(aspectFilter);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> shouldValidateByFilterAspect() {
        return Stream.of(
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.0.1#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.0.22#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.1.0#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.17.1#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.22.0#SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:2.0.0", true),
                Arguments.of("SingleLevelBomAsBuilt", true),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:1.9.0#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:1.9.9#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:3.4.1#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:io.catenax.single_level_bom_as_built:17.0.1#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:samm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:com.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt", false),
                Arguments.of("urn:bamm:com.catenax.single_level_bom_as_built", false),
                Arguments.of("urn:bamm:io.catenax.serial_part:1.0.1#SerialPart", false),
                Arguments.of("SerialPart", false),
                Arguments.of("", false),
                Arguments.of(null, false)
        );
    }

}
