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
package org.eclipse.tractusx.irs.testing.dataintegrity.models;

import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Simple SingleLevelBomAsBuilt class
 */
@Data
@Builder
@Jacksonized
public final class SingleLevelBomAsBuiltPayload {
    private String catenaXId;
    private Set<ChildData> childItems;

    /**
     * ChildData for SingleLevelBomAsBuilt
     */
    @Data
    @Builder
    @Jacksonized
    public static class ChildData {
        private String catenaXId;
        private Object quantity;
        private String businessPartner;
        private String createdOn;
        private String lastModifiedOn;

    }

}
