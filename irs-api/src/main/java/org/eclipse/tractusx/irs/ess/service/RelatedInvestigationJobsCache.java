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
package org.eclipse.tractusx.irs.ess.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Related Investigation Jobs Cache
 */
interface RelatedInvestigationJobsCache {

    void store(String notificationId, RelatedInvestigationJobs relatedInvestigationJobs);
    Optional<RelatedInvestigationJobs> findByRecursiveRelatedJobId(UUID relatedJobId);
    void remove(String notificationId);

}

/**
 * Temporary in memory implementation
 */
@Service
class InMemoryRelatedInvestigationJobsCache implements RelatedInvestigationJobsCache {

    private final ConcurrentHashMap<String, RelatedInvestigationJobs> inMemory = new ConcurrentHashMap<>();

    @Override
    public void store(final String notificationId, final RelatedInvestigationJobs relatedInvestigationJobs) {
        inMemory.put(notificationId, relatedInvestigationJobs);
    }

    @Override
    public Optional<RelatedInvestigationJobs> findByRecursiveRelatedJobId(final UUID relatedJobId) {
        return inMemory.values().stream()
                       .filter(relatedInvestigationJobs -> relatedInvestigationJobs.recursiveRelatedJobIds().contains(relatedJobId))
                .findFirst();

    }

    @Override
    public void remove(final String notificationId) {
        inMemory.remove(notificationId);
    }
}
