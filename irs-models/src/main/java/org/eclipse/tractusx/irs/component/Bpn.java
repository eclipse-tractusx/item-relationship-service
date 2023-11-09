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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Business partner id with name
 */
@Getter
@EqualsAndHashCode(exclude = {"manufacturerName"})
@Schema(description = "Business partner id with name")
@SuppressWarnings("PMD.ShortClassName")
public class Bpn {

    @Schema(implementation = String.class, example = "BPNL00000003AYRE")
    private String manufacturerId;
    @Schema(implementation = String.class, example = "OEM A")
    private String manufacturerName;

    public static Bpn withManufacturerId(final String manufacturerId) {
        final Bpn bpn = new Bpn();
        bpn.manufacturerId = manufacturerId;
        return bpn;
    }

    public Bpn updateManufacturerName(final String manufacturerName) {
        this.manufacturerName = manufacturerName;
        return this;
    }

}
