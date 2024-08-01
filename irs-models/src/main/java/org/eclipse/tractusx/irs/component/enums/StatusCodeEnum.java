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
package org.eclipse.tractusx.irs.component.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.eclipse.tractusx.irs.component.IStatusCodeEnum;

/**
 * Http return status code
 */
@Schema(description = "Http return status code")
public enum StatusCodeEnum implements IStatusCodeEnum {

    BAD_REQUEST(400, "BAD_REQUEST"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    PAYMENT_REQUIRED(402, "PAYMENT_REQUIRED"),
    FORBIDDEN(403, "FORBIDDEN"),
    NOT_FOUND(404, "NOT_FOUND"),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED"),
    NOT_ACCEPTABLE(406, "NOT_ACCEPTABLE"),
    PROXY_AUTHENTICATION_REQUIRED(407, "PROXY_AUTHENTICATION_REQUIRED"),
    REQUEST_TIMEOUT(408, "REQUEST_TIMEOUT"),
    CONFLICT(409, "CONFLICT"),
    GONE(410, "GONE"),
    LENGTH_REQUIRED(411, "LENGTH_REQUIRED"),
    PRECONDITION_FAILED(412, "PRECONDITION_FAILED"),
    PAYLOAD_TOO_LARGE(413, "PAYLOAD_TOO_LARGE"),
    REQUEST_ENTITY_TOO_LARGE(413, "REQUEST_ENTITY_TOO_LARGE"),
    URI_TOO_LONG(414, "URI_TOO_LONG"),
    REQUEST_URI_TOO_LONG(414, "REQUEST_URI_TOO_LONG"),
    UNSUPPORTED_MEDIA_TYPE(415, "UNSUPPORTED_MEDIA_TYPE"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "REQUESTED_RANGE_NOT_SATISFIABLE"),
    EXPECTATION_FAILED(417, "EXPECTATION_FAILED"),
    I_AM_A_TEAPOT(418, "I_AM_A_TEAPOT"),
    INSUFFICIENT_SPACE_ON_RESOURCE(419, "INSUFFICIENT_SPACE_ON_RESOURCE"),
    METHOD_FAILURE(420, "METHOD_FAILURE"),
    DESTINATION_LOCKED(421, "DESTINATION_LOCKED"),
    UNPROCESSABLE_ENTITY(422, "UNPROCESSABLE_ENTITY"),
    LOCKED(423, "LOCKED"),
    FAILED_DEPENDENCY(424, "FAILED_DEPENDENCY"),
    TOO_EARLY(425, "TOO_EARLY"),
    UPGRADE_REQUIRED(426, "UPGRADE_REQUIRED"),
    PRECONDITION_REQUIRED(428, "PRECONDITION_REQUIRED"),
    TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "REQUEST_HEADER_FIELDS_TOO_LARGE"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "UNAVAILABLE_FOR_LEGAL_REASONS"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR"),
    NOT_IMPLEMENTED(501, "NOT_IMPLEMENTED"),
    BAD_GATEWAY(502, "BAD_GATEWAY"),
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE"),
    GATEWAY_TIMEOUT(504, "GATEWAY_TIMEOUT"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP_VERSION_NOT_SUPPORTED"),
    VARIANT_ALSO_NEGOTIATES(506, "VARIANT_ALSO_NEGOTIATES"),
    INSUFFICIENT_STORAGE(507, "INSUFFICIENT_STORAGE"),
    LOOP_DETECTED(508, "LOOP_DETECTED"),
    BANDWIDTH_LIMIT_EXCEEDED(509, "BANDWIDTH_LIMIT_EXCEEDED"),
    NOT_EXTENDED(510, "NOT_EXTENDED"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "NETWORK_AUTHENTICATION_REQUIRED");

    @Getter
    Integer code;

    @Getter
    String message;

    StatusCodeEnum(final Integer code, final String message) {
        this.code = code;
        this.message = message;
    }

}
