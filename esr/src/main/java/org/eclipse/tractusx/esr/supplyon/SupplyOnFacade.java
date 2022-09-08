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
package org.eclipse.tractusx.esr.supplyon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for SupplyOn domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupplyOnFacade {

    /**
     * Spike covers ISO14001 type only
     */
    private static final String DEFAULT_CERTIFICATE_TYPE = "ISO14001";

    private static final String REQUESTOR_BPN = "BPNL00000003AYRE"; // TODO

    private final SupplyOnClient supplyOnClient;

    public EsrCertificate getESRCertificate(final String supplierBPN) {
        return supplyOnClient.getESRCertificate(REQUESTOR_BPN, supplierBPN, DEFAULT_CERTIFICATE_TYPE);
    }

}
