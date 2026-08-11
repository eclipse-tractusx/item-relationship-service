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

import java.util.regex.Pattern;

/**
 * Validation patterns used at recursive IRS boundaries.
 */
public final class RecursivePatternStore {

    private static final String BPNL_VALUE = "BPNL[0-9A-Za-z]{12}";

    private static final String UUID_VALUE = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-"
            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private static final String GLOBAL_ASSET_ID_VALUE = "(?:urn:uuid:)?" + UUID_VALUE;

    private static final String MESSAGE_ID_VALUE = "(?:urn:uuid:)?" + UUID_VALUE;

    public static final String BPNL_STRING = "^" + BPNL_VALUE + "$";

    public static final Pattern BPNL_PATTERN = Pattern.compile(BPNL_STRING);

    public static final String OPTIONAL_BPNL_STRING = "^(?:" + BPNL_VALUE + ")?$";

    public static final String GLOBAL_ASSET_ID_STRING = "^" + GLOBAL_ASSET_ID_VALUE + "$";

    public static final Pattern GLOBAL_ASSET_ID_PATTERN = Pattern.compile(GLOBAL_ASSET_ID_STRING);

    public static final String OPTIONAL_GLOBAL_ASSET_ID_STRING = "^(?:" + GLOBAL_ASSET_ID_VALUE + ")?$";

    public static final String MESSAGE_ID_STRING = "^" + MESSAGE_ID_VALUE + "$";

    public static final Pattern MESSAGE_ID_PATTERN = Pattern.compile(MESSAGE_ID_STRING);

    public static final String SAFE_SINGLE_LINE_STRING = "^[^\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]+$";

    public static final Pattern SAFE_SINGLE_LINE_PATTERN = Pattern.compile(SAFE_SINGLE_LINE_STRING);

    public static final String OPTIONAL_SAFE_SINGLE_LINE_STRING = "^[^\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]*$";

    private RecursivePatternStore() {
    }
}
