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
import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
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

    @Schema(implementation = BomLifecycle.class, example = "asBuilt")
    private BomLifecycle bomLifecycle;

    @Schema(implementation = String.class, example = "SerialPart")
    @Singular
    private List<String> aspects;

    @Schema(implementation = Integer.class, example = "1")
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer depth;

    @Schema(implementation = String.class, example = "BPNL00000003AYRE")
    private String bpn;

    @Schema(implementation = Direction.class, example = "upward")
    private Direction direction;

    @Schema(implementation = Boolean.class, example = "false")
    private boolean collectAspects;

    @Schema(implementation = Boolean.class, example = "false")
    private boolean lookupBPNs;

    @Schema(implementation = String.class, example = "https://hostname.com/callback?jobId={jobId}")
    private String callbackUrl;

    public static JobParameter create(final @NonNull RegisterJob request) {
        final BomLifecycle bomLifecycle = Optional.ofNullable(request.getBomLifecycle()).orElse(BomLifecycle.AS_BUILT);
        final List<String> aspectTypeValues = Optional.ofNullable(request.getAspects())
                                                      .orElse(List.of(bomLifecycle.getDefaultAspect()));
        final Direction direction = Optional.ofNullable(request.getDirection()).orElse(Direction.DOWNWARD);

        return JobParameter.builder()
                           .depth(request.getDepth())
                           .bomLifecycle(bomLifecycle)
                           .bpn(request.getKey().getBpn())
                           .direction(direction)
                           .aspects(aspectTypeValues.isEmpty()
                                   ? List.of(bomLifecycle.getDefaultAspect())
                                   : aspectTypeValues)
                           .collectAspects(request.isCollectAspects())
                           .lookupBPNs(request.isLookupBPNs())
                           .callbackUrl(request.getCallbackUrl())
                           .build();
    }

}
