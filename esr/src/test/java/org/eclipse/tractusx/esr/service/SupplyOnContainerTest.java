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
package org.eclipse.tractusx.esr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleRelationship;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShellWithGlobalAssetId;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.esr.irs.IrsResponse;
import org.eclipse.tractusx.esr.irs.Job;
import org.junit.jupiter.api.Test;

class SupplyOnContainerTest {
    @Test
    void shouldCreateSupplyOnContainer() {
        final IrsResponse irsResponse = IrsResponse.builder()
                                                   .relationships(List.of(exampleRelationship(
                                                           "urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447",
                                                           "urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9")))
                                                   .shells(List.of(exampleShellWithGlobalAssetId(
                                                                   "urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447"),
                                                           exampleShellWithGlobalAssetId(
                                                                   "urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9")))
                                                   .job(Job.builder()
                                                           .globalAssetId(
                                                                   "urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")
                                                           .build())
                                                   .build();
        final Optional<SupplyOnContainer> supplyOnContainer = SupplyOnContainer.from(irsResponse);

        System.out.println(irsResponse.getRelationships());

        assertThat(supplyOnContainer).isPresent();
        assertThat(supplyOnContainer.get().getRequestor().getBpn()).isEqualTo("BPNL00000003AYRE");
        assertThat(supplyOnContainer.get().getSuppliers()).isNotEmpty();
        assertThat(supplyOnContainer.get().getSuppliers().get(0).getBpn()).isEqualTo("BPNL00000003AYRE");
    }
}