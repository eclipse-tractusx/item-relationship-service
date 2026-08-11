/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class RecursiveHttpExceptionResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RecursiveHttpExceptionResolver resolver;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        resolver = new RecursiveHttpExceptionResolver(objectMapper);
        request = new MockHttpServletRequest(HttpMethod.GET.name(), "/irs/recursive/jobs");
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturnMethodNotAllowedForRecursiveEndpoint() throws Exception {
        final ModelAndView result = resolver.resolveException(request, response, null,
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET")));

        assertError(result, HttpStatus.METHOD_NOT_ALLOWED, RecursiveErrorCode.METHOD_NOT_ALLOWED);
        assertThat(response.getHeader(HttpHeaders.ALLOW)).isEqualTo(HttpMethod.GET.name());
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForRecursiveEndpoint() throws Exception {
        final ModelAndView result = resolver.resolveException(request, response, null,
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertError(result, HttpStatus.UNSUPPORTED_MEDIA_TYPE, RecursiveErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void shouldReturnNotAcceptableForRecursiveEndpoint() throws Exception {
        final ModelAndView result = resolver.resolveException(request, response, null,
                new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)));

        assertError(result, HttpStatus.NOT_ACCEPTABLE, RecursiveErrorCode.NOT_ACCEPTABLE);
    }

    @Test
    void shouldReturnNotFoundForUnknownRecursiveEndpoint() throws Exception {
        final ModelAndView result = resolver.resolveException(request, response, null,
                new NoResourceFoundException(HttpMethod.GET, "/irs/recursive/missing"));

        assertError(result, HttpStatus.NOT_FOUND, RecursiveErrorCode.RECURSIVE_ENDPOINT_NOT_FOUND);
    }

    @Test
    void shouldIgnoreNonRecursiveEndpoint() {
        request = new MockHttpServletRequest(HttpMethod.GET.name(), "/irs/jobs");

        final ModelAndView result = resolver.resolveException(request, response, null,
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET")));

        assertThat(result).isNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsByteArray()).isEmpty();
    }

    private void assertError(final ModelAndView result, final HttpStatus expectedStatus,
            final RecursiveErrorCode expectedCode) throws Exception {
        final RecursiveErrorResponse error = objectMapper.readValue(response.getContentAsByteArray(),
                RecursiveErrorResponse.class);
        assertThat(result).isNotNull();
        assertThat(response.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(error.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(error.getCode()).isEqualTo(expectedCode);
        assertThat(error.getErrorRef()).isNotBlank();
    }
}
