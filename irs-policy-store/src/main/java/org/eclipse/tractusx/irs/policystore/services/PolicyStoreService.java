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
package org.eclipse.tractusx.irs.policystore.services;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to manage stored policies in IRS.
 */
@Service
public class PolicyStoreService {

    private final String apiAllowedBpn;

    private final Clock clock;

    private final PolicyPersistence persistence;

    public PolicyStoreService(@Value("${apiAllowedBpn:}") final String apiAllowedBpn,
            final PolicyPersistence persistence, final Clock clock) {
        this.apiAllowedBpn = apiAllowedBpn;
        this.persistence = persistence;
        this.clock = clock;
    }

    public void registerPolicy(final CreatePolicyRequest request) {
        persistence.save(apiAllowedBpn,
                new Policy(request.policyId(), OffsetDateTime.now(clock), request.validUntil()));
    }

    public List<Policy> getStoredPolicies() {
        return persistence.readAll(apiAllowedBpn);
    }

    public void deletePolicy(final String policyId) {
        persistence.delete(apiAllowedBpn, policyId);
    }
}
