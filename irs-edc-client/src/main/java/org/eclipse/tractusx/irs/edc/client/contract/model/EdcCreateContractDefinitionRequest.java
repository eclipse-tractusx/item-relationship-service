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
package org.eclipse.tractusx.irs.edc.client.contract.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;
import org.eclipse.tractusx.irs.edc.client.asset.model.EdcContext;

/**
 * EdcCreateContractDefinitionRequest used for creation of contract
 */

@ToString
@Builder
public class EdcCreateContractDefinitionRequest {

    @JsonProperty("@context")
    private EdcContext edcContext;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("@id")
    private String contractDefinitionId;

    @JsonProperty("accessPolicyId")
    private String accessPolicyId;

    @JsonProperty("contractPolicyId")
    private String contractPolicyId;

    @JsonProperty("assetsSelector")
    private EdcContractDefinitionCriteria assetsSelector;

}
