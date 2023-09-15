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
package org.eclipse.tractusx.irs.policystore.models;

import java.time.OffsetDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request object for policy creation
 *
 * @param policyId   the ID of the policy
 * @param validUntil the timestamp after which the policy should no longer be accepted
 */
@Schema(description = "Request to add a policy")
public record CreatePolicyRequest(@Schema(description = "The ID of the policy to add") @NotNull String policyId,

                                  @Schema(description = "Timestamp after which the policy will no longer be accepted in negotiations") @NotNull OffsetDateTime validUntil,
                                  @Schema(description = "List of permissions that will be add to Policy on creation.") @NotNull List<Permission> permissions
                                  ) {

}
