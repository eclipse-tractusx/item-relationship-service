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
package org.eclipse.tractusx.irs.recursive.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import lombok.RequiredArgsConstructor;

import org.eclipse.tractusx.irs.recursive.model.RecursiveJobState;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;

/**
 * Serializes all state mutations of a recursive job.
 *
 * <p>The blob store has no compare-and-swap, so concurrent child responses, send failures and
 * timeout sweeps could otherwise overwrite each other (lost update). Every read-modify-write
 * cycle must go through {@link #updateIfNotTerminal(UUID, RecursiveJobState, UnaryOperator)} -
 * there is intentionally no other mutation path for existing jobs.</p>
 */
@RequiredArgsConstructor
public class RecursiveJobRepository {

    private final RecursiveJobStateStore store;

    /**
     * One lock object per jobId. Entries are kept for the pod lifetime; jobs are few and the
     * objects are tiny, so no eviction is needed.
     */
    private final ConcurrentHashMap<UUID, Object> jobLocks = new ConcurrentHashMap<>();

    public Optional<RecursiveJobState> findById(final UUID jobId) {
        return store.findById(jobId);
    }

    public List<RecursiveJobState> findAll() {
        return store.findAll();
    }

    public Optional<UUID> findJobIdByIncomingRequestMessageId(final String messageId) {
        return store.findJobIdByIncomingRequestMessageId(messageId);
    }

    public void registerIncomingRequestMessageId(final String messageId, final UUID jobId) {
        store.registerIncomingRequestMessageId(messageId, jobId);
    }

    public Optional<UUID> findJobIdByChildRequestMessageId(final String messageId) {
        return store.findJobIdByChildRequestMessageId(messageId);
    }

    public void registerChildRequestMessageId(final String messageId, final UUID jobId) {
        store.registerChildRequestMessageId(messageId, jobId);
    }

    /**
     * Persists a freshly created job. Existing jobs must be changed via updateIfNotTerminal.
     *
     * @param state freshly created job state
     */
    public void saveNew(final RecursiveJobState state) {
        store.save(state);
    }

    /**
     * Applies {@code update} to the current persisted state of the job while holding the per-job
     * lock. The update is skipped when the job is already terminal or when {@code update} returns
     * null (= condition not met, nothing to change).
     *
     * @param jobId    the job to update
     * @param fallback state to use when the job is not present in the store
     * @param update   pure transformation from current to next state; null aborts the update
     * @return the persisted next state, or empty when the update was skipped
     */
    public Optional<RecursiveJobState> updateIfNotTerminal(final UUID jobId, final RecursiveJobState fallback,
            final UnaryOperator<RecursiveJobState> update) {
        synchronized (jobLocks.computeIfAbsent(jobId, id -> new Object())) {
            final RecursiveJobState current = store.findById(jobId).orElse(fallback);
            if (isTerminal(current)) {
                return Optional.empty();
            }
            final RecursiveJobState next = update.apply(current);
            if (next == null) {
                return Optional.empty();
            }
            store.save(next);
            return Optional.of(next);
        }
    }

    public static boolean isTerminal(final RecursiveJobState state) {
        return state.getState().isTerminal();
    }
}
