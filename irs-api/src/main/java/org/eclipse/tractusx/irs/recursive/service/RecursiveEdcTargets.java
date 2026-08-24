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

/**
 * Deterministic ordering for EDC connector endpoint candidates.
 *
 * <p>Discovery may return multiple connector endpoints per partner in unstable order. All
 * recursive callers try the candidates in this normalized order (deduplicated, lexicographic)
 * and accept the first endpoint that answers; only when every candidate fails does the call
 * abort hard. This keeps retries and error reports reproducible across runs and instances.</p>
 */
final class RecursiveEdcTargets {

    private RecursiveEdcTargets() {
    }

    /* package */ static List<String> deterministicOrder(final List<String> connectorEndpoints) {
        if (connectorEndpoints == null) {
            return List.of();
        }
        return connectorEndpoints.stream()
                                 .filter(endpoint -> endpoint != null && !endpoint.isBlank())
                                 .distinct()
                                 .sorted()
                                 .toList();
    }
}
