/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import lombok.Getter;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;

/**
 * Relationship aspect types
 */
@SuppressWarnings("PMD.FieldNamingConventions")
public enum RelationshipAspect {
    SingleLevelBomAsPlanned(SingleLevelBomAsPlanned.class),
    AssemblyPartRelationship(AssemblyPartRelationship.class);
//    SingleLevelUsageAsBuilt;

    @Getter
    private final Class<? extends RelationshipSubmodel> clazz;

    RelationshipAspect(Class<? extends RelationshipSubmodel> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param bomLifecycle lifecycle
     * @param direction direction
     * @return Returns traversal aspect type
     * asBuilt + downward => AssemblyPartRelationship
     * asPlanned + downward => SingleLevelBomAsPlanned
     * asBuilt + upward => SingleLevelUsageAsBuilt
     * asPlanned + upward => SingleLevelXXXAsPlanned
     */
    public static RelationshipAspect from(final BomLifecycle bomLifecycle, final Direction direction) {
        if (bomLifecycle.equals(BomLifecycle.AS_BUILT) && direction.equals(Direction.DOWNWARD)) {
            return RelationshipAspect.AssemblyPartRelationship;
        } else if (bomLifecycle.equals(BomLifecycle.AS_PLANNED) && direction.equals(Direction.DOWNWARD)) {
            return RelationshipAspect.SingleLevelBomAsPlanned;
        }
        throw new UnsupportedOperationException("Not implemented");
    }
}
