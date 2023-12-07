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
package org.eclipse.tractusx.irs.edc.client.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * EDC EDR entry response.
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class EndpointDataReferenceEntryResponse {

    @JsonProperty("edc:agreementId")
    private String agreementId;
    @JsonProperty("edc:transferProcessId")
    private String transferProcessId;
    @JsonProperty("edc:assetId")
    private String assetId;
    @JsonProperty("edc:providerId")
    private String providerId;
    @JsonProperty("@type")
    private String type;
    @JsonProperty("tx:edrState")
    private String state;
    @JsonProperty("tx:expirationDate")
    private Instant expirationDate;
    @JsonProperty("edc:errorDetail")
    private String errorDetail;

}
