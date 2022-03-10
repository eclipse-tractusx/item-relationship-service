// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerProxy;

import net.catenax.brokerproxy.controllers.ApiErrorsConstants;
import net.catenax.prs.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Object Mother to generate expected Broker API responses for integration tests.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class BrokerProxyResponseMother {

    /**
     * Generates error response for invalid arguments provided scenario.
     * @param errors List of errors.
     * @return An {@link ErrorResponse} object containing list of supplied errors.
     */
    public ErrorResponse invalidArgument(List<String> errors) {
        return ErrorResponse.builder()
                .withStatusCode(HttpStatus.BAD_REQUEST)
                .withMessage(ApiErrorsConstants.INVALID_ARGUMENTS)
                .withErrors(errors).build();
    }
}
