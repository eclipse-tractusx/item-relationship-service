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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * LifecycleContextCharacteristic
 */
@Getter
@Jacksonized
enum LifecycleContextCharacteristic {
    ASREQUIRED("AsRequired"),
    /**
     * Build up the initial BoM in design phase of a new automotive product including
     * alternative partsExpected to have reserach & development part descriptions
     * instead of specific part numbers
     */
    ASDESIGNED("AsDesigned"),
    /**
     * BoM as it is used to plan manufacturing including alternative parts
     * Sourcing will most likely be based on this (besides key parts
     * which start earlier)
     */
    ASPLANNED("AsPlanned"),
    /**
     * BoM as a component is built or manufacturedDuring manufactoring of a
     * vehicle the serial numbers & batch numbers are documented (German: Verbaudokumentation)
     * This leads to one BOM per build car
     */

    ASBUILT("AsBuilt"),
    /**
     * BoM AsMaintained describes the BoM after purchase by a
     * customer and updates through maintenace.
     */
    ASMAINTAINED("AsMaintained"),
    /**
     * BoM AsRecycled describes the BoM after the recycling of the product.
     */
    ASRECYCLED("AsRecycled");

    /**
     * value
     */
    @JsonValue
    private final String value;

    LifecycleContextCharacteristic(final String value) {
        this.value = value;
    }

}
