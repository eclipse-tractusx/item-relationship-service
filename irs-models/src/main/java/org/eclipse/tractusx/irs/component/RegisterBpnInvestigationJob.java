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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.component;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for registering new job
 */
@Schema(description = "The requested job definition.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterBpnInvestigationJob {

    private static final String GLOBAL_ASSET_ID_REGEX = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final String BPN_REGEX = "(BPN)[LSA][\\w\\d]{10}[\\w\\d]{2}";
    private static final int UUID_SIZE = 36;
    private static final int URN_PREFIX_SIZE = 9;
    private static final int GLOBAL_ASSET_ID_SIZE = URN_PREFIX_SIZE + UUID_SIZE;

    @Pattern(regexp = GLOBAL_ASSET_ID_REGEX)
    @NotBlank
    @Size(min = GLOBAL_ASSET_ID_SIZE, max = GLOBAL_ASSET_ID_SIZE)
    @Schema(description = "Id of global asset.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
            implementation = String.class, minLength = GLOBAL_ASSET_ID_SIZE, maxLength = GLOBAL_ASSET_ID_SIZE)
    private String globalAssetId;

    @NotEmpty
    @ArraySchema(schema = @Schema(description = "Array of BPN numbers.", example = "BPNS000000000DDD", implementation = String.class, pattern = BPN_REGEX), maxItems = Integer.MAX_VALUE)
    private List<@Pattern(regexp = BPN_REGEX) String> incidentBpns;

    @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @URL
    @Schema(description = "Callback url to notify requestor when job processing is finished. There are two uri variable placeholders that can be used: jobId and jobState.",
            example = "https://hostname.com/callback?jobId={jobId}&jobState={jobState}")
    private String callbackUrl;

}
