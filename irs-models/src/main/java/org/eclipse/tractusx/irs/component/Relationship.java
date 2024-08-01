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
package org.eclipse.tractusx.irs.component;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Relationship
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@Schema(description = "Relationships between parent and child items.")
public class Relationship {

    private static final int GLOBAL_ASSET_ID_LENGTH = 45;

    @Schema(implementation = String.class, description = "CATENA-X global asset id in the format urn:uuid:uuid4.",
            example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0", minLength = GLOBAL_ASSET_ID_LENGTH,
            maxLength = GLOBAL_ASSET_ID_LENGTH,
            pattern = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    @JsonUnwrapped
    private GlobalAssetIdentification catenaXId;

    private LinkedItem linkedItem;

    @Schema(implementation = String.class, example = "SingleLevelBomAsBuilt")
    private String aspectType;

    @Schema(implementation = String.class, example = "BPNL00000003AYRE")
    private String bpn;

}
