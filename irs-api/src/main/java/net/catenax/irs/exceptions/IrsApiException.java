//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.exceptions;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.enums.StatusCodeEnum;

/**
 * Irs API request exception
 */
@Schema(description = "Detailed exception information")
@Value
@Jacksonized
@Builder(toBuilder = true)
public class IrsApiException {

    List<Throwable> exception;

    String errorDetails;

    Instant exceptionDate;

    StatusCodeEnum statusCode;

}
