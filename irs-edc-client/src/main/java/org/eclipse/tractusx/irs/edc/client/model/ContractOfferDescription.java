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
package org.eclipse.tractusx.irs.edc.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.edc.policy.model.Policy;

/**
 * EDC Contract Offer Description.
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class ContractOfferDescription {
    public static final String CONTRACT_OFFER_TYPE = "https://w3id.org/edc/v0.0.1/ns/ContractOfferDescription";
    public static final String CONTRACT_OFFER_OFFER_ID = "https://w3id.org/edc/v0.0.1/ns/offerId";
    public static final String CONTRACT_OFFER_ASSET_ID = "https://w3id.org/edc/v0.0.1/ns/assetId";
    public static final String CONTRACT_OFFER_POLICY = "https://w3id.org/edc/v0.0.1/ns/policy";
    private @NotBlank(message = "offerId is mandatory") String offerId;
    private @NotBlank(message = "assetId is mandatory") String assetId;
    private @NotNull(message = "policy cannot be null") Policy policy;
    private @Positive(message = "validity must be positive") long validity;
}
