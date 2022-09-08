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

import java.util.Random;

import org.springframework.stereotype.Service;

/**
 * OnSupplyAPI Rest Client
 */
interface SupplyOnClient {

    /**
     *
     * @param requestorBPN BPNL number of the requestor
     * @param supplierBPN BPNL number of the requestors - supplier.
     * @param certificateType Requested certificate type
     * @return ESR Certificate payload
     */
    EsrCertificate getESRCertificate(String requestorBPN, String supplierBPN, String certificateType);

}

/**
 * OnSupplyAPI Rest Client Stub used in local environment
 */
@Service
class SupplyOnClientLocalStub implements SupplyOnClient {

    @Override
    public EsrCertificate getESRCertificate(final String requestorBPN, final String supplierBPN, final String certificateType) {
        return EsrCertificate
                .builder()
                .certificateState(randomCertificateState())
                .build();
    }

    private CertificateState randomCertificateState()  {
        final int rndIndex = new Random().nextInt(CertificateState.values().length);
        return CertificateState.values()[rndIndex];
    }
}
