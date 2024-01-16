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
package org.eclipse.tractusx.irs.configuration.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

class AuthenticationServiceTest {

    private final ApiKeysConfiguration apiKeysConfiguration = mock(ApiKeysConfiguration.class);

    private final AuthenticationService authenticationService = new AuthenticationService(apiKeysConfiguration);

    @Test
    void shouldReturnApiKeyAuthentication() {
        // given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String apiKey = "12345";
        request.addHeader("X-API-KEY", apiKey);
        when(apiKeysConfiguration.authorityOf(apiKey)).thenReturn(ApiKeyAuthority.of(apiKey, AuthorityUtils.createAuthorityList(IrsRoles.ADMIN_IRS)));

        // when
        final Authentication authentication = authenticationService.getAuthentication(request);

        // then
        assertThat(authentication.getPrincipal()).isEqualTo(apiKey);
        assertThat(authentication.getAuthorities()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenXApiKeyHeaderIsMissing() {
        final MockHttpServletRequest noApiKeyRequest = new MockHttpServletRequest();
        when(apiKeysConfiguration.authorityOf(noApiKeyRequest.getHeader("X-API-KEY"))).thenThrow(new BadCredentialsException("Wrong ApiKey"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.getAuthentication(noApiKeyRequest));
    }
}