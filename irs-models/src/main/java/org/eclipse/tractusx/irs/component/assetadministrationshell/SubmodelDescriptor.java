/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vdurmont.semver4j.SemverException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * SubmodelDescriptor
 */
@Data
@Builder
@Jacksonized
@SuppressWarnings("PMD.ShortVariable")
public class SubmodelDescriptor {

    /**
     * administration
     */
    private AdministrativeInformation administration;
    /**
     * description
     */
    @ArraySchema(maxItems = Integer.MAX_VALUE)
    private List<LangString> description;
    /**
     * idShort
     */
    private String idShort;
    /**
     * identification
     */
    private String id;
    /**
     * semanticId
     */
    private Reference semanticId;
    /**
     * endpoint
     */
    @ArraySchema(maxItems = Integer.MAX_VALUE)
    private List<Endpoint> endpoints;

    /**
     * @return The first value of the semanticId or null, if none is present.
     */
    @JsonIgnore
    public String getAspectType() {
        return getSemanticId().getKeys().stream().findFirst().map(SemanticId::getValue).orElse(null);
    }

    /* package */ boolean isAspect(final String filterSemanticId) {
        return Optional.ofNullable(getAspectType())
                .map(semanticId -> semanticId.contains(lowerCaseNameWithUnderscores(filterSemanticId))
                        || semanticModelNamesMatchAndVersionIsInRange(semanticId, filterSemanticId))
                .orElse(false);
    }

    private String lowerCaseNameWithUnderscores(final String filterSemanticId) {
        return String.join("_", filterSemanticId.split("(?=[A-Z])")).toLowerCase(Locale.ROOT);
    }

    private boolean semanticModelNamesMatchAndVersionIsInRange(final String semanticId, final String filterSemanticId) {
        try {
            final SemanticModel submodel = SemanticModel.parse(semanticId);
            final SemanticModel filter = SemanticModel.parse(filterSemanticId);

            return filter.matches(submodel);
        } catch (final IllegalArgumentException | SemverException e) {
            return false;
        }
    }
}
