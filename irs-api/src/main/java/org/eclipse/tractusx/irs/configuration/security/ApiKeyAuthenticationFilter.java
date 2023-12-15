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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to execute authentication based on X-API-KEY header
 */
@RequiredArgsConstructor
class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;
    private final JsonUtil jsonUtil;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        try {
            final Authentication authentication = authenticationService.getAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (final BadCredentialsException exception) {
            unauthorizedResponse(response, exception);
        }

        filterChain.doFilter(request, response);
    }

    private void unauthorizedResponse(final HttpServletResponse servletResponse, final Exception exception) throws IOException {
        servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        final ErrorResponse errorResponse = ErrorResponse.builder()
                                                         .withStatusCode(HttpStatus.UNAUTHORIZED)
                                                         .withError(exception.getMessage())
                                                         .build();


        try (PrintWriter writer = servletResponse.getWriter()) {
            writer.print(jsonUtil.asString(errorResponse));
            writer.flush();
        }
    }
}
