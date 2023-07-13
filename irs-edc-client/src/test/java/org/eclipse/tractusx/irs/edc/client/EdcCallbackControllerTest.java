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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_CID;

import java.time.Duration;
import java.util.Map;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.junit.jupiter.api.Test;

class EdcCallbackControllerTest {

    private final EndpointDataReferenceStorage storage = new EndpointDataReferenceStorage(Duration.ofMinutes(1));
    private final EdcCallbackController testee = new EdcCallbackController(storage);

    @Test
    void shouldStoreAgreementId() {
        // arrange
        final var ref = EndpointDataReference.Builder.newInstance()
                                                     .endpoint("test")
                                                     .properties(Map.of(NAMESPACE_EDC_CID, "testId"))
                                                     .build();

        // act
        testee.receiveEdcCallback(ref);

        // assert
        final var result = storage.remove("testId");
        assertThat(result).isNotNull().contains(ref);
    }
}