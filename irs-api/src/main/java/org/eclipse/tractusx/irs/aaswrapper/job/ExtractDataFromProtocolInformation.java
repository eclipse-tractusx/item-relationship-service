/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.job;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Extract asset id and path suffix from Protocol Information
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtractDataFromProtocolInformation {
    public static final String DSP_ENDPOINT = "dspEndpoint";
    public static final String DSP_ID = "id";
    private static final String ASSET_ID_SEPARATOR = ";";

    public static String extractAssetId(final String subprotocolBody) {
        final Map<String, String> parametersFromPath = Stream.of(subprotocolBody.split(ASSET_ID_SEPARATOR))
                                                             .map(str -> str.split("="))
                                                             .collect(Collectors.toMap(e -> e[0], e -> e[1]));
        return parametersFromPath.get(DSP_ID);
    }

    public static Optional<String> extractDspEndpoint(final String subprotocolBody) {
        if (subprotocolBody.contains(DSP_ENDPOINT)) {
            final Map<String, String> parametersFromPath = Stream.of(subprotocolBody.split(ASSET_ID_SEPARATOR))
                                                                 .map(str -> str.split("="))
                                                                 .collect(Collectors.toMap(e -> e[0], e -> e[1]));
            if (parametersFromPath.containsKey(DSP_ENDPOINT)) {
                return Optional.of(parametersFromPath.get(DSP_ENDPOINT));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

}
