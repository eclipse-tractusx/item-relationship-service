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
package org.eclipse.tractusx.irs.component;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;

/*** API type for LinkedItem name/url entry. */
@Schema(description = "Set of child parts the parent object is assembled by (one structural level down).")
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class LinkedItem {

    @Schema(description = "Quantity component.", implementation = Quantity.class)
    private Quantity quantity;

    @Schema(description = "The lifecycle context in which the child part was assembled into the parent part.",
            implementation = BomLifecycle.class)
    private BomLifecycle lifecycleContext;

    @Schema(description = "Datetime of assembly.", implementation = ZonedDateTime.class)
    private ZonedDateTime assembledOn;

    @Schema(description = "Last datetime item was modified.", implementation = ZonedDateTime.class)
    private ZonedDateTime lastModifiedOn;

    @Schema(description = "CatenaX child Id.", implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification childCatenaXId;

}
