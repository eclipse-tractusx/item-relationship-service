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
package org.eclipse.tractusx.irs.edc.client.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class AcceptedPoliciesProviderTest {

    private final AcceptedPoliciesProvider.DefaultAcceptedPoliciesProvider testee = new AcceptedPoliciesProvider.DefaultAcceptedPoliciesProvider();

    @Test
    void getAcceptedPolicies() {
        final var acceptedPolicies = testee.getAcceptedPolicies();

        assertThat(acceptedPolicies).isEmpty();
    }

    @Test
    void shouldReturnStoredPolicies() {
        testee.addAcceptedPolicies(List.of(policy()));
        final var acceptedPolicies = testee.getAcceptedPolicies();

        assertThat(acceptedPolicies).hasSize(1);
    }
    @Test
    void shouldRemoveStoredPolicies() {
        testee.addAcceptedPolicies(List.of(policy()));
        final var acceptedPolicies = testee.getAcceptedPolicies();

        assertThat(acceptedPolicies).hasSize(1);

        testee.removeAcceptedPolicies(acceptedPolicies);

        assertThat(testee.getAcceptedPolicies()).isEmpty();
    }
    @NotNull
    private static AcceptedPolicy policy() {
        return new AcceptedPolicy(new Policy(), OffsetDateTime.now());
    }
}