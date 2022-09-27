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
package org.eclipse.tractusx.esr.irs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.esr.irs.model.relationship.Relationship;
import org.eclipse.tractusx.esr.irs.model.relationship.LinkedItem;
import org.eclipse.tractusx.esr.irs.model.shell.Shell;
import org.eclipse.tractusx.esr.service.BpnData;

/**
 * Irs Response from IRS API
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class IrsResponse {

    private Job job;
    private List<Relationship> relationships;
    private List<Shell> shells;

    public boolean isRunning() {
        return "RUNNING".equals(job.getJobState());
    }

    /**
     * @return Returns BPN data for initial global asset id
     */
    public Optional<BpnData> findRequestorBPN() {
        final String globalAssetId = this.getJob().getGlobalAssetId();

        return this.findShellBpn(globalAssetId).map(bpn -> BpnData.from(globalAssetId, bpn));
    }

    /**
     * @param globalAssetId id of parent
     * @return Returns BPN data for children of shell
     */
    public List<BpnData> findSuppliersBPN(final String globalAssetId) {
        final List<String> childIds = this.getRelationships()
                                          .stream()
                                          .filter(relationship -> relationship.getCatenaXId().equals(globalAssetId))
                                          .map(Relationship::getLinkedItem)
                                          .map(LinkedItem::getChildCatenaXId)
                                          .collect(Collectors.toList());

        final List<BpnData> suppliers = new ArrayList<>();

        childIds.forEach(childId -> {
            this.findShellBpn(childId).ifPresent(bpn -> suppliers.add(BpnData.from(childId, bpn)));
        });

        return suppliers;
    }

    /**
     * @param globalAssetId id
     * @return Bpn value if Shell with id exists
     */
    private Optional<String> findShellBpn(final String globalAssetId) {
        return this.getShells()
                   .stream()
                   .filter(shell -> shell.getGlobalAssetId().getValue().contains(globalAssetId))
                   .findFirst()
                   .flatMap(Shell::findManufacturerIdIfValid);
    }

    public List<Shell> getRequestorChild() {
        final String globalAssetId = this.getJob().getGlobalAssetId();
        final List<String> childIds = this.getRelationships()
                                          .stream()
                                          .filter(relationship -> relationship.getCatenaXId().equals(globalAssetId))
                                          .map(Relationship::getLinkedItem)
                                          .map(LinkedItem::getChildCatenaXId)
                                          .collect(Collectors.toList());
        return this.getShells()
                   .stream()
                   .filter(shell -> childIds.contains(shell.getGlobalAssetId().getValue().get(0)))
                   .collect(Collectors.toList());
    }

}
