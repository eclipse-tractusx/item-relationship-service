/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.connector.job;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.Getter;
import org.eclipse.tractusx.irs.component.JobErrorDetails;

/**
 * Job Exception with embedded JobErrorDetails
 */
public class JobException extends RuntimeException {
    private static final String DEFAULT_ERROR_MESSAGE = "Critical error occurred!";

    @Getter
    private final JobErrorDetails jobErrorDetails;

    public JobException() {
        super(DEFAULT_ERROR_MESSAGE);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(ResponseStatus.FATAL_ERROR.toString())
                                         .errorDetail(DEFAULT_ERROR_MESSAGE)
                                         .exceptionDate(ZonedDateTime.now(ZoneOffset.UTC))
                                         .build();
    }

    public JobException(final String message) {
        super(message);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(message)
                                         .errorDetail(DEFAULT_ERROR_MESSAGE)
                                         .exceptionDate(ZonedDateTime.now(ZoneOffset.UTC))
                                         .build();
    }

    public JobException(final String message, final String detail) {
        super(message);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(message)
                                         .errorDetail(detail)
                                         .exceptionDate(ZonedDateTime.now(ZoneOffset.UTC))
                                         .build();
    }

    public JobException(final String message, final Throwable cause) {
        super(message, cause);
        jobErrorDetails = JobErrorDetails.builder()
                                         .exception(cause.getMessage())
                                         .errorDetail(message)
                                         .exceptionDate(ZonedDateTime.now(ZoneOffset.UTC))
                                         .build();
    }
}
