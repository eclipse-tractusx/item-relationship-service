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
 * Thrown when a root or partner request names aspects outside its recursive use-case bundle.
 */
public class RecursiveUnsupportedAspectException extends IllegalArgumentException {

    private final List<String> unknownAspects;
    private final List<String> supportedAspects;

    public RecursiveUnsupportedAspectException(final List<String> unknownAspects,
            final List<String> supportedAspects) {
        super("Unknown or unsupported aspect(s) for the recursive path: " + String.join(", ", unknownAspects));
        this.unknownAspects = List.copyOf(unknownAspects);
        this.supportedAspects = List.copyOf(supportedAspects);
    }

    public List<String> getUnknownAspects() {
        return unknownAspects;
    }

    public List<String> getSupportedAspects() {
        return supportedAspects;
    }
}
