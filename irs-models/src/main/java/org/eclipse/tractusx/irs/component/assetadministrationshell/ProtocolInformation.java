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
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ProtocolInformation
 */
@Data
@Builder
@Jacksonized
public class ProtocolInformation {

    /**
     * endpointAddress
     */
    @Schema(implementation = String.class, example = "https://edc.data.plane/{path}/submodel")
    private String href;
    /**
     * endpointProtocol
     */
    @Schema(implementation = String.class, example = "HTTPS")
    private String endpointProtocol;
    /**
     * endpointProtocolVersion
     */
    @ArraySchema(arraySchema = @Schema(implementation = String.class, example = "[\"1.0\"]"))
    private List<String> endpointProtocolVersion;
    /**
     * subprotocol
     */
    private String subprotocol;
    /**
     * subprotocolBody
     */
    private String subprotocolBody;
    /**
     * subprotocolBodyEncoding
     */
    private String subprotocolBodyEncoding;

}
