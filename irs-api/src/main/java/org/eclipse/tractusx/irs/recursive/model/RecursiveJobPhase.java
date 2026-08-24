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
package org.eclipse.tractusx.irs.recursive.model;

import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * Internal lifecycle phases of a recursive job.
 *
 * <p>Each phase maps to the generic IRS {@link JobState} exposed by the job status API.</p>
 */
public enum RecursiveJobPhase {

    /** Grant has been validated; BOM resolution and partner filtering are queued or running. */
    GRANT_CHECKED,

    /** Partner notifications have been sent. Awaiting child responses. */
    AWAITING_CHILDREN,

    /** Job completed successfully. */
    COMPLETED,

    /** Job failed (grant rejected, timeout, or error). */
    FAILED;

    /**
     * Maps this recursive phase to the generic IRS job state exposed by the API.
     *
     * @return the corresponding generic IRS job state
     */
    public JobState toJobState() {
        return switch (this) {
            case GRANT_CHECKED -> JobState.RUNNING;
            case AWAITING_CHILDREN -> JobState.RUNNING;
            case COMPLETED -> JobState.COMPLETED;
            case FAILED -> JobState.ERROR;
        };
    }

    /**
     * @return true when the recursive job cannot be updated anymore
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
