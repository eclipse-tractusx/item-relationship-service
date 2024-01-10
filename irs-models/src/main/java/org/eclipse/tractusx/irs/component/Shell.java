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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * An AAS shell.
 */
@Value
@Builder(toBuilder = true)
@Schema(description = "")
@AllArgsConstructor
@JsonDeserialize(builder = Shell.ShellBuilder.class)
public class Shell {

    @Schema(implementation = String.class)
    private String identification;

    @Schema(implementation = String.class)
    private String idShort;

    @Schema()
    @Singular
    private Map<String, String> specificAssetIds;

    @Schema()
    @Singular
    private List<Description> descriptions;

    @Schema()
    @Singular
    private List<GlobalAssetIdentification> globalAssetIds;

    @Schema()
    @Singular
    private List<SubmodelDescriptor> submodelDescriptors;

    /**
     * User to build Shell
     */
    @Schema(description = "User to build shell items")
    @JsonPOJOBuilder(withPrefix = "")
    public static class ShellBuilder {
    }
}
