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
package org.eclipse.tractusx.irs.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.common.ApiConstants;
import org.junit.jupiter.api.Test;

class TrustedEndpointsFilterTest {

    @Test
    void shouldCallChain() throws ServletException, IOException {
        // arrange
        final var testee = new TrustedEndpointsFilter("0");
        final var request = mock(ServletRequest.class);
        final var response = mock(ServletResponse.class);
        final var chain = mock(FilterChain.class);

        // act
        testee.doFilter(request, response, chain);

        // assert
        verify(chain).doFilter(any(), any());
    }

    @Test
    void shouldBlockRequestToTrustedPort() throws ServletException, IOException {
        // arrange
        final var testee = new TrustedEndpointsFilter("8081");
        final var request = mock(HttpServletRequestWrapper.class);
        when(request.getLocalPort()).thenReturn(8081);
        when(request.getRequestURI()).thenReturn("/" + IrsApplication.API_PREFIX + "/test");
        final var response = mock(HttpServletResponseWrapper.class);
        final var stream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(stream);
        final var chain = mock(FilterChain.class);

        // act
        testee.doFilter(request, response, chain);

        // assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(chain, times(0)).doFilter(any(), any());
    }

    @Test
    void shouldBlockRequestToUntrustedPort() throws ServletException, IOException {
        // arrange
        final var testee = new TrustedEndpointsFilter("8081");
        final var request = mock(HttpServletRequestWrapper.class);
        when(request.getLocalPort()).thenReturn(8080);
        when(request.getRequestURI()).thenReturn("/" + ApiConstants.API_PREFIX_INTERNAL + "/test");
        final var response = mock(HttpServletResponseWrapper.class);
        final var stream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(stream);
        final var chain = mock(FilterChain.class);

        // act
        testee.doFilter(request, response, chain);

        // assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(chain, times(0)).doFilter(any(), any());
    }
}