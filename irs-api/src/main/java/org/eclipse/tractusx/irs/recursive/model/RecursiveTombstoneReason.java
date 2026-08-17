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
import java.util.Map;

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

    private static final Map<String, RecursiveTombstoneReason> INTERNAL_REASONS = Map.ofEntries(
            Map.entry("ASPECT_NOT_SUPPORTED", UNSUPPORTED_ANONYMIZED_ASPECT),
            Map.entry("UNSUPPORTED_ANONYMIZED_ASPECT", UNSUPPORTED_ANONYMIZED_ASPECT),
            Map.entry("SHELL_NOT_FOUND", LOCAL_ASPECT_NOT_AVAILABLE),
            Map.entry("SUBMODEL_NOT_FOUND", LOCAL_ASPECT_NOT_AVAILABLE),
            Map.entry("LOCAL_ASPECT_NOT_AVAILABLE", LOCAL_ASPECT_NOT_AVAILABLE),
            Map.entry("SUBMODEL_REQUEST_FAILED", LOCAL_ASPECT_REQUEST_FAILED),
            Map.entry("DIGITAL_TWIN_REQUEST_FAILED", LOCAL_ASPECT_REQUEST_FAILED),
            Map.entry("USAGE_POLICY_EXPIRED", LOCAL_ASPECT_REQUEST_FAILED),
            Map.entry("USAGE_POLICY_VALIDATION_FAILED", LOCAL_ASPECT_REQUEST_FAILED),
            Map.entry("LOCAL_ASPECT_REQUEST_FAILED", LOCAL_ASPECT_REQUEST_FAILED),
            Map.entry("PART_TYPE_INFORMATION_NOT_AVAILABLE", PART_TYPE_INFORMATION_NOT_AVAILABLE),
            Map.entry("PART_TYPE_INFORMATION_REQUEST_FAILED", PART_TYPE_INFORMATION_REQUEST_FAILED),
            Map.entry("BOM_QUANTITY_NOT_AVAILABLE", BOM_QUANTITY_NOT_AVAILABLE),
            Map.entry("BOM_CHILD_GLOBAL_ASSET_ID_INVALID", BOM_CHILD_GLOBAL_ASSET_ID_INVALID),
            Map.entry("BOM_SUBMODEL_NOT_SUPPORTED", BOM_SUBMODEL_NOT_SUPPORTED),
            Map.entry("BOM_SUBMODEL_ENDPOINT_MISSING", BOM_SUBMODEL_ENDPOINT_MISSING),
            Map.entry("CHILD_RESPONSE_TIMEOUT", CHILD_RESPONSE_TIMEOUT),
            Map.entry("RECURSIVE_DEADLINE_EXCEEDED", RECURSIVE_DEADLINE_EXCEEDED),
            Map.entry("CHAIN_OPENING_REJECTED", CHAIN_OPENING_REJECTED),
            Map.entry("CHILD_RESPONSE_INVALID", CHILD_RESPONSE_INVALID),
            Map.entry("EDC_NOTIFICATION_FAILED", CHILD_BRANCH_FAILED),
            Map.entry("RECURSIVE_JOB_PROCESSING_FAILED", CHILD_BRANCH_FAILED),
            Map.entry("CHILD_BRANCH_FAILED", CHILD_BRANCH_FAILED));

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
        return INTERNAL_REASONS.getOrDefault(normalized, CHILD_BRANCH_FAILED);
    }
}
