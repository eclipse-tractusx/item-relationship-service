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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for registering new job
 */
@Schema(description = "The requested job definition.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterJob {

    private static final String MIN_TREE_DEPTH_DESC = "1";
    private static final String MAX_TREE_DEPTH_DESC = "100";
    private static final int MIN_TREE_DEPTH = 1;
    private static final int MAX_TREE_DEPTH = 100;
    private static final String ASPECT_MODEL_REGEX = "^(urn:bamm:.*\\d\\.\\d\\.\\d)?(#)?(\\w+)?$";

    @NotNull
    @Valid
    @Schema(description = "Key object contains required attributes for identify part chain entry node.", implementation = PartChainIdentificationKey.class)
    private PartChainIdentificationKey key;

    @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @ArraySchema(schema = @Schema(implementation = String.class), maxItems = Integer.MAX_VALUE)
    private List<String> aspects;

    @Schema(implementation = Integer.class, minimum = MIN_TREE_DEPTH_DESC, maximum = MAX_TREE_DEPTH_DESC,
            description = "Max depth of the item graph returned. If no depth is set item graph with max depth is returned.")
    @Min(MIN_TREE_DEPTH)
    @Max(MAX_TREE_DEPTH)
    private Integer depth;

    @Schema(implementation = Direction.class, defaultValue = Direction.DirectionConstants.DOWNWARD)
    private Direction direction;

    @Schema(description = "Flag to specify whether aspects should be requested and collected. Default is false.")
    private boolean collectAspects;

    @Schema(description = "Flag to specify whether BPNs should be collected and resolved via the configured BPDM URL. Default is false.")
    private boolean lookupBPNs;

    @Schema(description = "Flag to enable or disable the integrity validation of a part chain. Default is true.")
    private boolean collectIntegrities = true;

    @URL(regexp = "^(http|https).*")
    @Schema(description = "Callback url to notify requestor when job processing is finished. There are two uri variable placeholders that can be used: id and state.",
            example = "https://hostname.com/callback?id={id}&state={state}")
    private String callbackUrl;

    /**
     * Returns requested depth if provided, otherwise MAX_TREE_DEPTH value
     *
     * @return depth
     */
    public int getDepth() {
        return depth == null ? MAX_TREE_DEPTH : depth;
    }
}
