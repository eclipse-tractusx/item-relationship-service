/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        return this.getSemanticId().getKeys().stream().findFirst().map(SemanticId::getValue).orElse(null);
    }

}
