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
package org.eclipse.tractusx.irs.recursive.util;

import java.util.UUID;

/**
 * Canonical handling for recursive global asset identifiers.
 */
public final class RecursiveGlobalAssetId {

    private static final String URN_UUID_PREFIX = "urn:uuid:";

    private RecursiveGlobalAssetId() {
    }

    public static String canonicalize(final String globalAssetId) {
        if (globalAssetId == null || !RecursivePatternStore.GLOBAL_ASSET_ID_PATTERN.matcher(globalAssetId).matches()) {
            throw new IllegalArgumentException("globalAssetId must be a UUID or URN UUID");
        }
        final String uuidValue = globalAssetId.startsWith(URN_UUID_PREFIX)
                ? globalAssetId.substring(URN_UUID_PREFIX.length())
                : globalAssetId;
        return URN_UUID_PREFIX + UUID.fromString(uuidValue).toString();
    }

    public static String canonicalizeOptional(final String globalAssetId) {
        return globalAssetId == null || globalAssetId.isBlank() ? globalAssetId : canonicalize(globalAssetId);
    }
}
