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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.ProcessingError;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.junit.jupiter.api.Test;

class JobResponseAnalyzerTest {

    @Test
    void parseAndPrintJobResponseResults() throws IOException {
        final String rootGlobalAssetId = "urn:uuid:f1f14335-bda1-4dea-8818-0a1042bef7af";
        final int rootDepth = 0;
        final Jobs jobs = this.loadJobsResponseFromFile("src/test/resources/__files/jobResponse.json");

        this.findAndPrintChildOf(jobs, rootGlobalAssetId, rootDepth);

        assertThat(jobs).isNotNull();
    }

    private List<String> findAndPrintChildOf(final Jobs jobs, final String globalAssetId, final int depth) {
        final List<String> childGlobalIds = jobs.getRelationships()
                                                .stream()
                                                .filter(rel -> rel.getCatenaXId()
                                                                  .getGlobalAssetId()
                                                                  .equals(globalAssetId))
                                                .map(rel -> rel.getLinkedItem().getChildCatenaXId().getGlobalAssetId())
                                                .collect(Collectors.toList());

        System.out.println();
        System.out.println("Depth: " + depth);
        System.out.println("GlobalAssetId: " + globalAssetId);
        System.out.println("BPN: " + this.findBpnOf(jobs.getShells(), globalAssetId));
        System.out.println("Child GlobalAssetId's: " + childGlobalIds);
        System.out.println("Tombstones: ");
        this.findTombstoneOf(jobs.getTombstones(), globalAssetId).forEach(System.out::println);

        childGlobalIds.forEach(child -> this.findAndPrintChildOf(jobs, child, depth + 1));

        return childGlobalIds;
    }

    private String findBpnOf(final List<AssetAdministrationShellDescriptor> shells, final String globalAssetId) {
        return shells.stream()
                   .filter(shell -> shell.getGlobalAssetId().equals(globalAssetId))
                   .findFirst()
                   .flatMap(AssetAdministrationShellDescriptor::findManufacturerId)
                   .orElse("");
    }

    private List<String> findTombstoneOf(final List<Tombstone> tombstones, final String globalAssetId) {
        return tombstones.stream()
                   .filter(tombstone -> tombstone.getCatenaXId().equals(globalAssetId))
                   .map(Tombstone::getProcessingError)
                   .map(ProcessingError::getErrorDetail)
                   .collect(Collectors.toList());
    }

    private Jobs loadJobsResponseFromFile(final String filePath) throws IOException {
        final File file = new File(filePath);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.readValue(file, Jobs.class);
    }

}