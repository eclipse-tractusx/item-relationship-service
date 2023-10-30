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
package org.eclipse.tractusx.irs.ess.service;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Return value indicates if supply chain is impacted. The supply chain contains the passed BPN.
 */
public enum SupplyChainImpacted {
    YES("Yes"),
    NO("No"),
    UNKNOWN("Unknown");

    @Getter
    @JsonValue
    private final String description;

    SupplyChainImpacted(final String description) {
        this.description = description;
    }

    @JsonCreator
    public static SupplyChainImpacted fromString(final String name) {
        return SupplyChainImpacted.valueOf(name.toUpperCase(Locale.ROOT));
    }

    @SuppressWarnings("PMD.ShortMethodName")
    public SupplyChainImpacted or(final SupplyChainImpacted newSupplyChainImpacted) {
        if (this.equals(YES)) {
            return this;
        } else if (this.equals(UNKNOWN)) {
            if (newSupplyChainImpacted.equals(NO)) {
                return this;
            } else {
                return newSupplyChainImpacted;
            }
        }

        return newSupplyChainImpacted;
    }
}
