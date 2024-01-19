/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.tractusx.irs.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * API Exception Handler.
 */
@Slf4j
@ControllerAdvice
public class IrsExceptionHandler {

    /**
     * Handler for response status exception
     *
     * @param exception see {@link ResponseStatusException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException exception) {
        log.info(exception.getClass().getName(), exception);

        final HttpStatus httpStatus = HttpStatus.valueOf(exception.getStatusCode().value());

        return ResponseEntity.status(httpStatus)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(httpStatus)
                                                .withError(httpStatus.getReasonPhrase())
                                                .withMessages(List.of(exception.getReason()))
                                                .build());
    }

    /**
     * Handler for spring BindException
     *
     * @param exception see {@link BindException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(final BindException exception) {
        log.info(exception.getClass().getName(), exception);

        final List<String> errors = exception.getBindingResult()
                                             .getFieldErrors()
                                             .stream()
                                             .map(e -> e.getField() + ":" + e.getDefaultMessage())
                                             .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withError(IrsAppConstants.INVALID_ARGUMENTS)
                                                .withMessages(errors)
                                                .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {
        log.info(exception.getClass().getName(), exception);

        if (exception.getRootCause() instanceof IllegalArgumentException illegalArgumentException) {
            return handleIllegalArgumentException(illegalArgumentException);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withError(exception.getMessage())
                                                .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(final IllegalStateException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withError(exception.getMessage())
                                                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {
        log.info(exception.getClass().getName(), exception);

        String message = "Malformed JSON request";

        if (exception.getRootCause() instanceof NoSuchElementException) {
            message = ExceptionUtils.getRootCauseMessage(exception);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withError(message)
                                                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withError(exception.getMessage())
                                                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.FORBIDDEN)
                                                .withError(exception.getMessage())
                                                .build());
    }

    /**
     * Catcher for all unhandled exceptions
     *
     * @param exception see {@link Exception}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(final Exception exception) {
        log.error(exception.getClass().getName(), exception);
        // Exception error not used in response to prevent leak of any possible sensitive information.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .withError("Error Occurred")
                                                .build());
    }
}
