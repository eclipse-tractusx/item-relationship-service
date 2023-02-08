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
package org.eclipse.tractusx.ess.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Submodel;

/**
 * Object to store in cache
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BpnInvestigationJob {

    private static final String SUPPLY_CHAIN_ASPECT_TYPE = "supply_chain_impacted";

    private Jobs jobSnapshot;
    private List<String> incidentBpns;
    private List<String> notifications;

    public static BpnInvestigationJob create(final Jobs jobSnapshot, final List<String> incidentBpns) {
        return new BpnInvestigationJob(
                jobSnapshot,
                incidentBpns,
                new ArrayList<>()
        );
    }

    public BpnInvestigationJob update(final Jobs jobSnapshot, final SupplyChainImpacted newSupplyChain) {
        final Optional<SupplyChainImpacted> previousSupplyChain = this.getJobSnapshot()
                                           .getSubmodels()
                                           .stream()
                                           .filter(sub -> SUPPLY_CHAIN_ASPECT_TYPE.equals(sub.getAspectType()))
                                           .map(sub -> sub.getPayload().get("supplyChainImpacted"))
                                           .map(Object::toString)
                                           .map(SupplyChainImpacted::fromString)
                                           .findFirst();

        final SupplyChainImpacted supplyChainImpacted = previousSupplyChain
                .map(prevSupplyChain -> prevSupplyChain.or(newSupplyChain))
                .orElse(newSupplyChain);

        this.jobSnapshot = extendJobWithSupplyChainSubmodel(jobSnapshot, supplyChainImpacted);
        return this;
    }

    public BpnInvestigationJob withNotifications(final List<String> notifications) {
        this.notifications.addAll(notifications);
        return this;
    }

    private static Jobs extendJobWithSupplyChainSubmodel(final Jobs irsJob, final SupplyChainImpacted supplyChainImpacted) {
        final Submodel supplyChainImpactedSubmodel = Submodel.builder()
                                                             .aspectType(SUPPLY_CHAIN_ASPECT_TYPE)
                                                             .payload(Map.of("supplyChainImpacted", supplyChainImpacted.getDescription()))
                                                             .build();

        return irsJob.toBuilder().submodels(Collections.singletonList(supplyChainImpactedSubmodel)).build();
    }
}
