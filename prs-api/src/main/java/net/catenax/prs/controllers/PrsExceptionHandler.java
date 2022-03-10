//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.controllers;

import lombok.extern.slf4j.Slf4j;
import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.dtos.ErrorResponse;
import net.catenax.prs.exceptions.EntityNotFoundException;
import net.catenax.prs.exceptions.MaxDepthTooLargeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API Exception Handler.
 */
@Slf4j
@ExcludeFromCodeCoverageGeneratedReport
@ControllerAdvice
public class PrsExceptionHandler {

    /**
     * Handler for max depth too large exception
     * @param exception see {@link MaxDepthTooLargeException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(MaxDepthTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleMaxDepthTooLarge(final MaxDepthTooLargeException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .withStatusCode(HttpStatus.BAD_REQUEST)
                        .withMessage(ApiErrorsConstants.INVALID_DEPTH)
                        .withErrors(List.of(exception.getMessage())).build());
    }

    /**
     * Handler for entity not found exception
     * @param exception see {@link EntityNotFoundException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(final EntityNotFoundException exception) {
        log.info(exception.getClass().getName(), exception);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .withStatusCode(HttpStatus.NOT_FOUND)
                        .withMessage(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .withErrors(List.of(exception.getMessage())).build());
    }

    /**
     * Handler for spring BindException
     * @param exception see {@link BindException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(final BindException exception) {
        log.info(exception.getClass().getName(), exception);

        final List<String> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .withStatusCode(HttpStatus.BAD_REQUEST)
                        .withMessage(ApiErrorsConstants.INVALID_ARGUMENTS)
                        .withErrors(errors).build());
    }

    /**
     * Catcher for all unhandled exceptions
     * @param exception see {@link Exception}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(final Exception exception) {
        log.error(exception.getClass().getName(), exception);
        // Exception error not used in response to prevent leak of any possible sensitive information.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .withStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                        .withMessage("Error Occurred")
                        .withErrors(new ArrayList<>()).build());
    }
}
