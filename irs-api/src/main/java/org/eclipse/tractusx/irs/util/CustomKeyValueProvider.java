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
package org.eclipse.tractusx.irs.util;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.stereotype.Service;

/**
 * This class provides a custom prometheus tag configuration for the request buckets.
 * The default configuration includes the query string in the URI tag. This leads
 * to lots of metric entries for each different call.
 */
@Service
public class CustomKeyValueProvider extends DefaultClientRequestObservationConvention {
    private static final String GLOBAL_ASSET_ID_REGEX = "urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    @Override
    public @NotNull KeyValues getLowCardinalityKeyValues(@NotNull final ClientRequestObservationContext context) {

        KeyValues keyValues = KeyValues.empty();

        keyValues = keyValues.and(
                KeyValues.of(clientName(context), outcome(context), method(context), status(context)));

        final KeyValue uri = uri(context);

        keyValues = keyValues.and(
                KeyValue.of(uri.getKey(), uri.getValue().replaceAll(GLOBAL_ASSET_ID_REGEX, "{uuid}")));

        return keyValues;
    }
}
