/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;

class CustomKeyValueProviderTest {

    @Test
    void shouldReplaceUuidCorrectlyInUri() {
        // arrange
        final UUID uuid = UUID.randomUUID();
        final String uriString = "http://localhost:8080/irs/jobs/" + uuid;
        final MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, uriString);
        final ClientRequestObservationContext context = new ClientRequestObservationContext(request);
        context.setUriTemplate(uriString);

        // act
        final CustomKeyValueProvider provider = new CustomKeyValueProvider();
        final KeyValues keyValues = provider.getLowCardinalityKeyValues(context);
        final Optional<KeyValue> returnedUri = keyValues.stream()
                                                        .filter(keyValue -> keyValue.getKey().equals("uri"))
                                                        .findFirst();

        // assert
        assertThat(returnedUri).isPresent();
        assertThat(returnedUri.get().getValue()).doesNotContain(uuid.toString()).contains("{uuid}");
    }

}