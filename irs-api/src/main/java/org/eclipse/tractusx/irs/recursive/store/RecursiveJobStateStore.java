/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;

/**
 * Persistence abstraction for recursive job state.
 *
 * <p>Production persistence is provided by {@link BlobRecursiveJobStateStore};
 * unit tests use lightweight test doubles where needed.</p>
 */
public interface RecursiveJobStateStore {

    /**
     * Saves or replaces a job state.
     *
     * @param state the state to persist
     */
    void save(RecursiveJobState state);

    /**
     * Finds a job by its ID.
     *
     * @param jobId the job identifier
     * @return the stored job state, if present
     */
    Optional<RecursiveJobState> findById(UUID jobId);

    Optional<UUID> findJobIdByIncomingRequestMessageId(String messageId);

    void registerIncomingRequestMessageId(String messageId, UUID jobId);

    Optional<UUID> findJobIdByChildRequestMessageId(String messageId);

    void registerChildRequestMessageId(String messageId, UUID jobId);

    /**
     * Returns all stored job states.
     *
     * @return all persisted recursive job states
     */
    List<RecursiveJobState> findAll();
}
