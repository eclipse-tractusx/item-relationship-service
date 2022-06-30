//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import java.io.Serializable;
import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Exception container for job
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({ "PMD.ShortClassName", "PMD.MethodArgumentCouldBeFinal" })
public class JobErrorDetails implements Serializable {

    public static final int EXCEPTION_NAME_MAX_LENGTH = 100;
    public static final int ERROR_DETAIL_MAX_LENGTH = 4000;

    @Schema(description = "Exception name.", implementation = String.class,
            maxLength = EXCEPTION_NAME_MAX_LENGTH)
    private String exception;

    @Schema(description = "Detailed exception information.", implementation = String.class,
            maxLength = ERROR_DETAIL_MAX_LENGTH)
    private String errorDetail;

    @Schema(description = "Datetime error occurs.", implementation = ZonedDateTime.class)
    private ZonedDateTime exceptionDate;

}
