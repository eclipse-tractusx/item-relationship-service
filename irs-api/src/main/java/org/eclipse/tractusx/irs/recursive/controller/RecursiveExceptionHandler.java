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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantAlreadyExistsException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantNotFoundException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantInactiveException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobNotFoundException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationAuthenticationException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationValidationException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveUnsupportedAspectException;
import org.eclipse.tractusx.irs.recursive.store.RecursiveStoreException;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps synchronous recursive API failures to a common external response. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {
    RecursiveJobController.class,
    RecursiveNotificationController.class,
    RecursiveChainOpeningGrantController.class
})
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
public class RecursiveExceptionHandler {

    private static final String INVALID_REQUEST_MESSAGE = "Invalid recursive request.";

    @ExceptionHandler({
        BindException.class,
        ConstraintViolationException.class,
        HandlerMethodValidationException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<RecursiveErrorResponse> handleValidationFailure(final Exception exception) {
        return response(HttpStatus.BAD_REQUEST, RecursiveErrorCode.INVALID_REQUEST, INVALID_REQUEST_MESSAGE,
                exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RecursiveErrorResponse> handleMalformedRequest(
            final HttpMessageNotReadableException exception) {
        return response(HttpStatus.BAD_REQUEST, RecursiveErrorCode.MALFORMED_REQUEST,
                "Malformed recursive request.", exception);
    }

    @ExceptionHandler(RecursiveUnsupportedAspectException.class)
    public ResponseEntity<RecursiveErrorResponse> handleUnsupportedAspect(
            final RecursiveUnsupportedAspectException exception) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, RecursiveErrorCode.UNSUPPORTED_ASPECT,
                "The recursive request contains unsupported aspects.", exception,
                exception.getUnknownAspects(), exception.getSupportedAspects());
    }

    @ExceptionHandler(RecursiveChainOpeningGrantInactiveException.class)
    public ResponseEntity<RecursiveErrorResponse> handleGrantValidation(
            final RecursiveChainOpeningGrantInactiveException exception) {
        return response(HttpStatus.FORBIDDEN, RecursiveErrorCode.CHAIN_OPENING_GRANT_REJECTED,
                "Chain opening grant validation failed.", exception);
    }

    @ExceptionHandler(RecursiveNotificationAuthenticationException.class)
    public ResponseEntity<RecursiveErrorResponse> handleNotificationAuthentication(
            final RecursiveNotificationAuthenticationException exception) {
        return response(HttpStatus.FORBIDDEN, RecursiveErrorCode.NOTIFICATION_AUTHENTICATION_FAILED,
                "Recursive notification transport identity check failed.", exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RecursiveErrorResponse> handleAccessDenied(final AccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, RecursiveErrorCode.AUTHORIZATION_FAILED,
                "Access to the recursive API was denied.", exception);
    }

    @ExceptionHandler(RecursiveNotificationValidationException.class)
    public ResponseEntity<RecursiveErrorResponse> handleNotificationValidation(
            final RecursiveNotificationValidationException exception) {
        return response(HttpStatus.BAD_REQUEST, RecursiveErrorCode.INVALID_NOTIFICATION,
                "Invalid recursive notification.", exception);
    }

    @ExceptionHandler(RecursiveJobNotFoundException.class)
    public ResponseEntity<RecursiveErrorResponse> handleJobNotFound(final RecursiveJobNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, RecursiveErrorCode.RECURSIVE_JOB_NOT_FOUND,
                "Recursive job not found.", exception);
    }

    @ExceptionHandler(RecursiveChainOpeningGrantNotFoundException.class)
    public ResponseEntity<RecursiveErrorResponse> handleGrantNotFound(
            final RecursiveChainOpeningGrantNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, RecursiveErrorCode.CHAIN_OPENING_GRANT_NOT_FOUND,
                "Recursive chain opening grant not found.", exception);
    }

    @ExceptionHandler(RecursiveChainOpeningGrantAlreadyExistsException.class)
    public ResponseEntity<RecursiveErrorResponse> handleGrantAlreadyExists(
            final RecursiveChainOpeningGrantAlreadyExistsException exception) {
        return response(HttpStatus.CONFLICT, RecursiveErrorCode.CHAIN_OPENING_GRANT_ALREADY_EXISTS,
                "Recursive chain opening grant already exists.", exception);
    }

    @ExceptionHandler(RecursiveStoreException.class)
    public ResponseEntity<RecursiveErrorResponse> handleStoreFailure(final RecursiveStoreException exception) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, RecursiveErrorCode.PERSISTENCE_UNAVAILABLE,
                "Recursive IRS persistence is temporarily unavailable.", exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RecursiveErrorResponse> handleInvalidRequest(final IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, RecursiveErrorCode.INVALID_REQUEST, INVALID_REQUEST_MESSAGE,
                exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RecursiveErrorResponse> handleUnexpectedFailure(final Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, RecursiveErrorCode.INTERNAL_ERROR,
                "An unexpected recursive IRS error occurred.", exception);
    }

    private ResponseEntity<RecursiveErrorResponse> response(final HttpStatus status,
            final RecursiveErrorCode code, final String message, final Exception exception) {
        return response(status, code, message, exception, List.of(), List.of());
    }

    private ResponseEntity<RecursiveErrorResponse> response(final HttpStatus status,
            final RecursiveErrorCode code, final String message, final Exception exception,
            final List<String> unknownAspects, final List<String> supportedAspects) {
        final String errorRef = UUID.randomUUID().toString();
        if (status.is5xxServerError()) {
            log.error("Recursive API request failed: status={} code={} errorRef={} causeType={} stackTrace={}",
                    status.value(), code, errorRef, exception.getClass().getName(), safeStackTrace(exception));
        } else {
            log.warn("Recursive API request rejected: status={} code={} errorRef={} causeType={}", status.value(),
                    code, errorRef, exception.getClass().getName());
        }
        return ResponseEntity.status(status)
                             .body(RecursiveErrorResponse.builder()
                                                         .status(status.value())
                                                         .code(code)
                                                         .message(message)
                                                         .errorRef(errorRef)
                                                         .unknownAspects(unknownAspects)
                                                         .supportedAspects(supportedAspects)
                                                         .build());
    }

    private static String safeStackTrace(final Throwable throwable) {
        final Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        final StringJoiner diagnostic = new StringJoiner(" <- ");
        Throwable current = throwable;
        while (current != null && visited.add(current)) {
            diagnostic.add(RecursiveLogValue.of(current.getClass().getName()));
            final StackTraceElement[] stackTrace = current.getStackTrace();
            if (stackTrace != null) {
                for (final StackTraceElement stackTraceElement : stackTrace) {
                    diagnostic.add(RecursiveLogValue.of(stackTraceElement.toString()));
                }
            }
            current = current.getCause();
        }
        return diagnostic.toString();
    }
}
