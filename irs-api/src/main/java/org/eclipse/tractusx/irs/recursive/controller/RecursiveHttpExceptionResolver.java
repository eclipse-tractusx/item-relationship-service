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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Handles recursive API errors that occur before a controller method is selected. */
@Slf4j
@Component
public class RecursiveHttpExceptionResolver extends AbstractHandlerExceptionResolver {

    private static final String RECURSIVE_API_PATH = "/irs/recursive";

    private final ObjectMapper objectMapper;

    public RecursiveHttpExceptionResolver(final ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception exception) {
        if (!isRecursiveRequest(request)) {
            return null;
        }
        if (exception instanceof HttpRequestMethodNotSupportedException methodException) {
            copyHeaders(methodException.getHeaders(), response);
            return writeResponse(response, HttpStatus.METHOD_NOT_ALLOWED, RecursiveErrorCode.METHOD_NOT_ALLOWED,
                    "HTTP method is not supported by the recursive API.", exception);
        }
        if (exception instanceof HttpMediaTypeNotSupportedException mediaTypeException) {
            copyHeaders(mediaTypeException.getHeaders(), response);
            return writeResponse(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    RecursiveErrorCode.UNSUPPORTED_MEDIA_TYPE,
                    "Request media type is not supported by the recursive API.", exception);
        }
        if (exception instanceof HttpMediaTypeNotAcceptableException mediaTypeException) {
            copyHeaders(mediaTypeException.getHeaders(), response);
            return writeResponse(response, HttpStatus.NOT_ACCEPTABLE, RecursiveErrorCode.NOT_ACCEPTABLE,
                    "Requested response media type is not supported by the recursive API.", exception);
        }
        if (exception instanceof NoHandlerFoundException || exception instanceof NoResourceFoundException) {
            return writeResponse(response, HttpStatus.NOT_FOUND, RecursiveErrorCode.RECURSIVE_ENDPOINT_NOT_FOUND,
                    "Recursive API endpoint not found.", exception);
        }
        return null;
    }

    private ModelAndView writeResponse(final HttpServletResponse response, final HttpStatus status,
            final RecursiveErrorCode code, final String message, final Exception exception) {
        final String errorRef = UUID.randomUUID().toString();
        final RecursiveErrorResponse body = RecursiveErrorResponse.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .errorRef(errorRef)
                .unknownAspects(List.of())
                .supportedAspects(List.of())
                .build();
        try {
            response.setStatus(status.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), body);
        } catch (IOException ioException) {
            log.error("Failed to write recursive API error response: status={} code={} errorRef={} causeType={}",
                    status.value(), code, errorRef, ioException.getClass().getName());
        }
        log.warn("Recursive API request rejected: status={} code={} errorRef={} causeType={}", status.value(),
                code, errorRef, exception.getClass().getName());
        return new ModelAndView();
    }

    private static void copyHeaders(final HttpHeaders headers, final HttpServletResponse response) {
        headers.forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
    }

    private static boolean isRecursiveRequest(final HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        final String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && !requestUri.startsWith(contextPath)) {
            return false;
        }
        final String path = contextPath.isEmpty() ? requestUri : requestUri.substring(contextPath.length());
        return RECURSIVE_API_PATH.equals(path) || path.startsWith(RECURSIVE_API_PATH + "/");
    }
}
