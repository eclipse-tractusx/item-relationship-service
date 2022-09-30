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
package org.eclipse.tractusx.esr.irs.model.shell;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 *  Submodel Descriptor
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class SubmodelDescriptor {

    private static final String ASSEMBLY_PART_RELATIONSHIP = "io.catenax.assembly_part_relationship";
    private static final String ESR_CERTIFICATE = "io.catenax.esr_certificates.esr_certificate_state_statistic";

    private String identification;
    private ListOfValues semanticId;
    private List<Endpoint> endpoints;

    public Boolean isPartRelationship() {
        return semanticId != null && semanticId.getValue().stream()
                                               .anyMatch(id -> id.contains(ASSEMBLY_PART_RELATIONSHIP));
    }

    public Boolean isEsrCertificate() {
        return semanticId != null && semanticId.getValue().stream()
                                               .anyMatch(id -> id.contains(ESR_CERTIFICATE));
    }
}
