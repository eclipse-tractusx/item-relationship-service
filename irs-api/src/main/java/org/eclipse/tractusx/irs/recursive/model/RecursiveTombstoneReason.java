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

import java.util.Locale;

/** Privacy-safe reasons exposed by recursive tombstones. */
public enum RecursiveTombstoneReason {
    RECURSIVE_DEADLINE_EXCEEDED(true),
    CHILD_RESPONSE_TIMEOUT(true),
    CHILD_BRANCH_FAILED(true),
    CHILD_RESPONSE_INVALID(true),
    CHAIN_OPENING_REJECTED(false),
    LOCAL_ASPECT_NOT_AVAILABLE(false),
    LOCAL_ASPECT_REQUEST_FAILED(true),
    PART_TYPE_INFORMATION_NOT_AVAILABLE(false),
    PART_TYPE_INFORMATION_REQUEST_FAILED(true),
    BOM_QUANTITY_NOT_AVAILABLE(false),
    BOM_CHILD_GLOBAL_ASSET_ID_INVALID(false),
    BOM_SUBMODEL_NOT_SUPPORTED(false),
    BOM_SUBMODEL_ENDPOINT_MISSING(false),
    UNSUPPORTED_ANONYMIZED_ASPECT(false);

    private final boolean retryable;

    RecursiveTombstoneReason(final boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public static RecursiveTombstoneReason fromInternalReason(final String value) {
        if (value == null || value.isBlank()) {
            return CHILD_BRANCH_FAILED;
        }
        String normalized = value.trim();
        final int detailSeparator = normalized.indexOf(':');
        if (detailSeparator > 0) {
            normalized = normalized.substring(0, detailSeparator);
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ASPECT_NOT_SUPPORTED", "UNSUPPORTED_ANONYMIZED_ASPECT" ->
                    UNSUPPORTED_ANONYMIZED_ASPECT;
            case "SHELL_NOT_FOUND", "SUBMODEL_NOT_FOUND", "LOCAL_ASPECT_NOT_AVAILABLE" ->
                    LOCAL_ASPECT_NOT_AVAILABLE;
            case "SUBMODEL_REQUEST_FAILED", "DIGITAL_TWIN_REQUEST_FAILED", "USAGE_POLICY_EXPIRED",
                 "USAGE_POLICY_VALIDATION_FAILED", "LOCAL_ASPECT_REQUEST_FAILED" -> LOCAL_ASPECT_REQUEST_FAILED;
            case "PART_TYPE_INFORMATION_NOT_AVAILABLE" -> PART_TYPE_INFORMATION_NOT_AVAILABLE;
            case "PART_TYPE_INFORMATION_REQUEST_FAILED" -> PART_TYPE_INFORMATION_REQUEST_FAILED;
            case "BOM_QUANTITY_NOT_AVAILABLE" -> BOM_QUANTITY_NOT_AVAILABLE;
            case "BOM_CHILD_GLOBAL_ASSET_ID_INVALID" -> BOM_CHILD_GLOBAL_ASSET_ID_INVALID;
            case "BOM_SUBMODEL_NOT_SUPPORTED" -> BOM_SUBMODEL_NOT_SUPPORTED;
            case "BOM_SUBMODEL_ENDPOINT_MISSING" -> BOM_SUBMODEL_ENDPOINT_MISSING;
            case "CHILD_RESPONSE_TIMEOUT" -> CHILD_RESPONSE_TIMEOUT;
            case "RECURSIVE_DEADLINE_EXCEEDED" -> RECURSIVE_DEADLINE_EXCEEDED;
            case "CHAIN_OPENING_REJECTED" -> CHAIN_OPENING_REJECTED;
            case "CHILD_RESPONSE_INVALID" -> CHILD_RESPONSE_INVALID;
            case "EDC_NOTIFICATION_FAILED", "RECURSIVE_JOB_PROCESSING_FAILED", "CHILD_BRANCH_FAILED" ->
                    CHILD_BRANCH_FAILED;
            default -> CHILD_BRANCH_FAILED;
        };
    }
}
