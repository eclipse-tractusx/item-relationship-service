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

package org.eclipse.tractusx.irs.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.actuate.health.Status;

/**
 * Utility class with helper methods for health status
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HealthStatusHelper {

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_DOWN = 1;
    public static final int STATUS_OUT_OF_SERVICE = 2;
    public static final int STATUS_UP = 3;

    /**
     * Converts health status for usage with Gauge.
     *
     * @param status the health status
     * @return the numeric representation of the health status
     */
    public static int healthStatusToNumeric(final Status status) {

        // see Spring documentation - map health indicators to metrics:
        //     https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
        //            #howto.actuator.map-health-indicators-to-metrics

        if (Status.UP.equals(status)) {
            return STATUS_UP;
        }
        if (Status.OUT_OF_SERVICE.equals(status)) {
            return STATUS_OUT_OF_SERVICE;
        }
        if (Status.DOWN.equals(status)) {
            return STATUS_DOWN;
        }

        return STATUS_UNKNOWN;
    }
}
