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
package org.eclipse.tractusx.irs.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Verifies if token from JWT claim is equal to configured BPN
 */
@Component
public class AuthorizationService {

    private final SecurityHelperService securityHelperService;
    private final String configurationAllowedBpn;

    public AuthorizationService(@Value("${apiAllowedBpn:}") final String configurationAllowedBpn) {
        this.securityHelperService = new SecurityHelperService();
        this.configurationAllowedBpn = configurationAllowedBpn;
    }

    public boolean verifyBpn() {
        if (StringUtils.isBlank(configurationAllowedBpn)) {
            return false;
        }

        final String bpnFromToken = securityHelperService.getBpnClaim();
        return configurationAllowedBpn.equals(bpnFromToken);
    }

}
