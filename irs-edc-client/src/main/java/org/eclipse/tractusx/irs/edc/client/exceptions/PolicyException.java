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
package org.eclipse.tractusx.irs.edc.client.exceptions;

import lombok.Getter;
import org.eclipse.edc.policy.model.Policy;

/**
 * Policy exception with detail information
 */
@Getter
public class PolicyException extends EdcClientException {

    private final transient Policy policy;
    private final String businessPartnerNumber;

    public PolicyException(final String message, final Policy policy, final String businessPartnerNumber) {
        super(message);
        this.policy = policy;
        this.businessPartnerNumber = businessPartnerNumber;
    }
}
