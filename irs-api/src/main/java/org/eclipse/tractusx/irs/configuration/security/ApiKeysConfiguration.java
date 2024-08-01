/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.configuration.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration class for api keys
 */
@Component
@ConfigurationProperties(prefix = "irs.security.api.keys")
@Validated
@Setter
class ApiKeysConfiguration {

    private static final int MIN_API_KEY_SIZE = 5;

    @NotBlank
    @Size(min = MIN_API_KEY_SIZE)
    private String admin;

    @NotBlank
    @Size(min = MIN_API_KEY_SIZE)
    private String regular;

    /* package */ ApiKeyAuthority authorityOf(final String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            throw new BadCredentialsException("Wrong ApiKey");
        }

        if (apiKey.equals(admin)) {
            return ApiKeyAuthority.of(apiKey, AuthorityUtils.createAuthorityList(IrsRoles.ADMIN_IRS));
        } else if (apiKey.equals(regular)) {
            return ApiKeyAuthority.of(regular, AuthorityUtils.createAuthorityList(IrsRoles.VIEW_IRS));
        }

        throw new BadCredentialsException("Wrong ApiKey");
    }
}
