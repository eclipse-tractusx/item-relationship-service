/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.tractusx.irs.configuration.local.CxTestDataContainer;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.eclipse.tractusx.irs.util.LocalTestDataConfigurationAware;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class SubmodelTestdataCreatorTest extends LocalTestDataConfigurationAware {

    private final SubmodelTestdataCreator submodelTestdataCreator;

    SubmodelTestdataCreatorTest() throws IOException {
        super();

        submodelTestdataCreator = new SubmodelTestdataCreator(localTestDataConfiguration.cxTestDataContainer());
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithoutChildrenWhenRequestingWithTestId() {
        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = submodelTestdataCreator.createSubmodelForId(
                "test", AssemblyPartRelationship.class);
        assertThat(dummyAssemblyPartRelationshipForId.getCatenaXId()).isNull();
        assertThat(dummyAssemblyPartRelationshipForId.getChildParts()).isNull();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithPreDefinedChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873";
        final AssemblyPartRelationship assemblyPartRelationship = submodelTestdataCreator.createSubmodelForId(
                catenaXId + "_assemblyPartRelationship", AssemblyPartRelationship.class);

        final Set<AssemblyPartRelationship.ChildData> childParts = assemblyPartRelationship.getChildParts();
        assertThat(childParts).hasSize(1);
        final List<String> childIDs = List.of("urn:uuid:ee5f6ca1-011d-4421-960a-4521b69b3503");
        childParts.forEach(childData -> assertThat(childIDs).contains(childData.getChildCatenaXId()));
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446";
        final SubmodelClientLocalStub client = new SubmodelClientLocalStub(new JsonUtil(), mock(
                CxTestDataContainer.class));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getSubmodel(catenaXId, AssemblyPartRelationship.class));
    }
}