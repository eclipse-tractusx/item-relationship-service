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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.irs.testing.containers.LocalTestDataConfigurationAware;
import org.junit.jupiter.api.Test;

class SubmodelTestdataCreatorTest extends LocalTestDataConfigurationAware {

    private final SubmodelTestdataCreator submodelTestdataCreator;

    SubmodelTestdataCreatorTest() throws IOException {
        super();

        submodelTestdataCreator = new SubmodelTestdataCreator(localTestDataConfiguration.cxTestDataContainer());
    }

    @Test
    void shouldReturnSingleLevelBomAsBuiltWithoutChildrenWhenRequestingWithTestId() {
        final SingleLevelBomAsBuilt dummySingleLevelBomAsBuiltForId = submodelTestdataCreator.createSubmodelForId(
                "test", SingleLevelBomAsBuilt.class);
        assertThat(dummySingleLevelBomAsBuiltForId.getCatenaXId()).isNull();
        assertThat(dummySingleLevelBomAsBuiltForId.getChildItems()).isNull();
    }

    @Test
    void shouldReturnSingleLevelBomAsBuiltWithPreDefinedChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:94e8a73b-006e-420e-9d46-45e8d0e83d1f";
        final SingleLevelBomAsBuilt singleLevelBomAsBuilt = submodelTestdataCreator.createSubmodelForId(
                catenaXId + "_singleLevelBomAsBuilt", SingleLevelBomAsBuilt.class);

        final Set<SingleLevelBomAsBuilt.ChildData> childItems = singleLevelBomAsBuilt.getChildItems();
        assertThat(childItems).isNotEmpty();
        final List<String> childIDs = List.of("urn:uuid:c7e388e6-c4be-4d52-beb2-c21d94e4762b");
        childItems.forEach(childData -> assertThat(childIDs).contains(childData.getCatenaXId()));
    }

}