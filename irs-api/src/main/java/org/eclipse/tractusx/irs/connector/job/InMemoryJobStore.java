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
package org.eclipse.tractusx.irs.connector.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages storage of {@link MultiTransferJob} state in memory with no persistence.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({ "PMD.TooManyMethods",
                    "PMD.PreserveStackTrace"
})

public class InMemoryJobStore extends BaseJobStore {

    /**
     * The collection of stored jobs.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap") // externally synchronized
    private final Map<String, MultiTransferJob> jobsById = new HashMap<>();

    @Override
    protected Optional<MultiTransferJob> get(final String jobId) {
        return Optional.ofNullable(jobsById.get(jobId));
    }

    @Override
    protected Collection<MultiTransferJob> getAll() {
        return jobsById.values();
    }

    @Override
    protected void put(final String jobId, final MultiTransferJob job) {
        jobsById.put(jobId, job);
    }

    @Override
    protected Optional<MultiTransferJob> remove(final String jobId) {
        return Optional.ofNullable(jobsById.remove(jobId));
    }

}
