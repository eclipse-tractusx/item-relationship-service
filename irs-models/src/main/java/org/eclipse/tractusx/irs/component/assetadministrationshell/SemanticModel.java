/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import com.vdurmont.semver4j.Semver;

record SemanticModel(String urn, String type, String name, Semver version) {

    private static final int VALID_SEMANTIC_ID_LENGTH = 4;

    static SemanticModel parse(final String semanticId) {
        final String[] parts = semanticId.split(":");
        if (parts.length != VALID_SEMANTIC_ID_LENGTH) {
            throw new IllegalArgumentException("Invalid semanticId value, cant parse: " + semanticId);
        }

        final String version = parts[3].split("#")[0];
        return new SemanticModel(parts[0], parts[1], parts[2], new Semver(version));
    }

    boolean matches(final SemanticModel model) {
        return urn.equals(model.urn)
                && type.equals(model.type)
                && name.equals(model.name)
                && versionIsInRange(model);
    }

    private boolean versionIsInRange(final SemanticModel model) {
        final Semver nextMajor = model.version.nextMajor();
        final Semver minMajor = new Semver(model.version.getMajor() + ".0.0");

        return version.isGreaterThanOrEqualTo(minMajor) && version.isLowerThan(nextMajor);
    }
}
