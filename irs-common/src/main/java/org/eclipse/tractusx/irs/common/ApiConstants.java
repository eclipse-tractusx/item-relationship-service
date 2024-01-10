/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Common constants for the IRS API
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public class ApiConstants {

    /**
     * The URL prefix for IRS internal URLs (not publicly available)
     */
    public static final String API_PREFIX_INTERNAL = "internal";

    /**
     * Api description for 401 http status
     */
    public static final String UNAUTHORIZED_DESC = "No valid authentication credentials.";
    /**
     * Api description for 403 http status
     */
    public static final String FORBIDDEN_DESC = "Authorization refused by server.";

}
