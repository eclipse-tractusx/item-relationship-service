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

/**
 * Provides a safe representation of external values for recursive IRS logs.
 */
public final class RecursiveLogValue {

    private static final String NULL_VALUE = "<null>";
    private static final String INVALID_VALUE = "<invalid>";

    private RecursiveLogValue() {
    }

    @SuppressWarnings("PMD.ShortMethodName")
    public static String of(final String value) {
        if (value == null) {
            return NULL_VALUE;
        }
        return RecursivePatternStore.SAFE_SINGLE_LINE_PATTERN.matcher(value).matches() ? value : INVALID_VALUE;
    }
}
