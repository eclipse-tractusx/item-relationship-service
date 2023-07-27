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
package org.eclipse.tractusx.irs.edc.client.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.data.StringMapper;

/**
 * The decoded Auth code JWT.
 */
@Builder
@Data
@Jacksonized
public class EDRAuthCode {
    private final long exp;
    private final String dad;
    private final String cid;

    public static EDRAuthCode extractContractAgreementId(final String token) {
        final var chunks = token.split("\\.");
        final var decoder = Base64.getUrlDecoder();
        final var payload = new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8);
        return StringMapper.mapFromString(payload, EDRAuthCode.class);
    }
}
