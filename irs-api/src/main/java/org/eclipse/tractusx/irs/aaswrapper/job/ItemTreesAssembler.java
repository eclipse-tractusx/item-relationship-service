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
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Bpn;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;

/**
 * Assembles multiple partial item graphs into one overall item graph.
 */
@Slf4j
@RequiredArgsConstructor
public class ItemTreesAssembler {

    /**
     * Assembles multiple partial item graphs into one overall item graph.
     *
     * @param partialGraph partial item graph.
     * @return An item graph containing all the items from {@code partialGraph}, with deduplication.
     */
    /* package */ ItemContainer retrieveItemGraph(final Stream<ItemContainer> partialGraph) {
        final var relationships = new LinkedHashSet<Relationship>();
        final var numberOfPartialTrees = new AtomicInteger();
        final ArrayList<Tombstone> tombstones = new ArrayList<>();
        final ArrayList<AssetAdministrationShellDescriptor> shells = new ArrayList<>();
        final ArrayList<Submodel> submodels = new ArrayList<>();
        final Set<Bpn> bpns = new HashSet<>();

        partialGraph.forEachOrdered(itemGraph -> {
            relationships.addAll(itemGraph.getRelationships());
            numberOfPartialTrees.incrementAndGet();
            tombstones.addAll(itemGraph.getTombstones());
            shells.addAll(itemGraph.getShells());
            submodels.addAll(itemGraph.getSubmodels());
            bpns.addAll(itemGraph.getBpnsWithManufacturerName());
        });

        log.info("Assembled item graph from {} partial graphs", numberOfPartialTrees);

        return ItemContainer.builder()
                            .relationships(relationships)
                            .tombstones(tombstones)
                            .shells(shells)
                            .submodels(submodels)
                            .bpns(bpns)
                            .build();
    }
}
