//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy.controllers;

import lombok.extern.slf4j.Slf4j;
import net.catenax.brokerproxy.exceptions.MessageProducerFailedException;
import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.dtos.ErrorResponse;
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
public class BrokerProxyExceptionHandler {

    /**
     * Handler for message producer failed exception
     * @param exception see {@link MessageProducerFailedException}
     * @return see {@link ErrorResponse}
     */
    @ExceptionHandler(MessageProducerFailedException.class)
    public ResponseEntity<ErrorResponse> handleMessageProducerFailed(final MessageProducerFailedException exception) {
        log.error(exception.getClass().getName(), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .withStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                        .withMessage(ApiErrorsConstants.MESSAGE_WRITER_FAILED)
                        .withErrors(new ArrayList<>()).build());
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
}
