//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.dtos.ErrorResponse;
import net.catenax.irs.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * API Exception Handler.
 */
@Slf4j
@ExcludeFromCodeCoverageGeneratedReport
@ControllerAdvice
public class IrsExceptionHandler {

    /**
     * Handler for entity not found exception
     *
     * @param exception see {@link EntityNotFoundException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(final EntityNotFoundException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.NOT_FOUND)
                                                .withMessage(HttpStatus.NOT_FOUND.getReasonPhrase())
                                                .withErrors(List.of(exception.getMessage()))
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
                                             .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withMessage(ApiErrorsConstants.INVALID_ARGUMENTS)
                                                .withErrors(errors)
                                                .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException exception) {
        log.info(exception.getClass().getName(), exception);

        if (exception.getRootCause() instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception.getRootCause());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withMessage(exception.getMessage())
                                                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {
        log.info(exception.getClass().getName(), exception);

        String message = "Malformed JSON request";

        if (exception.getRootCause() instanceof NoSuchElementException) {
            message = exception.getRootCause().getMessage();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withMessage(message)
                                                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ErrorResponse.builder()
                                                .withStatusCode(HttpStatus.BAD_REQUEST)
                                                .withMessage(exception.getMessage())
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
                                                .withMessage("Error Occurred")
                                                .withErrors(new ArrayList<>())
                                                .build());
    }
}
