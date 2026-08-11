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

import java.util.List;
import java.util.regex.Pattern;

/**
 * Produces tombstone-safe failure details. Details are useful for debugging but
 * must not leak partner URLs, BPNLs or asset identifiers.
 */
final class RecursiveFailureDetails {

    private static final int MAX_DETAIL_LENGTH = 240;
    private static final Pattern URL = Pattern.compile("https?://[^\\s,'\")]+");
    private static final Pattern URN_UUID = Pattern.compile("urn:uuid:[0-9a-fA-F-]{36}");
    private static final Pattern UUID = Pattern.compile(
            "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final Pattern BPN = Pattern.compile("\\bBPN[A-Z0-9]{10,20}\\b");
    private static final List<NamedValuePattern> NAMED_VALUE_PATTERNS = List.of(
            new NamedValuePattern(Pattern.compile("(?i)\\b(globalAssetId=)[^,;\\s]+"), "$1<globalAssetId>"),
            new NamedValuePattern(Pattern.compile("(?i)\\b(bpn=)[^,;\\s]+"), "$1<bpn>"),
            new NamedValuePattern(Pattern.compile("(?i)\\b(assetId=)[^,;\\s]+"), "$1<assetId>"),
            new NamedValuePattern(Pattern.compile("(?i)\\b(connectorEndpoint=)[^,;\\s]+"), "$1<url>"));

    private RecursiveFailureDetails() {
    }

    /* package */ static String anonymizedDetail(final Exception exception) {
        return anonymizedDetail(rootMessage(exception));
    }

    /* package */ static String anonymizedDetail(final String detail) {
        String sanitized = detail == null || detail.isBlank() ? "External recursive request failed." : detail;
        for (NamedValuePattern replacement : NAMED_VALUE_PATTERNS) {
            sanitized = replacement.pattern().matcher(sanitized).replaceAll(replacement.replacement());
        }
        sanitized = URL.matcher(sanitized).replaceAll("<url>");
        sanitized = URN_UUID.matcher(sanitized).replaceAll("<globalAssetId>");
        sanitized = UUID.matcher(sanitized).replaceAll("<id>");
        sanitized = BPN.matcher(sanitized).replaceAll("<bpn>");
        if (sanitized.length() > MAX_DETAIL_LENGTH) {
            return sanitized.substring(0, MAX_DETAIL_LENGTH - 3) + "...";
        }
        return sanitized;
    }

    private static String rootMessage(final Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        final String message = rootCause.getMessage() == null ? exception.getMessage() : rootCause.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message;
    }

    private record NamedValuePattern(Pattern pattern, String replacement) {
    }
}
