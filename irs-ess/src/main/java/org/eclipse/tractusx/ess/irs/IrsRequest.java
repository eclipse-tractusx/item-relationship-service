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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.ess.irs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/**
 *  Irs Request for IRS API
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
class IrsRequest {
    private IdentificationKey key;
    private String bomLifecycle;
    private List<String> aspects;
    private boolean collectAspects;
    private int depth;

    /**
     * Predefined request body for SupplyChain processing
     * @param globalAssetId id
     * @param bomLifecycle lifecycle - default is asPlanned
     * @return request body
     */
    /* package */ static IrsRequest bpnInvestigations(final String globalAssetId, final BomLifecycle bomLifecycle) {
        return IrsRequest.builder()
                         .key(IdentificationKey.builder().globalAssetId(globalAssetId).build())
                         .bomLifecycle(bomLifecycle != null
                                 ? bomLifecycle.getName() : BomLifecycle.AS_PLANNED.getName())
                         .depth(1)
                         .collectAspects(false)
                         .build();
    }

    /**
     * Key object contains required attributes for identify part chain entry node
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class IdentificationKey {
        private String globalAssetId;
        private String bpn;
    }
}
