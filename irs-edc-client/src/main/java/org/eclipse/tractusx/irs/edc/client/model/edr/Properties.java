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
package org.eclipse.tractusx.irs.edc.client.model.edr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Properties represents the properties of a transfer process EDR callback.
 *
 * @param processId       The process ID of the callback.
 * @param participantId   The participant ID i.e. BPN.
 * @param assetId         The EDC asset ID of the negotiation.
 * @param endpointType     The type of the endpoint. E.g. "https://w3id.org/idsa/v4.1/HTTP"
 * @param refreshEndpoint  The refresh endpoint to get a new Token.
 * @param audience         The audience DID.
 * @param agreementId     The EDC contract agreement ID.
 * @param flowType        The flow type. E.g. "PULL"
 * @param type             The type. E.g. "https://w3id.org/idsa/v4.1/HTTP"
 * @param endpoint         The EDC dataplane endpoint.
 * @param refreshToken     The refresh JWT token.
 * @param expiresIn        The expiration time in seconds.
 * @param authorization    The authorization JWT token.
 * @param refreshAudience  The refresh audience DID.
 */
@Builder
@Jacksonized
public record Properties(@JsonProperty("process_id") String processId,
                  @JsonProperty("participant_id") String participantId,
                  @JsonProperty("asset_id") String assetId,
                  @JsonProperty(NAMESPACE_EDC + "endpointType") String endpointType,
                  @JsonProperty(NAMESPACE_TRACTUSX_AUTH + "refreshEndpoint") String refreshEndpoint,
                  @JsonProperty(NAMESPACE_TRACTUSX_AUTH + "audience") String audience,
                  @JsonProperty("agreement_id") String agreementId,
                  @JsonProperty("flow_type") String flowType,
                  @JsonProperty(NAMESPACE_EDC + "type") String type,
                  @JsonProperty(NAMESPACE_EDC + "endpoint") String endpoint,
                  @JsonProperty(NAMESPACE_TRACTUSX_AUTH + "refreshToken") String refreshToken,
                  @JsonProperty(NAMESPACE_TRACTUSX_AUTH + "expiresIn") String expiresIn,
                  @JsonProperty(NAMESPACE_EDC + "authorization") String authorization,
                  @JsonProperty(NAMESPACE_TRACTUSX_AUTH + "refreshAudience") String refreshAudience) {

    public static final String NAMESPACE_EDC = "https://w3id.org/edc/v0.0.1/ns/";
    public static final String NAMESPACE_TRACTUSX_AUTH = "https://w3id.org/tractusx/auth/";
}
