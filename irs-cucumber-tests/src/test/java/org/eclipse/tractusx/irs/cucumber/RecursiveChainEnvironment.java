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
package org.eclipse.tractusx.irs.cucumber;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Value;

/**
 * Addresses and identifiers of the recursive chain under test.
 * <p>
 * The values differ per environment and are therefore never written into a scenario. Keeping them
 * here means a changed BPNL or test dataset is a change to the workflow configuration, not to the
 * test cases in the board.
 */
@Value
/* package */ class RecursiveChainEnvironment {

    private static final String ROOT_URL = "RECURSIVE_ROOT_URL";
    private static final String ROOT_BPNL = "RECURSIVE_ROOT_BPNL";
    private static final String ROOT_ALLOWED_BPNLS = "RECURSIVE_ROOT_ALLOWED_BPNLS";
    private static final String ROOT_GLOBAL_ASSET_ID = "RECURSIVE_ROOT_GLOBAL_ASSET_ID";
    private static final String OPENING_ID = "RECURSIVE_OPENING_ID";
    private static final String USE_CASE = "RECURSIVE_USE_CASE";

    private static final String DEFAULT_USE_CASE = "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE";

    private static final Pattern BPNL_PATTERN = Pattern.compile("^BPNL[a-zA-Z0-9]{12}$");
    private static final Pattern GLOBAL_ASSET_ID_PATTERN = Pattern.compile(
            "^(urn:uuid:)?[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$");

    String rootUrl;
    String rootBpnl;
    /** Only the partners the root instance may ask directly, not every participant of the chain. */
    List<String> rootAllowedBpnls;
    String rootGlobalAssetId;
    String openingId;
    String useCase;

    /* package */
    static RecursiveChainEnvironment fromEnvironment() throws PropertyNotFoundException {
        return new RecursiveChainEnvironment(required(ROOT_URL),
                requireBpnl(ROOT_BPNL, required(ROOT_BPNL)),
                splitBpnls(ROOT_ALLOWED_BPNLS, required(ROOT_ALLOWED_BPNLS)),
                requireGlobalAssetId(ROOT_GLOBAL_ASSET_ID, required(ROOT_GLOBAL_ASSET_ID)),
                required(OPENING_ID), optional(USE_CASE, DEFAULT_USE_CASE));
    }

    private static String required(final String name) throws PropertyNotFoundException {
        final String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new PropertyNotFoundException("Environment Variable missing: " + name);
        }
        return value.trim();
    }

    private static String optional(final String name, final String fallback) {
        final String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static List<String> splitBpnls(final String name, final String value)
            throws PropertyNotFoundException {
        final List<String> bpnls = Arrays.stream(value.split(","))
                                         .map(String::trim)
                                         .filter(bpnl -> !bpnl.isEmpty())
                                         .toList();
        if (bpnls.isEmpty()) {
            throw new PropertyNotFoundException("Environment Variable contains no BPNL: " + name);
        }
        for (final String bpnl : bpnls) {
            requireBpnl(name, bpnl);
        }
        return bpnls;
    }

    private static String requireBpnl(final String name, final String value) throws PropertyNotFoundException {
        if (!BPNL_PATTERN.matcher(value).matches()) {
            throw new PropertyNotFoundException("Environment Variable is not a valid BPNL: " + name);
        }
        return value;
    }

    private static String requireGlobalAssetId(final String name, final String value)
            throws PropertyNotFoundException {
        if (!GLOBAL_ASSET_ID_PATTERN.matcher(value).matches()) {
            throw new PropertyNotFoundException("Environment Variable is not a valid globalAssetId: " + name);
        }
        return value;
    }
}
