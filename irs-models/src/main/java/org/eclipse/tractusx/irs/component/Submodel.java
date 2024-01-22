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
package org.eclipse.tractusx.irs.component;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Submodel with identification of SubmodelDescriptor, aspect type and payload as String
 */
@Value
@Builder
@Jacksonized
@Schema(description = "Submodel with identification of SubmodelDescriptor, aspect type and payload as String")
public class Submodel {
    private String identification;
    private String aspectType;
    private Map<String, Object> payload;

    public static Submodel from(final String identification, final String aspectType, final Map<String, Object> payload) {
        return Submodel.builder()
                       .identification(identification)
                       .aspectType(aspectType)
                       .payload(payload)
                       .build();
    }
}
