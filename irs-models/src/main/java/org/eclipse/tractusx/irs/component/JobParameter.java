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
package org.eclipse.tractusx.irs.component;

import java.util.List;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;

/**
 * Job parameter of job processing
 */
@Schema(description = "Job parameter of job processing.")
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class JobParameter {

    @Schema(implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @Schema(implementation = String.class)
    @Singular
    private List<String> aspects;

    @Schema(implementation = Integer.class)
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer depth;

    @Schema(implementation = String.class)
    private String bpn;

    @Schema(implementation = Direction.class)
    private Direction direction;

    @Schema(implementation = Boolean.class)
    private boolean collectAspects;

    @Schema(implementation = Boolean.class)
    private boolean integrityCheck;

    @Schema(implementation = Boolean.class)
    private boolean lookupBPNs;

    private String callbackUrl;

}
