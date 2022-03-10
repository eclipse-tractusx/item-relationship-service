//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Exception thrown by the service when the message could not be sent to Kafka.
 */
@ResponseStatus(value = /* 500 */ INTERNAL_SERVER_ERROR, reason = "Failed to send message to broker")
public class MessageProducerFailedException extends RuntimeException {

    /**
     * Constructs a new {@link MessageProducerFailedException}
     * exception with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public MessageProducerFailedException(final Throwable cause) {
        super(cause);
    }
}
