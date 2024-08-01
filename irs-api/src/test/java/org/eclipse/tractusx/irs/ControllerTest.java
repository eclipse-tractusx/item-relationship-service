/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2022,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.tractusx.irs.configuration.security.ApiKeyAuthentication;
import org.eclipse.tractusx.irs.configuration.security.ApiKeyAuthority;
import org.eclipse.tractusx.irs.configuration.security.AuthenticationService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.AuthorityUtils;

public abstract class ControllerTest {

    @MockBean
    protected AuthenticationService authenticationService;

    protected void authenticateWith(String... roles) {
        when(authenticationService.getAuthentication(any(HttpServletRequest.class)))
                .thenReturn(new ApiKeyAuthentication(
                        ApiKeyAuthority.of("123", AuthorityUtils.createAuthorityList(roles))));
    }

}
