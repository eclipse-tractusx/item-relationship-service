//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import static feign.FeignException.errorStatus;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

/**
 * customize the thrown Exceptions, greater than the status code 500, as RetryableException
 */
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(final String methodKey, final Response response) {
        final FeignException exception = errorStatus(methodKey, response);
        final int status = response.status();
        final int statusCode = 500;

        if (status >= statusCode) {
            return new RetryableException(response.status(), exception.getMessage(), response.request().httpMethod(),
                    exception, null, response.request());
        }
        return exception;
    }
}
