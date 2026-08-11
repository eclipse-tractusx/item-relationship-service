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
package org.eclipse.tractusx.irs.recursive.service;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyExpiredException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyPermissionException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;

/**
 * Classifies recursive failures without leaking partner-specific details into API-visible tombstones.
 */
final class RecursiveFailureReasonMapper {

    private RecursiveFailureReasonMapper() {
    }

    /**
     * Derives the external-safe reason for a job-processing failure.
     *
     * @param exception the failure thrown while processing an accepted job
     * @return external-safe recursive tombstone reason
     */
    /* package */ static RecursiveTombstoneReason failureReason(final Exception exception) {
        final Optional<RecursiveTombstoneReason> policyReason = policyReason(exception);
        if (policyReason.isPresent()) {
            return policyReason.get();
        }
        if (exception instanceof RecursiveChainOpeningGrantInactiveException) {
            return RecursiveTombstoneReason.CHAIN_OPENING_REJECTED;
        }
        if (exception instanceof RecursiveExternalCallException externalCallException) {
            return RecursiveTombstoneReason.fromInternalReason(externalCallException.getReason());
        }
        final String detail = failureSignal(exception);
        if (detail.contains("digital_twin_request_failed") || detail.contains("dtr")
                || detail.contains("digital twin") || detail.contains("shell")) {
            return RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED;
        }
        if (detail.contains("submodel_request_failed") || detail.contains("submodel") || detail.contains("bom")) {
            return RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED;
        }
        if (detail.contains("edc_notification_failed") || detail.contains("edc") || detail.contains("notification")
                || detail.contains("connector endpoint")) {
            return RecursiveTombstoneReason.CHILD_BRANCH_FAILED;
        }
        return RecursiveTombstoneReason.CHILD_BRANCH_FAILED;
    }

    private static String failureSignal(final Exception exception) {
        final StringBuilder signal = new StringBuilder();
        Throwable current = exception;
        while (current != null) {
            signal.append(' ').append(current.getClass().getSimpleName());
            if (current.getMessage() != null) {
                signal.append(' ').append(current.getMessage());
            }
            current = current.getCause();
        }
        return RecursiveFailureDetails.anonymizedDetail(signal.toString()).toLowerCase(Locale.ROOT);
    }

    /* package */ static Optional<RecursiveTombstoneReason> policyReason(final Throwable failure) {
        if (hasCause(failure, UsagePolicyExpiredException.class)) {
            return Optional.of(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
        }
        if (hasCause(failure, UsagePolicyPermissionException.class)) {
            return Optional.of(RecursiveTombstoneReason.LOCAL_ASPECT_REQUEST_FAILED);
        }
        return Optional.empty();
    }

    private static boolean hasCause(final Throwable failure, final Class<? extends Throwable> type) {
        Throwable current = failure;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
