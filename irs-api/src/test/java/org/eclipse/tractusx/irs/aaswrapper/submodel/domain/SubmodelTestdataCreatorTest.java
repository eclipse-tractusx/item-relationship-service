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

import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class SubmodelTestdataCreatorTest {

    private final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

    @Test
    void shouldReturnAssemblyPartRelationshipWithoutChildrenWhenRequestingWithTestId() {
        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(
                "test");

        assertThat(dummyAssemblyPartRelationshipForId.getCatenaXId()).isEqualTo("test");
        assertThat(dummyAssemblyPartRelationshipForId.getChildParts()).isEmpty();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithPreDefinedChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final AssemblyPartRelationship assemblyPartRelationship = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(
                catenaXId);

        final Set<ChildData> childParts = assemblyPartRelationship.getChildParts();
        assertThat(childParts).hasSize(3);
        final List<String> childIDs = List.of("urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc",
                "urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4", "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
        childParts.forEach(childData -> assertThat(childIDs).contains(childData.getChildCatenaXId()));
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithCustomChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<String> children = List.of("abc", "def", "ghi");

        final AssemblyPartRelationship assemblyPartRelationship = submodelTestdataCreator.getDummyAssemblyPartRelationshipWithChildren(
                catenaXId, children);

        final Set<ChildData> childParts = assemblyPartRelationship.getChildParts();
        assertThat(childParts).hasSize(3);
        final List<String> childIDs = List.of("abc", "def", "ghi");
        childParts.forEach(childData -> assertThat(childIDs).contains(childData.getChildCatenaXId()));
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446";
        final SubmodelClientLocalStub client = new SubmodelClientLocalStub();

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getSubmodel(catenaXId, AssemblyPartRelationship.class));
    }
}